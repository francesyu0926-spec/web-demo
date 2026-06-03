package com.guandian.bidding.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.evaluation.dto.AssignmentRespondRequest;
import com.guandian.bidding.module.evaluation.dto.ExpertAssignmentItemResponse;
import com.guandian.bidding.module.tender.entity.ExpertAssignment;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.ExpertAssignmentMapper;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
