package com.guandian.bidding.module.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.manager.dto.*;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.module.auth.entity.SysRole;
import com.guandian.bidding.module.auth.entity.SysUser;
import com.guandian.bidding.module.auth.entity.SysUserRole;
import com.guandian.bidding.module.auth.mapper.SysRoleMapper;
import com.guandian.bidding.module.auth.mapper.SysUserMapper;
import com.guandian.bidding.module.auth.mapper.SysUserRoleMapper;
import com.guandian.bidding.module.audit.enums.OperationAction;
import com.guandian.bidding.module.audit.enums.OperationModule;
import com.guandian.bidding.module.audit.service.OperationLogService;
import com.guandian.bidding.module.tender.entity.Announcement;
import com.guandian.bidding.module.tender.entity.EvaluationItem;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.AnnouncementMapper;
import com.guandian.bidding.module.tender.mapper.BidRegistrationMapper;
import com.guandian.bidding.module.tender.mapper.EvaluationItemMapper;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerTenderService {

    private final TenderProjectMapper projectMapper;
    private final AnnouncementMapper announcementMapper;
    private final EvaluationItemMapper evaluationItemMapper;
    private final BidRegistrationMapper registrationMapper;
    private final ManagerProjectGuard projectGuard;
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final OperationLogService operationLogService;

    public PageResult<ManagerTenderSummaryResponse> list(String name, String projectNo, String tenderType,
                                                          String status, LocalDateTime bidOpenFrom,
                                                          LocalDateTime bidOpenTo, long pageNum, long pageSize) {
        Long managerId = SecurityUtils.getUserId();
        LambdaQueryWrapper<TenderProject> wrapper = new LambdaQueryWrapper<TenderProject>()
                .eq(TenderProject::getManagerId, managerId)
                .like(StringUtils.hasText(name), TenderProject::getName, name)
                .like(StringUtils.hasText(projectNo), TenderProject::getProjectNo, projectNo)
                .eq(StringUtils.hasText(tenderType), TenderProject::getTenderType, tenderType)
                .eq(StringUtils.hasText(status), TenderProject::getStatus, status)
                .ge(bidOpenFrom != null, TenderProject::getBidOpenTime, bidOpenFrom)
                .le(bidOpenTo != null, TenderProject::getBidOpenTime, bidOpenTo)
                .orderByDesc(TenderProject::getCreateTime);

        Page<TenderProject> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ManagerTenderSummaryResponse> list = page.getRecords().stream()
                .map(p -> ManagerTenderSummaryResponse.builder()
                        .id(p.getId())
                        .projectNo(p.getProjectNo())
                        .name(p.getName())
                        .procurementType(p.getProcurementType())
                        .tenderType(p.getTenderType())
                        .status(p.getStatus())
                        .evalNode(p.getEvalNode())
                        .bidOpenTime(p.getBidOpenTime())
                        .regEnd(p.getRegEnd())
                        .registrationCount(countRegistrations(p.getId()))
                        .createTime(p.getCreateTime())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public ManagerTenderDetailResponse detail(Long id) {
        TenderProject project = projectGuard.requireOwnedProject(id);
        List<EvaluationItemDto> items = evaluationItemMapper.selectList(
                        new LambdaQueryWrapper<EvaluationItem>()
                                .eq(EvaluationItem::getProjectId, id)
                                .orderByAsc(EvaluationItem::getSort))
                .stream().map(this::toEvalDto).collect(Collectors.toList());
        return ManagerTenderDetailResponse.builder()
                .id(project.getId())
                .projectNo(project.getProjectNo())
                .name(project.getName())
                .section(project.getSection())
                .procurementType(project.getProcurementType())
                .tenderType(project.getTenderType())
                .industry(project.getIndustry())
                .region(project.getRegion())
                .budget(project.getBudget())
                .status(project.getStatus())
                .evalNode(project.getEvalNode())
                .regStart(project.getRegStart())
                .regEnd(project.getRegEnd())
                .bidOpenTime(project.getBidOpenTime())
                .fileFee(project.getFileFee())
                .platformFee(project.getPlatformFee())
                .evalTotalScore(project.getEvalTotalScore())
                .priceScoreMethod(project.getPriceScoreMethod())
                .content(project.getContent())
                .bidFileId(project.getBidFileId())
                .evalItems(items)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public ManagerTenderDetailResponse create(ManagerTenderCreateRequest req) {
        Long managerId = SecurityUtils.getUserId();
        TenderProject project = new TenderProject();
        project.setProjectNo(generateProjectNo());
        fillProject(project, req);
        project.setManagerId(managerId);
        project.setStatus(Boolean.TRUE.equals(req.getPublish()) ? "BIDDING" : "DRAFT");
        project.setEvalNode("NOT_STARTED");
        project.setArchived(0);
        projectMapper.insert(project);

        if ("BIDDING".equals(project.getStatus())) {
            publishAnnouncement(project);
            operationLogService.recordProject(OperationModule.TENDER, OperationAction.PUBLISH,
                    project.getId(), "projectNo=" + project.getProjectNo() + ", name=" + project.getName());
        } else {
            operationLogService.recordProject(OperationModule.TENDER, OperationAction.CREATE,
                    project.getId(), "projectNo=" + project.getProjectNo() + ", status=DRAFT");
        }
        return detail(project.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ManagerTenderDetailResponse update(Long id, ManagerTenderUpdateRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(id);
        projectGuard.requireStatus(project, "BIDDING");
        applyUpdate(project, req);
        projectMapper.updateById(project);
        return detail(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ManagerTenderDetailResponse abort(Long id, AbortTenderRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(id);
        if ("ABORTED".equals(project.getStatus()) || "ARCHIVED".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID);
        }
        project.setStatus("ABORTED");
        String note = "\n[废标原因] " + req.getReason();
        project.setContent((project.getContent() == null ? "" : project.getContent()) + note);
        projectMapper.updateById(project);
        operationLogService.recordProject(OperationModule.TENDER, OperationAction.ABORT, id,
                "reason=" + req.getReason());
        return detail(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ManagerTenderDetailResponse skipEval(Long id) {
        TenderProject project = projectGuard.requireOwnedProject(id);
        projectGuard.requireStatus(project, "BIDDING");
        project.setStatus("AWARDED");
        project.setEvalNode("FINISHED");
        projectMapper.updateById(project);
        operationLogService.recordProject(OperationModule.TENDER, OperationAction.UPDATE, id, "skipEval=true");
        return detail(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ManagerTenderDetailResponse updateEvalItems(Long id, EvalItemsUpdateRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(id);
        projectGuard.requireStatus(project, "BIDDING");

        evaluationItemMapper.delete(new LambdaQueryWrapper<EvaluationItem>()
                .eq(EvaluationItem::getProjectId, id));

        if (req.getItems() != null) {
            int sort = 1;
            for (EvaluationItemDto dto : req.getItems()) {
                EvaluationItem item = new EvaluationItem();
                item.setProjectId(id);
                item.setType(dto.getType());
                item.setName(dto.getName());
                item.setMaxScore(dto.getMaxScore());
                item.setSubTotal(dto.getSubTotal());
                item.setSort(dto.getSort() != null ? dto.getSort() : sort++);
                evaluationItemMapper.insert(item);
            }
        }
        project.setEvalTotalScore(req.getTotalScore());
        project.setPriceScoreMethod(req.getPriceScoreMethod());
        projectMapper.updateById(project);
        return detail(id);
    }

    public ParseTenderResponse parseTender(ParseTenderRequest req) {
        return ParseTenderResponse.builder()
                .name("示例采购项目（自附件解析）")
                .procurementType("PUBLIC")
                .tenderType("GOODS")
                .budget(new java.math.BigDecimal("500000"))
                .content("此为占位解析结果，附件ID=" + req.getAttachId())
                .build();
    }

    private void fillProject(TenderProject project, ManagerTenderCreateRequest req) {
        project.setName(req.getName());
        project.setSection(req.getSection());
        project.setProcurementType(req.getProcurementType());
        project.setTenderType(req.getTenderType());
        project.setIndustry(req.getIndustry());
        project.setRegion(req.getRegion());
        project.setBudget(req.getBudget());
        validateTendererId(req.getTendererId());
        project.setTendererId(req.getTendererId());
        project.setAgencyId(req.getAgencyId());
        project.setFileFee(req.getFileFee());
        project.setPlatformFee(req.getPlatformFee());
        project.setRegStart(req.getRegStart());
        project.setRegEnd(req.getRegEnd());
        project.setBidOpenTime(req.getBidOpenTime());
        project.setContent(req.getContent());
        project.setBidFileId(req.getBidFileId());
    }

    private void applyUpdate(TenderProject project, ManagerTenderUpdateRequest req) {
        if (req.getName() != null) project.setName(req.getName());
        if (req.getSection() != null) project.setSection(req.getSection());
        if (req.getIndustry() != null) project.setIndustry(req.getIndustry());
        if (req.getRegion() != null) project.setRegion(req.getRegion());
        if (req.getBudget() != null) project.setBudget(req.getBudget());
        if (req.getFileFee() != null) project.setFileFee(req.getFileFee());
        if (req.getPlatformFee() != null) project.setPlatformFee(req.getPlatformFee());
        if (req.getRegStart() != null) project.setRegStart(req.getRegStart());
        if (req.getRegEnd() != null) project.setRegEnd(req.getRegEnd());
        if (req.getBidOpenTime() != null) project.setBidOpenTime(req.getBidOpenTime());
        if (req.getContent() != null) project.setContent(req.getContent());
        if (req.getBidFileId() != null) project.setBidFileId(req.getBidFileId());
    }

    private void publishAnnouncement(TenderProject project) {
        Announcement announcement = new Announcement();
        announcement.setProjectId(project.getId());
        announcement.setType(1);
        announcement.setTitle(project.getName() + "招标公告");
        announcement.setContent(project.getContent());
        announcement.setPublishTime(LocalDateTime.now());
        announcementMapper.insert(announcement);
    }

    private String generateProjectNo() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "TP" + date + rand;
    }

    private long countRegistrations(Long projectId) {
        return registrationMapper.selectCount(
                new LambdaQueryWrapper<com.guandian.bidding.module.tender.entity.BidRegistration>()
                        .eq(com.guandian.bidding.module.tender.entity.BidRegistration::getProjectId, projectId));
    }

    private EvaluationItemDto toEvalDto(EvaluationItem item) {
        EvaluationItemDto dto = new EvaluationItemDto();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }

    private void validateTendererId(Long tendererId) {
        if (tendererId == null) {
            return;
        }
        SysUser user = userMapper.selectById(tendererId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "招标人用户不存在");
        }
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "TENDERER"));
        if (role == null) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "招标人角色未配置");
        }
        Long count = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, tendererId)
                .eq(SysUserRole::getRoleId, role.getId())
                .eq(SysUserRole::getAuditStatus, 1));
        if (count == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "指定用户尚未成为招标人，请先发送邀请");
        }
    }
}
