package com.guandian.bidding.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.module.audit.enums.OperationAction;
import com.guandian.bidding.module.audit.enums.OperationModule;
import com.guandian.bidding.module.audit.service.OperationLogService;
import com.guandian.bidding.module.notify.enums.NotificationType;
import com.guandian.bidding.module.notify.service.NotificationService;
import com.guandian.bidding.module.tender.entity.*;
import com.guandian.bidding.module.tender.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerOpeningService {

    private final ManagerProjectGuard projectGuard;
    private final TenderProjectMapper projectMapper;
    private final ExpertAssignmentMapper assignmentMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final BidRegistrationMapper registrationMapper;
    private final BidDocumentMapper bidDocumentMapper;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    @Transactional(rollbackFor = Exception.class)
    public ProjectProgressResponse openProject(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        projectGuard.requireStatus(project, "BIDDING");
        if (project.getBidOpenTime() != null && LocalDateTime.now().isBefore(project.getBidOpenTime())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "尚未到达开标时间");
        }
        project.setStatus("OPENING");
        project.setEvalNode("NOT_STARTED");
        projectMapper.updateById(project);
        notifyOpening(project);
        operationLogService.recordProject(OperationModule.OPENING, OperationAction.OPEN_START,
                projectId, "projectNo=" + project.getProjectNo());
        return getProgress(projectId);
    }

    private void notifyOpening(TenderProject project) {
        List<BidRegistration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getProjectId, project.getId())
                        .eq(BidRegistration::getRegStatus, "SUCCESS"));
        for (BidRegistration reg : registrations) {
            notificationService.send(reg.getSupplierId(), NotificationType.OPEN,
                    "项目已开标",
                    "「" + project.getName() + "」已进入开标阶段，请按时参与解密。",
                    project.getId());
        }
    }

    public ProjectProgressResponse getProgress(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        List<ProgressItemDto> items = new ArrayList<>();

        long expertTotal = assignmentMapper.selectCount(
                new LambdaQueryWrapper<ExpertAssignment>().eq(ExpertAssignment::getProjectId, projectId));
        long expertSigned = assignmentMapper.selectCount(
                new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, projectId)
                        .eq(ExpertAssignment::getStatus, "SIGNED"));
        items.add(ProgressItemDto.builder().name("专家签到")
                .status(expertSigned > 0 ? "IN_PROGRESS" : "PENDING")
                .detail(expertSigned + "/" + expertTotal).build());

        List<BidRegistration> regs = registrationMapper.selectList(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getProjectId, projectId)
                        .eq(BidRegistration::getRegStatus, "SUCCESS"));
        long docTotal = 0;
        long decrypted = 0;
        for (BidRegistration reg : regs) {
            BidDocument doc = bidDocumentMapper.selectOne(
                    new LambdaQueryWrapper<BidDocument>()
                            .eq(BidDocument::getRegistrationId, reg.getId())
                            .isNull(BidDocument::getWithdrawTime)
                            .last("LIMIT 1"));
            if (doc != null) {
                docTotal++;
                if (doc.getDecryptStatus() != null && doc.getDecryptStatus() == 1) {
                    decrypted++;
                }
            }
        }
        items.add(ProgressItemDto.builder().name("文件解密")
                .status(decrypted == docTotal && docTotal > 0 ? "DONE" : "PENDING")
                .detail(decrypted + "/" + docTotal).build());

        items.add(ProgressItemDto.builder().name("符合性审查")
                .status("REVIEWING".equals(project.getEvalNode()) ? "IN_PROGRESS" : "PENDING").build());
        items.add(ProgressItemDto.builder().name("评分汇总")
                .status("FINISHED".equals(project.getEvalNode()) ? "DONE" : "PENDING").build());

        return ProjectProgressResponse.builder()
                .projectId(projectId)
                .projectStatus(project.getStatus())
                .evalNode(project.getEvalNode())
                .items(items)
                .build();
    }

    public List<ExpertSignInItemResponse> getSignInStatus(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        projectGuard.requireStatus(project, "OPENING", "OPENED");

        List<ExpertAssignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<ExpertAssignment>().eq(ExpertAssignment::getProjectId, projectId));
        Map<Long, ExpertProfile> profiles = expertProfileMapper.selectList(
                        new LambdaQueryWrapper<ExpertProfile>().in(
                                ExpertProfile::getUserId,
                                assignments.stream().map(ExpertAssignment::getExpertId).collect(Collectors.toList())))
                .stream().collect(Collectors.toMap(ExpertProfile::getUserId, Function.identity(), (a, b) -> a));

        return assignments.stream().map(a -> {
            ExpertProfile p = profiles.get(a.getExpertId());
            return ExpertSignInItemResponse.builder()
                    .assignmentId(a.getId())
                    .expertId(a.getExpertId())
                    .expertNo(p != null ? p.getExpertNo() : null)
                    .major(p != null ? p.getMajor() : null)
                    .status(a.getStatus())
                    .signTime(a.getSignTime())
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectProgressResponse confirmSignIn(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        projectGuard.requireStatus(project, "OPENING");

        long signed = assignmentMapper.selectCount(
                new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, projectId)
                        .eq(ExpertAssignment::getStatus, "SIGNED"));
        if (signed == 0) {
            throw new BusinessException(ResultCode.EXPERT_NOT_SIGNED, "尚无专家签到，不可进入解密环节");
        }
        project.setEvalNode("REVIEWING");
        projectMapper.updateById(project);
        return getProgress(projectId);
    }

    public List<DecryptStatusItemResponse> getDecryptStatus(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        projectGuard.requireStatus(project, "OPENING", "OPENED");

        List<BidRegistration> regs = registrationMapper.selectList(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getProjectId, projectId)
                        .eq(BidRegistration::getRegStatus, "SUCCESS"));

        List<DecryptStatusItemResponse> list = new ArrayList<>();
        for (BidRegistration reg : regs) {
            BidDocument doc = bidDocumentMapper.selectOne(
                    new LambdaQueryWrapper<BidDocument>()
                            .eq(BidDocument::getRegistrationId, reg.getId())
                            .isNull(BidDocument::getWithdrawTime)
                            .orderByDesc(BidDocument::getSubmitTime)
                            .last("LIMIT 1"));
            if (doc != null) {
                list.add(DecryptStatusItemResponse.builder()
                        .registrationId(reg.getId())
                        .companyName(reg.getCompanyName())
                        .encrypted(doc.getEncrypted())
                        .decryptStatus(doc.getDecryptStatus())
                        .decryptTime(doc.getDecryptTime())
                        .build());
            }
        }
        return list;
    }
}
