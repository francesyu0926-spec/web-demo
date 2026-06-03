package com.guandian.bidding.module.manager.support;

import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ManagerProjectGuard {

    private final TenderProjectMapper projectMapper;

    public TenderProject requireOwnedProject(Long projectId) {
        TenderProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(project.getManagerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "非本项目经理，无权操作");
        }
        return project;
    }

    public void requireStatus(TenderProject project, String... allowed) {
        Set<String> set = new HashSet<>(Arrays.asList(allowed));
        if (!set.contains(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID,
                    "当前项目状态为 " + project.getStatus() + "，不允许该操作");
        }
    }
}
