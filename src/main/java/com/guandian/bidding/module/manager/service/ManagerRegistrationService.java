package com.guandian.bidding.module.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.manager.dto.*;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.module.tender.entity.BidRegistration;
import com.guandian.bidding.module.tender.entity.PaymentOrder;
import com.guandian.bidding.module.tender.entity.PaymentOrderItem;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.BidRegistrationMapper;
import com.guandian.bidding.module.tender.mapper.PaymentOrderItemMapper;
import com.guandian.bidding.module.tender.mapper.PaymentOrderMapper;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import com.guandian.bidding.security.LoginUser;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerRegistrationService {

    private final BidRegistrationMapper registrationMapper;
    private final TenderProjectMapper projectMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentOrderItemMapper paymentOrderItemMapper;
    private final ManagerProjectGuard projectGuard;

    public List<RegistrationSummaryResponse> listByProject(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        return registrationMapper.selectList(
                        new LambdaQueryWrapper<BidRegistration>()
                                .eq(BidRegistration::getProjectId, projectId)
                                .orderByDesc(BidRegistration::getCreateTime))
                .stream().map(r -> toSummary(r, project.getName()))
                .collect(Collectors.toList());
    }

    public RegistrationDetailResponse getDetail(Long id) {
        BidRegistration reg = requireAccessibleRegistration(id);
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        return RegistrationDetailResponse.builder()
                .id(reg.getId())
                .projectId(reg.getProjectId())
                .projectName(project != null ? project.getName() : null)
                .companyName(reg.getCompanyName())
                .contactName(reg.getContactName())
                .contactPhone(reg.getContactPhone())
                .applyFileId(reg.getApplyFileId())
                .auditStatus(reg.getAuditStatus())
                .auditRemark(reg.getAuditRemark())
                .regStatus(reg.getRegStatus())
                .bidStatus(reg.getBidStatus())
                .regTime(reg.getRegTime())
                .progress(buildProgress(reg))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public RegistrationDetailResponse audit(Long id, RegistrationAuditRequest req) {
        if (req.getAuditStatus() == null || (req.getAuditStatus() != 1 && req.getAuditStatus() != 2)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "auditStatus 仅支持 1(通过) 或 2(驳回)");
        }
        BidRegistration reg = registrationMapper.selectById(id);
        if (reg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        TenderProject project = projectGuard.requireOwnedProject(reg.getProjectId());
        projectGuard.requireStatus(project, "BIDDING");

        reg.setAuditStatus(req.getAuditStatus());
        reg.setAuditRemark(req.getRemark());
        if (req.getAuditStatus() == 1) {
            reg.setRegStatus("UNPAID");
        } else {
            reg.setRegStatus("REJECTED");
        }
        registrationMapper.updateById(reg);
        return getDetail(id);
    }

    public RegistrationPaymentResponse getPayment(Long id) {
        BidRegistration reg = requireAccessibleRegistration(id);
        PaymentOrder order = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getRegistrationId, reg.getId())
                        .eq(PaymentOrder::getBizType, "REGISTER")
                        .orderByDesc(PaymentOrder::getCreateTime)
                        .last("LIMIT 1"));
        if (order == null) {
            return null;
        }
        List<PaymentItemDto> items = paymentOrderItemMapper.selectList(
                        new LambdaQueryWrapper<PaymentOrderItem>().eq(PaymentOrderItem::getOrderId, order.getId()))
                .stream()
                .map(i -> PaymentItemDto.builder().feeType(i.getFeeType()).amount(i.getAmount()).build())
                .collect(Collectors.toList());
        return RegistrationPaymentResponse.builder()
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .payChannel(order.getPayChannel())
                .payTime(order.getPayTime())
                .items(items)
                .build();
    }

    private BidRegistration requireAccessibleRegistration(Long id) {
        BidRegistration reg = registrationMapper.selectById(id);
        if (reg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        LoginUser user = SecurityUtils.requireLoginUser();
        if ("MANAGER".equals(user.getActiveRole())) {
            projectGuard.requireOwnedProject(reg.getProjectId());
            return reg;
        }
        if ("BIDDER".equals(user.getActiveRole()) && user.getUserId().equals(reg.getSupplierId())) {
            return reg;
        }
        throw new BusinessException(ResultCode.FORBIDDEN);
    }

    private RegistrationSummaryResponse toSummary(BidRegistration reg, String projectName) {
        return RegistrationSummaryResponse.builder()
                .id(reg.getId())
                .projectId(reg.getProjectId())
                .projectName(projectName)
                .supplierId(reg.getSupplierId())
                .companyName(reg.getCompanyName())
                .contactName(reg.getContactName())
                .contactPhone(reg.getContactPhone())
                .auditStatus(reg.getAuditStatus())
                .regStatus(reg.getRegStatus())
                .bidStatus(reg.getBidStatus())
                .regTime(reg.getRegTime())
                .build();
    }

    private List<ProgressStepDto> buildProgress(BidRegistration reg) {
        List<ProgressStepDto> steps = new ArrayList<>();
        steps.add(ProgressStepDto.builder().step("提交报名").status("DONE")
                .time(reg.getRegTime() != null ? reg.getRegTime() : reg.getCreateTime()).build());
        String auditStatus = reg.getAuditStatus() == 0 ? "PENDING" : (reg.getAuditStatus() == 1 ? "DONE" : "REJECTED");
        steps.add(ProgressStepDto.builder().step("报名审核").status(auditStatus).time(reg.getUpdateTime()).build());
        String payStatus = "UNPAID".equals(reg.getRegStatus()) ? "PENDING"
                : ("SUCCESS".equals(reg.getRegStatus()) ? "DONE" : "WAIT");
        steps.add(ProgressStepDto.builder().step("报名缴费").status(payStatus).build());
        steps.add(ProgressStepDto.builder().step("递交投标文件")
                .status("NONE".equals(reg.getBidStatus()) ? "WAIT" : "DONE").build());
        return steps;
    }
}
