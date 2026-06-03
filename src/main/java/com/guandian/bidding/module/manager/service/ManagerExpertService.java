package com.guandian.bidding.module.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.manager.dto.ExpertAssignmentResponse;
import com.guandian.bidding.module.manager.dto.ExpertDrawRequest;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.module.tender.entity.ExpertAssignment;
import com.guandian.bidding.module.tender.entity.ExpertProfile;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.ExpertAssignmentMapper;
import com.guandian.bidding.module.tender.mapper.ExpertProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerExpertService {

    private final ExpertAssignmentMapper assignmentMapper;
    private final ExpertProfileMapper expertProfileMapper;
    private final ManagerProjectGuard projectGuard;

    public List<ExpertAssignmentResponse> listExperts(Long projectId) {
        projectGuard.requireOwnedProject(projectId);
        List<ExpertAssignment> assignments = assignmentMapper.selectList(
                new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, projectId)
                        .orderByDesc(ExpertAssignment::getCreateTime));
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, ExpertProfile> profileMap = loadProfiles(assignments.stream()
                .map(ExpertAssignment::getExpertId).collect(Collectors.toList()));
        return assignments.stream()
                .map(a -> toResponse(a, profileMap.get(a.getExpertId())))
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeExpert(Long projectId, Long assignmentId) {
        projectGuard.requireOwnedProject(projectId);
        ExpertAssignment assignment = assignmentMapper.selectById(assignmentId);
        if (assignment == null || !projectId.equals(assignment.getProjectId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        assignmentMapper.deleteById(assignmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ExpertAssignmentResponse> drawExperts(Long projectId, ExpertDrawRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        requireDrawAllowed(project);

        if (req.getDrawType() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "drawType 不能为空");
        }
        List<Long> expertUserIds;
        if (req.getDrawType() == 1) {
            if (CollectionUtils.isEmpty(req.getExpertIds())) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "邀请模式需提供 expertIds");
            }
            expertUserIds = req.getExpertIds();
        } else if (req.getDrawType() == 2) {
            int count = req.getCount() != null ? req.getCount() : 3;
            expertUserIds = randomExperts(projectId, req.getMajors(), count);
        } else {
            throw new BusinessException(ResultCode.PARAM_ERROR, "drawType 仅支持 1(邀请) 或 2(随机)");
        }

        for (Long expertUserId : expertUserIds) {
            ExpertProfile profile = expertProfileMapper.selectOne(
                    new LambdaQueryWrapper<ExpertProfile>()
                            .eq(ExpertProfile::getUserId, expertUserId)
                            .eq(ExpertProfile::getStatus, 1));
            if (profile == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "专家不存在或已停用: " + expertUserId);
            }
            Long exists = assignmentMapper.selectCount(
                    new LambdaQueryWrapper<ExpertAssignment>()
                            .eq(ExpertAssignment::getProjectId, projectId)
                            .eq(ExpertAssignment::getExpertId, expertUserId));
            if (exists > 0) {
                continue;
            }
            ExpertAssignment assignment = new ExpertAssignment();
            assignment.setProjectId(projectId);
            assignment.setExpertId(expertUserId);
            assignment.setIsLeader(0);
            assignment.setEvalPeriod(req.getEvalPeriod());
            assignment.setReportPlace(req.getReportPlace());
            assignment.setDrawType(req.getDrawType());
            assignment.setStatus("PENDING");
            assignmentMapper.insert(assignment);
        }
        return listExperts(projectId);
    }

    private void requireDrawAllowed(TenderProject project) {
        if (!"BIDDING".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID);
        }
        if (project.getRegEnd() != null && project.getRegEnd().isAfter(LocalDateTime.now())) {
            // 报名未截止也允许提前抽专家（部分场景），不强制拦截
        }
    }

    private List<Long> randomExperts(Long projectId, List<String> majors, int count) {
        Set<Long> assigned = assignmentMapper.selectList(
                        new LambdaQueryWrapper<ExpertAssignment>().eq(ExpertAssignment::getProjectId, projectId))
                .stream().map(ExpertAssignment::getExpertId).collect(Collectors.toSet());

        LambdaQueryWrapper<ExpertProfile> wrapper = new LambdaQueryWrapper<ExpertProfile>()
                .eq(ExpertProfile::getStatus, 1);
        if (!CollectionUtils.isEmpty(majors)) {
            wrapper.and(w -> {
                for (String major : majors) {
                    w.or().like(ExpertProfile::getMajor, major);
                }
            });
        }
        List<ExpertProfile> candidates = expertProfileMapper.selectList(wrapper).stream()
                .filter(p -> !assigned.contains(p.getUserId()))
                .collect(Collectors.toList());
        Collections.shuffle(candidates);
        return candidates.stream().limit(count).map(ExpertProfile::getUserId).collect(Collectors.toList());
    }

    private Map<Long, ExpertProfile> loadProfiles(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return expertProfileMapper.selectList(
                        new LambdaQueryWrapper<ExpertProfile>().in(ExpertProfile::getUserId, userIds))
                .stream().collect(Collectors.toMap(ExpertProfile::getUserId, p -> p, (a, b) -> a));
    }

    private ExpertAssignmentResponse toResponse(ExpertAssignment a, ExpertProfile profile) {
        return ExpertAssignmentResponse.builder()
                .id(a.getId())
                .expertId(a.getExpertId())
                .expertNo(profile != null ? profile.getExpertNo() : null)
                .major(profile != null ? profile.getMajor() : null)
                .org(profile != null ? profile.getOrg() : null)
                .title(profile != null ? profile.getTitle() : null)
                .isLeader(a.getIsLeader())
                .evalPeriod(a.getEvalPeriod())
                .reportPlace(a.getReportPlace())
                .drawType(a.getDrawType())
                .status(a.getStatus())
                .signTime(a.getSignTime())
                .build();
    }
}
