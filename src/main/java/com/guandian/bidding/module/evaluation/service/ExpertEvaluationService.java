package com.guandian.bidding.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.tender.entity.*;
import com.guandian.bidding.module.tender.mapper.*;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertEvaluationService {

    private final ExpertAssignmentMapper assignmentMapper;
    private final TenderProjectMapper projectMapper;
    private final EvaluationItemMapper evaluationItemMapper;
    private final EvaluationScoreMapper evaluationScoreMapper;
    private final BidRegistrationMapper registrationMapper;
    private final BidDocumentMapper bidDocumentMapper;
    private final EvaluationResultService resultService;

    public List<ExpertAssignmentItemResponse> listAssignments(String status) {
        requireExpert();
        Long expertId = SecurityUtils.getUserId();

        LambdaQueryWrapper<ExpertAssignment> wrapper = new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getExpertId, expertId)
                .orderByDesc(ExpertAssignment::getCreateTime);
        if (StringUtils.hasText(status)) {
            wrapper.eq(ExpertAssignment::getStatus, mapStatusFilter(status));
        }

        List<ExpertAssignment> assignments = assignmentMapper.selectList(wrapper);
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> projectIds = assignments.stream().map(ExpertAssignment::getProjectId).distinct().collect(Collectors.toList());
        Map<Long, TenderProject> projectMap = projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(TenderProject::getId, Function.identity()));

        return assignments.stream().map(a -> {
            TenderProject p = projectMap.get(a.getProjectId());
            return ExpertAssignmentItemResponse.builder()
                    .assignmentId(a.getId())
                    .projectId(a.getProjectId())
                    .projectNo(p != null ? p.getProjectNo() : null)
                    .projectName(p != null ? p.getName() : null)
                    .projectStatus(p != null ? p.getStatus() : null)
                    .bidOpenTime(p != null ? p.getBidOpenTime() : null)
                    .isLeader(a.getIsLeader())
                    .assignmentStatus(a.getStatus())
                    .signTime(a.getSignTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ExpertAssignmentItemResponse respond(Long assignmentId, AssignmentRespondRequest req) {
        ExpertAssignment assignment = requireOwnedAssignment(assignmentId);
        if (!"PENDING".equals(assignment.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "该邀请已处理");
        }
        assignment.setStatus(Boolean.TRUE.equals(req.getAccept()) ? "ACCEPTED" : "REJECTED");
        assignment.setRespondTime(LocalDateTime.now());
        assignmentMapper.updateById(assignment);
        return toItem(assignment);
    }

    @Transactional(rollbackFor = Exception.class)
    public ExpertAssignmentItemResponse signIn(Long assignmentId) {
        ExpertAssignment assignment = requireOwnedAssignment(assignmentId);
        if (!"ACCEPTED".equals(assignment.getStatus()) && !"PENDING".equals(assignment.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "当前状态不可签到");
        }
        TenderProject project = projectMapper.selectById(assignment.getProjectId());
        if (project == null || !"OPENING".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "项目未处于开标中");
        }
        assignment.setStatus("SIGNED");
        assignment.setSignTime(LocalDateTime.now());
        assignmentMapper.updateById(assignment);
        return toItem(assignment);
    }

    @Transactional(rollbackFor = Exception.class)
    public ComplianceResultResponse submitReviews(ExpertReviewRequest request) {
        Long expertId = SecurityUtils.getUserId();
        requireExpert();

        Long projectId = null;
        for (ExpertReviewRequest.ReviewScoreItem item : request.getScores()) {
            BidRegistration reg = requireRegistration(item.getRegistrationId());
            if (projectId == null) {
                projectId = reg.getProjectId();
            } else if (!projectId.equals(reg.getProjectId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "scores 必须属于同一项目");
            }
            requireSignedExpert(projectId, expertId);
            requireDecrypted(reg.getId());

            EvaluationItem evalItem = evaluationItemMapper.selectById(item.getItemId());
            if (evalItem == null || !projectId.equals(evalItem.getProjectId())) {
                throw new BusinessException(ResultCode.NOT_FOUND, "评审项不存在");
            }
            if (!isReviewType(evalItem.getType())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "评审项类型不正确: " + evalItem.getType());
            }
            if (item.getPass() != 0 && item.getPass() != 1) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "pass 只能为 0 或 1");
            }
            upsertReview(projectId, reg.getId(), expertId, evalItem.getId(), item.getPass());
        }
        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "scores 不能为空");
        }
        return resultService.buildCompliance(projectId, null, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public ScoreSummaryResponse submitScores(ExpertScoreSubmitRequest request) {
        Long expertId = SecurityUtils.getUserId();
        requireExpert();

        Long projectId = null;
        int submittedFlag = Boolean.TRUE.equals(request.getSubmitted()) ? 1 : 0;

        for (ExpertScoreSubmitRequest.ScoreItem item : request.getScores()) {
            BidRegistration reg = requireRegistration(item.getRegistrationId());
            if (projectId == null) {
                projectId = reg.getProjectId();
            } else if (!projectId.equals(reg.getProjectId())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "scores 必须属于同一项目");
            }
            requireSignedExpert(projectId, expertId);
            requireDecrypted(reg.getId());

            EvaluationItem evalItem = evaluationItemMapper.selectById(item.getItemId());
            if (evalItem == null || !projectId.equals(evalItem.getProjectId())) {
                throw new BusinessException(ResultCode.NOT_FOUND, "评审项不存在");
            }
            if (!isScoreType(evalItem.getType())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "评审项类型不正确: " + evalItem.getType());
            }
            requireScorePermission(projectId, expertId, evalItem.getType());
            if (evalItem.getMaxScore() != null && item.getScore().compareTo(evalItem.getMaxScore()) > 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "得分不能超过满分 " + evalItem.getMaxScore());
            }
            if (item.getScore().compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "得分不能为负数");
            }
            upsertScore(projectId, reg.getId(), expertId, evalItem.getId(), item.getScore(), submittedFlag);
        }
        if (projectId == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "scores 不能为空");
        }

        if (submittedFlag == 1) {
            tryFinishEvaluation(projectId);
        }
        return resultService.buildScoreSummary(projectId, null, false);
    }

    public FinalScoreResponse getFinalScore(Long projectId) {
        requireExpert();
        requireSignedExpert(projectId, SecurityUtils.getUserId());
        TenderProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return resultService.buildFinalScore(projectId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ExpertAssignmentItemResponse> electLeader(Long projectId, ElectLeaderRequest request) {
        requireExpert();
        Long voterId = SecurityUtils.getUserId();
        requireSignedExpert(projectId, voterId);

        ExpertAssignment target = assignmentMapper.selectOne(new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getProjectId, projectId)
                .eq(ExpertAssignment::getExpertId, request.getLeaderExpertId())
                .last("LIMIT 1"));
        if (target == null || !"SIGNED".equals(target.getStatus())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "被推选专家须为本项目已签到专家");
        }

        List<ExpertAssignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, projectId)
                        .eq(ExpertAssignment::getStatus, "SIGNED"));
        for (ExpertAssignment a : assignments) {
            a.setIsLeader(a.getExpertId().equals(request.getLeaderExpertId()) ? 1 : 0);
            assignmentMapper.updateById(a);
        }

        return assignments.stream().map(a -> {
            TenderProject p = projectMapper.selectById(projectId);
            return ExpertAssignmentItemResponse.builder()
                    .assignmentId(a.getId())
                    .projectId(projectId)
                    .projectNo(p != null ? p.getProjectNo() : null)
                    .projectName(p != null ? p.getName() : null)
                    .projectStatus(p != null ? p.getStatus() : null)
                    .bidOpenTime(p != null ? p.getBidOpenTime() : null)
                    .isLeader(a.getIsLeader())
                    .assignmentStatus(a.getStatus())
                    .signTime(a.getSignTime())
                    .build();
        }).collect(Collectors.toList());
    }

    private void requireScorePermission(Long projectId, Long expertId, String itemType) {
        if ("TECH".equals(itemType)) {
            return;
        }
        ExpertAssignment assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getProjectId, projectId)
                .eq(ExpertAssignment::getExpertId, expertId)
                .last("LIMIT 1"));
        if (assignment == null || assignment.getIsLeader() == null || assignment.getIsLeader() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "商务分/报价分仅专家组长可评分");
        }
    }

    private void upsertReview(Long projectId, Long registrationId, Long expertId, Long itemId, Integer pass) {
        EvaluationScore existing = findScore(registrationId, expertId, itemId);
        if (existing == null) {
            EvaluationScore score = new EvaluationScore();
            score.setProjectId(projectId);
            score.setRegistrationId(registrationId);
            score.setExpertId(expertId);
            score.setItemId(itemId);
            score.setPass(pass);
            score.setSubmitted(1);
            score.setCreateBy(expertId);
            evaluationScoreMapper.insert(score);
        } else {
            existing.setPass(pass);
            existing.setSubmitted(1);
            existing.setUpdateBy(expertId);
            evaluationScoreMapper.updateById(existing);
        }
    }

    private void upsertScore(Long projectId, Long registrationId, Long expertId, Long itemId,
                             BigDecimal scoreValue, int submitted) {
        EvaluationScore existing = findScore(registrationId, expertId, itemId);
        if (existing == null) {
            EvaluationScore score = new EvaluationScore();
            score.setProjectId(projectId);
            score.setRegistrationId(registrationId);
            score.setExpertId(expertId);
            score.setItemId(itemId);
            score.setScore(scoreValue);
            score.setSubmitted(submitted);
            score.setCreateBy(expertId);
            evaluationScoreMapper.insert(score);
        } else {
            if (existing.getSubmitted() != null && existing.getSubmitted() == 1) {
                throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "评分已提交，不可修改");
            }
            existing.setScore(scoreValue);
            existing.setSubmitted(submitted);
            existing.setUpdateBy(expertId);
            evaluationScoreMapper.updateById(existing);
        }
    }

    private EvaluationScore findScore(Long registrationId, Long expertId, Long itemId) {
        return evaluationScoreMapper.selectOne(new LambdaQueryWrapper<EvaluationScore>()
                .eq(EvaluationScore::getRegistrationId, registrationId)
                .eq(EvaluationScore::getExpertId, expertId)
                .eq(EvaluationScore::getItemId, itemId)
                .last("LIMIT 1"));
    }

    private void tryFinishEvaluation(Long projectId) {
        List<ExpertAssignment> signed = assignmentMapper.selectList(
                new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, projectId)
                        .eq(ExpertAssignment::getStatus, "SIGNED"));
        if (signed.isEmpty()) {
            return;
        }
        List<EvaluationItem> scoreItems = evaluationItemMapper.selectList(
                new LambdaQueryWrapper<EvaluationItem>()
                        .eq(EvaluationItem::getProjectId, projectId)
                        .in(EvaluationItem::getType, "COMMERCE", "TECH", "PRICE"));
        if (scoreItems.isEmpty()) {
            return;
        }
        List<BidRegistration> regs = registrationMapper.selectList(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getProjectId, projectId)
                        .eq(BidRegistration::getRegStatus, "SUCCESS"));
        List<Long> qualifiedRegs = regs.stream()
                .filter(r -> resultService.isRegistrationQualified(projectId, r.getId()))
                .map(BidRegistration::getId)
                .collect(Collectors.toList());
        if (qualifiedRegs.isEmpty()) {
            return;
        }

        Long leaderId = signed.stream()
                .filter(a -> a.getIsLeader() != null && a.getIsLeader() == 1)
                .map(ExpertAssignment::getExpertId)
                .findFirst()
                .orElse(null);

        for (Long regId : qualifiedRegs) {
            for (EvaluationItem item : scoreItems) {
                if ("TECH".equals(item.getType())) {
                    for (ExpertAssignment assignment : signed) {
                        EvaluationScore score = findScore(regId, assignment.getExpertId(), item.getId());
                        if (score == null || score.getSubmitted() == null || score.getSubmitted() != 1) {
                            return;
                        }
                    }
                } else if ("COMMERCE".equals(item.getType()) || "PRICE".equals(item.getType())) {
                    if (leaderId == null) {
                        return;
                    }
                    EvaluationScore score = findScore(regId, leaderId, item.getId());
                    if (score == null || score.getSubmitted() == null || score.getSubmitted() != 1) {
                        return;
                    }
                }
            }
        }
        TenderProject project = projectMapper.selectById(projectId);
        if (project != null && !"FINISHED".equals(project.getEvalNode())) {
            project.setEvalNode("FINISHED");
            projectMapper.updateById(project);
        }
    }

    private void requireSignedExpert(Long projectId, Long expertId) {
        ExpertAssignment assignment = assignmentMapper.selectOne(
                new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, projectId)
                        .eq(ExpertAssignment::getExpertId, expertId)
                        .last("LIMIT 1"));
        if (assignment == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "您未被指派参与该项目");
        }
        if (!"SIGNED".equals(assignment.getStatus())) {
            throw new BusinessException(ResultCode.EXPERT_NOT_SIGNED);
        }
        TenderProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!"OPENING".equals(project.getStatus()) && !"OPENED".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "项目未处于开评标阶段");
        }
        if (!"REVIEWING".equals(project.getEvalNode()) && !"FINISHED".equals(project.getEvalNode())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "当前评审节点不允许评分");
        }
    }

    private BidRegistration requireRegistration(Long registrationId) {
        BidRegistration reg = registrationMapper.selectById(registrationId);
        if (reg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在");
        }
        return reg;
    }

    private void requireDecrypted(Long registrationId) {
        BidDocument doc = bidDocumentMapper.selectOne(new LambdaQueryWrapper<BidDocument>()
                .eq(BidDocument::getRegistrationId, registrationId)
                .isNull(BidDocument::getWithdrawTime)
                .orderByDesc(BidDocument::getSubmitTime)
                .last("LIMIT 1"));
        if (doc == null || doc.getDecryptStatus() == null || doc.getDecryptStatus() != 1) {
            throw new BusinessException(ResultCode.BID_FILE_LOCKED, "投标文件尚未解密，不可评审");
        }
    }

    private boolean isReviewType(String type) {
        return "FORMAL".equals(type) || "QUALIFY".equals(type) || "RESPONSE".equals(type);
    }

    private boolean isScoreType(String type) {
        return "COMMERCE".equals(type) || "TECH".equals(type) || "PRICE".equals(type);
    }

    private ExpertAssignment requireOwnedAssignment(Long assignmentId) {
        requireExpert();
        ExpertAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(assignment.getExpertId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return assignment;
    }

    private ExpertAssignmentItemResponse toItem(ExpertAssignment a) {
        TenderProject p = projectMapper.selectById(a.getProjectId());
        return ExpertAssignmentItemResponse.builder()
                .assignmentId(a.getId())
                .projectId(a.getProjectId())
                .projectNo(p != null ? p.getProjectNo() : null)
                .projectName(p != null ? p.getName() : null)
                .projectStatus(p != null ? p.getStatus() : null)
                .bidOpenTime(p != null ? p.getBidOpenTime() : null)
                .isLeader(a.getIsLeader())
                .assignmentStatus(a.getStatus())
                .signTime(a.getSignTime())
                .build();
    }

    private void requireExpert() {
        if (!"EXPERT".equals(SecurityUtils.requireLoginUser().getActiveRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请切换为专家身份");
        }
    }

    /** 支持中文筛选别名 */
    private String mapStatusFilter(String status) {
        switch (status) {
            case "待确认":
                return "PENDING";
            case "未开标":
                return "ACCEPTED";
            case "评审中":
                return "SIGNED";
            default:
                return status;
        }
    }
}
