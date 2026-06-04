package com.guandian.bidding.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.content.dto.ComplaintCreateRequest;
import com.guandian.bidding.module.content.dto.ComplaintResponse;
import com.guandian.bidding.module.content.entity.Complaint;
import com.guandian.bidding.module.content.mapper.ComplaintMapper;
import com.guandian.bidding.module.audit.enums.OperationAction;
import com.guandian.bidding.module.audit.enums.OperationModule;
import com.guandian.bidding.module.audit.service.OperationLogService;
import com.guandian.bidding.module.notify.enums.NotificationType;
import com.guandian.bidding.module.notify.service.NotificationService;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final ComplaintMapper complaintMapper;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    public PageResult<ComplaintResponse> list(String keyword, LocalDateTime startTime,
                                              LocalDateTime endTime, long pageNum, long pageSize) {
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<Complaint>()
                .eq(Complaint::getUserId, userId)
                .like(StringUtils.hasText(keyword), Complaint::getTitle, keyword)
                .ge(startTime != null, Complaint::getCreateTime, startTime)
                .le(endTime != null, Complaint::getCreateTime, endTime)
                .orderByDesc(Complaint::getCreateTime);

        Page<Complaint> page = complaintMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ComplaintResponse> list = page.getRecords().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public ComplaintResponse create(ComplaintCreateRequest req) {
        Long userId = SecurityUtils.getUserId();
        Complaint complaint = new Complaint();
        complaint.setUserId(userId);
        complaint.setProjectId(req.getProjectId());
        complaint.setCategory(req.getCategory().toUpperCase());
        complaint.setSubType(req.getSubType());
        complaint.setTitle(req.getTitle());
        complaint.setContent(req.getContent());
        complaint.setAttachId(req.getAttachId());
        complaint.setStatus(0);
        complaintMapper.insert(complaint);
        return toResponse(complaint);
    }

    public ComplaintResponse getDetail(Long id) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null || !SecurityUtils.getUserId().equals(complaint.getUserId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return toResponse(complaint);
    }

    public PageResult<ComplaintResponse> listForManager(String keyword, Integer status,
                                                        long pageNum, long pageSize) {
        LambdaQueryWrapper<Complaint> wrapper = new LambdaQueryWrapper<Complaint>()
                .like(StringUtils.hasText(keyword), Complaint::getTitle, keyword)
                .eq(status != null, Complaint::getStatus, status)
                .orderByDesc(Complaint::getCreateTime);
        Page<Complaint> page = complaintMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ComplaintResponse> list = page.getRecords().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public ComplaintResponse feedback(Long id, String reply) {
        Complaint complaint = complaintMapper.selectById(id);
        if (complaint == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        complaint.setReply(reply);
        complaint.setStatus(2);
        complaint.setHandler(SecurityUtils.getUserId());
        complaint.setHandleTime(LocalDateTime.now());
        complaintMapper.updateById(complaint);
        if (complaint.getUserId() != null) {
            notificationService.send(complaint.getUserId(), NotificationType.SYSTEM,
                    "投诉建议已回复",
                    "您提交的「" + complaint.getTitle() + "」已收到处理反馈，请查看详情。",
                    complaint.getId());
        }
        operationLogService.record(OperationModule.COMPLAINT, OperationAction.FEEDBACK, complaint.getId(),
                "title=" + complaint.getTitle());
        return toResponse(complaint);
    }

    private ComplaintResponse toResponse(Complaint complaint) {
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .projectId(complaint.getProjectId())
                .category(complaint.getCategory())
                .subType(complaint.getSubType())
                .title(complaint.getTitle())
                .content(complaint.getContent())
                .attachId(complaint.getAttachId())
                .status(complaint.getStatus())
                .reply(complaint.getReply())
                .handler(complaint.getHandler())
                .handleTime(complaint.getHandleTime())
                .createTime(complaint.getCreateTime())
                .build();
    }
}
