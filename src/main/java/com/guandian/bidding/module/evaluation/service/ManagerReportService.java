package com.guandian.bidding.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.module.notify.enums.NotificationType;
import com.guandian.bidding.module.notify.service.NotificationService;
import com.guandian.bidding.module.tender.entity.EvaluationReport;
import com.guandian.bidding.module.tender.entity.ExpertAssignment;
import com.guandian.bidding.module.tender.entity.ReportDoc;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.EvaluationReportMapper;
import com.guandian.bidding.module.tender.mapper.ExpertAssignmentMapper;
import com.guandian.bidding.module.tender.mapper.ReportDocMapper;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerReportService {

    private static final List<String> DEFAULT_DOC_NAMES = Arrays.asList(
            "评标报告正文", "评分汇总表", "澄清说明附件");

    private final ManagerProjectGuard projectGuard;
    private final EvaluationReportMapper reportMapper;
    private final ReportDocMapper reportDocMapper;
    private final ExpertAssignmentMapper assignmentMapper;
    private final EvaluationResultService resultService;
    private final NotificationService notificationService;

    public ReportSummaryResponse getReport(Long projectId) {
        projectGuard.requireOwnedProject(projectId);
        EvaluationReport report = ensureReport(projectId);
        return toSummary(report);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReportSummaryResponse updateReport(Long projectId, ReportUpdateRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        requireEvalFinished(project);
        EvaluationReport report = ensureReport(projectId);
        if (StringUtils.hasText(req.getPurchaseContent())) {
            report.setPurchaseContent(req.getPurchaseContent());
        }
        if (StringUtils.hasText(req.getRejectNote())) {
            report.setRejectNote(req.getRejectNote());
        }
        if (StringUtils.hasText(req.getCandidateList())) {
            report.setCandidateList(req.getCandidateList());
        } else if (report.getCandidateList() == null) {
            report.setCandidateList(buildDefaultCandidateList(projectId));
        }
        if (StringUtils.hasText(req.getClarifyNote())) {
            report.setClarifyNote(req.getClarifyNote());
        }
        report.setGeneratedDocs(report.getTotalDocs());
        report.setStatus(1);
        report.setUpdateBy(SecurityUtils.getUserId());
        reportMapper.updateById(report);
        return toSummary(report);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReportSummaryResponse pushReport(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        EvaluationReport report = ensureReport(projectId);
        List<ReportDoc> docs = listDocs(report.getId());
        for (ReportDoc doc : docs) {
            if (!"DONE".equals(doc.getStatus())) {
                doc.setStatus("PENDING_SIGN");
                reportDocMapper.updateById(doc);
            }
        }
        notifyReportPush(project);
        return toSummary(report);
    }

    private void notifyReportPush(TenderProject project) {
        List<Long> expertIds = assignmentMapper.selectList(new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, project.getId())
                        .eq(ExpertAssignment::getStatus, "SIGNED"))
                .stream()
                .map(ExpertAssignment::getExpertId)
                .collect(Collectors.toList());
        notificationService.sendBatch(expertIds, NotificationType.REPORT,
                "评标报告待签名",
                "「" + project.getName() + "」评标报告已推送，请尽快完成签名。",
                project.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ReportExportResponse exportReport(Long projectId) {
        projectGuard.requireOwnedProject(projectId);
        EvaluationReport report = ensureReport(projectId);
        List<ReportDoc> docs = listDocs(report.getId());
        boolean allSigned = !docs.isEmpty() && docs.stream().allMatch(d -> "DONE".equals(d.getStatus()));
        if (!allSigned) {
            throw new BusinessException(ResultCode.REPORT_NOT_ALL_SIGNED);
        }
        if (report.getExportAttachId() == null) {
            report.setExportAttachId(report.getId());
            reportMapper.updateById(report);
        }
        return ReportExportResponse.builder()
                .projectId(projectId)
                .exportAttachId(report.getExportAttachId())
                .message("评标报告已导出")
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ReportDocItemResponse signReportDoc(Long docId) {
        requireExpert();
        ReportDoc doc = reportDocMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        EvaluationReport report = reportMapper.selectById(doc.getReportId());
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        requireExpertOnProject(report.getProjectId());
        if (!"PENDING_SIGN".equals(doc.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "文档当前不可签名");
        }
        doc.setStatus("DONE");
        doc.setSignedBy(SecurityUtils.getUserId());
        doc.setSignTime(LocalDateTime.now());
        reportDocMapper.updateById(doc);
        return toDocItem(doc);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReportSummaryResponse signAllReport(Long projectId) {
        requireExpert();
        requireExpertOnProject(projectId);
        EvaluationReport report = ensureReport(projectId);
        Long expertId = SecurityUtils.getUserId();
        List<ReportDoc> docs = listDocs(report.getId()).stream()
                .filter(d -> "PENDING_SIGN".equals(d.getStatus()))
                .collect(Collectors.toList());
        for (ReportDoc doc : docs) {
            doc.setStatus("DONE");
            doc.setSignedBy(expertId);
            doc.setSignTime(LocalDateTime.now());
            reportDocMapper.updateById(doc);
        }
        return toSummary(report);
    }

    public ReportSummaryResponse getReportForExpert(Long projectId) {
        requireExpert();
        requireExpertOnProject(projectId);
        return toSummary(ensureReport(projectId));
    }

    private EvaluationReport ensureReport(Long projectId) {
        EvaluationReport report = reportMapper.selectOne(new LambdaQueryWrapper<EvaluationReport>()
                .eq(EvaluationReport::getProjectId, projectId)
                .last("LIMIT 1"));
        if (report != null) {
            return report;
        }
        long signedCount = assignmentMapper.selectCount(new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getProjectId, projectId)
                .eq(ExpertAssignment::getStatus, "SIGNED"));
        int totalDocs = DEFAULT_DOC_NAMES.size() + (int) signedCount;

        report = new EvaluationReport();
        report.setProjectId(projectId);
        report.setTotalDocs(totalDocs);
        report.setGeneratedDocs(0);
        report.setStatus(0);
        report.setCandidateList(buildDefaultCandidateList(projectId));
        report.setCreateBy(SecurityUtils.getUserId());
        reportMapper.insert(report);

        for (String name : DEFAULT_DOC_NAMES) {
            insertDoc(report.getId(), name);
        }
        List<ExpertAssignment> signed = assignmentMapper.selectList(new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getProjectId, projectId)
                .eq(ExpertAssignment::getStatus, "SIGNED"));
        for (ExpertAssignment a : signed) {
            insertDoc(report.getId(), "专家签名页-" + a.getExpertId());
        }
        return report;
    }

    private void insertDoc(Long reportId, String name) {
        ReportDoc doc = new ReportDoc();
        doc.setReportId(reportId);
        doc.setDocName(name);
        doc.setStatus("NOT_GEN");
        reportDocMapper.insert(doc);
    }

    private String buildDefaultCandidateList(Long projectId) {
        FinalScoreResponse scores = resultService.buildFinalScore(projectId);
        if (scores.getCandidates() == null || scores.getCandidates().isEmpty()) {
            return "";
        }
        return scores.getCandidates().stream()
                .map(c -> c.getRank() + "." + c.getCompanyName() + "(" + c.getTotalScore() + "分)")
                .collect(Collectors.joining("\n"));
    }

    private List<ReportDoc> listDocs(Long reportId) {
        return reportDocMapper.selectList(new LambdaQueryWrapper<ReportDoc>()
                .eq(ReportDoc::getReportId, reportId)
                .orderByAsc(ReportDoc::getId));
    }

    private ReportSummaryResponse toSummary(EvaluationReport report) {
        List<ReportDocItemResponse> docs = listDocs(report.getId()).stream()
                .map(this::toDocItem)
                .collect(Collectors.toList());
        return ReportSummaryResponse.builder()
                .projectId(report.getProjectId())
                .reportId(report.getId())
                .totalDocs(report.getTotalDocs())
                .generatedDocs(report.getGeneratedDocs())
                .status(report.getStatus())
                .purchaseContent(report.getPurchaseContent())
                .rejectNote(report.getRejectNote())
                .clarifyNote(report.getClarifyNote())
                .candidateList(report.getCandidateList())
                .exportAttachId(report.getExportAttachId())
                .docs(docs)
                .build();
    }

    private ReportDocItemResponse toDocItem(ReportDoc doc) {
        return ReportDocItemResponse.builder()
                .id(doc.getId())
                .docName(doc.getDocName())
                .status(doc.getStatus())
                .attachId(doc.getAttachId())
                .signedBy(doc.getSignedBy())
                .signTime(doc.getSignTime())
                .build();
    }

    private void requireEvalFinished(TenderProject project) {
        if (!"FINISHED".equals(project.getEvalNode())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "评审节点未完成，不可编辑评标报告");
        }
    }

    private void requireExpert() {
        if (!"EXPERT".equals(SecurityUtils.requireLoginUser().getActiveRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请切换为专家身份");
        }
    }

    private void requireExpertOnProject(Long projectId) {
        ExpertAssignment assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getProjectId, projectId)
                .eq(ExpertAssignment::getExpertId, SecurityUtils.getUserId())
                .last("LIMIT 1"));
        if (assignment == null || !"SIGNED".equals(assignment.getStatus())) {
            throw new BusinessException(ResultCode.EXPERT_NOT_SIGNED);
        }
    }
}
