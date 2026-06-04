package com.guandian.bidding.module.tender.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.auth.entity.SysUser;
import com.guandian.bidding.module.auth.mapper.SysUserMapper;
import com.guandian.bidding.module.tender.dto.TendererProjectDetailResponse;
import com.guandian.bidding.module.tender.dto.TendererProjectSummaryResponse;
import com.guandian.bidding.module.tender.entity.BidRegistration;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.BidRegistrationMapper;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TendererProjectService {

    private final TenderProjectMapper projectMapper;
    private final BidRegistrationMapper registrationMapper;
    private final SysUserMapper userMapper;

    public PageResult<TendererProjectSummaryResponse> list(String name, String status,
                                                           long pageNum, long pageSize) {
        requireTenderer();
        Long tendererId = SecurityUtils.getUserId();

        LambdaQueryWrapper<TenderProject> wrapper = new LambdaQueryWrapper<TenderProject>()
                .eq(TenderProject::getTendererId, tendererId)
                .like(StringUtils.hasText(name), TenderProject::getName, name)
                .eq(StringUtils.hasText(status), TenderProject::getStatus, status)
                .orderByDesc(TenderProject::getCreateTime);

        Page<TenderProject> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, SysUser> managerMap = loadManagers(page.getRecords());
        List<TendererProjectSummaryResponse> list = page.getRecords().stream()
                .map(project -> toSummary(project, managerMap.get(project.getManagerId())))
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public TendererProjectDetailResponse detail(Long id) {
        requireTenderer();
        TenderProject project = requireOwnedProject(id);
        SysUser manager = userMapper.selectById(project.getManagerId());
        long registrationCount = registrationMapper.selectCount(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getProjectId, id));
        return toDetail(project, manager, registrationCount);
    }

    private TenderProject requireOwnedProject(Long projectId) {
        TenderProject project = projectMapper.selectById(projectId);
        if (project == null || !SecurityUtils.getUserId().equals(project.getTendererId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return project;
    }

    private void requireTenderer() {
        if (!"TENDERER".equals(SecurityUtils.requireLoginUser().getActiveRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请切换为招标人身份");
        }
    }

    private Map<Long, SysUser> loadManagers(List<TenderProject> projects) {
        List<Long> managerIds = projects.stream()
                .map(TenderProject::getManagerId)
                .distinct()
                .collect(Collectors.toList());
        if (managerIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(managerIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    private TendererProjectSummaryResponse toSummary(TenderProject project, SysUser manager) {
        return TendererProjectSummaryResponse.builder()
                .id(project.getId())
                .projectNo(project.getProjectNo())
                .name(project.getName())
                .procurementType(project.getProcurementType())
                .tenderType(project.getTenderType())
                .status(project.getStatus())
                .evalNode(project.getEvalNode())
                .budget(project.getBudget())
                .bidOpenTime(project.getBidOpenTime())
                .regEnd(project.getRegEnd())
                .managerName(manager != null ? displayName(manager) : null)
                .createTime(project.getCreateTime())
                .build();
    }

    private TendererProjectDetailResponse toDetail(TenderProject project, SysUser manager, long registrationCount) {
        return TendererProjectDetailResponse.builder()
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
                .content(project.getContent())
                .managerName(manager != null ? displayName(manager) : null)
                .registrationCount(registrationCount)
                .build();
    }

    private String displayName(SysUser user) {
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName();
        }
        return user.getUsername();
    }
}
