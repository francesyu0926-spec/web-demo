package com.guandian.bidding.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.audit.enums.OperationAction;
import com.guandian.bidding.module.audit.enums.OperationModule;
import com.guandian.bidding.module.audit.service.OperationLogService;
import com.guandian.bidding.module.notify.enums.NotificationType;
import com.guandian.bidding.module.notify.service.NotificationService;
import com.guandian.bidding.module.tender.entity.*;
import com.guandian.bidding.module.tender.mapper.*;
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
public class NegotiationService {

    private final NegotiationMapper negotiationMapper;
    private final SecondRoundQuoteMapper secondRoundQuoteMapper;
    private final ExpertAssignmentMapper assignmentMapper;
    private final BidRegistrationMapper registrationMapper;
    private final TenderProjectMapper projectMapper;
    private final NotificationService notificationService;
    private final OperationLogService operationLogService;

    @Transactional(rollbackFor = Exception.class)
    public NegotiationItemResponse createNegotiation(NegotiationCreateRequest req) {
        Long expertId = requireExpert();
        BidRegistration reg = requireRegistration(req.getRegistrationId());
        requireLeader(reg.getProjectId(), expertId);
        requireNegotiationProject(reg.getProjectId());

        Negotiation n = new Negotiation();
        n.setProjectId(reg.getProjectId());
        n.setRegistrationId(reg.getId());
        n.setInitiatorId(expertId);
        n.setContent(req.getContent());
        n.setAttachId(req.getAttachId());
        n.setStatus(0);
        n.setCreateBy(expertId);
        negotiationMapper.insert(n);

        updateEvalNode(reg.getProjectId(), "NEGOTIATING");
        notifyNegotiation(reg, projectMapper.selectById(reg.getProjectId()), n.getId());
        operationLogService.recordProject(OperationModule.EVALUATION, OperationAction.NEGOTIATION,
                reg.getProjectId(), "negotiationId=" + n.getId() + ", registrationId=" + reg.getId());
        return toNegotiationItem(n);
    }

    private void notifyNegotiation(BidRegistration reg, TenderProject project, Long negotiationId) {
        String projectName = project != null ? project.getName() : "项目";
        notificationService.send(reg.getSupplierId(), NotificationType.NEGOTIATION,
                "谈判/磋商邀请",
                "「" + projectName + "」专家组长向您发起了谈判/磋商要求，请及时回复。",
                negotiationId);
    }

    public List<NegotiationItemResponse> listNegotiations(Long registrationId) {
        requireOwnedRegistration(registrationId);
        return negotiationMapper.selectList(new LambdaQueryWrapper<Negotiation>()
                        .eq(Negotiation::getRegistrationId, registrationId)
                        .orderByDesc(Negotiation::getCreateTime))
                .stream().map(this::toNegotiationItem).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public NegotiationItemResponse replyNegotiation(Long id, NegotiationReplyRequest req) {
        BidRegistration reg = requireOwnedRegistrationByNegotiation(id);
        Negotiation n = negotiationMapper.selectById(id);
        if (n == null || !reg.getId().equals(n.getRegistrationId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (n.getStatus() != null && n.getStatus() == 1) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "已回复，不可重复回复");
        }
        n.setReplyContent(req.getContent());
        n.setReplyAttachId(req.getAttachId());
        n.setReplyTime(LocalDateTime.now());
        n.setStatus(1);
        negotiationMapper.updateById(n);
        updateEvalNode(n.getProjectId(), "REVIEWING");
        return toNegotiationItem(n);
    }

    @Transactional(rollbackFor = Exception.class)
    public SecondQuoteItemResponse createSecondQuote(SecondQuoteCreateRequest req) {
        Long expertId = requireExpert();
        BidRegistration reg = requireRegistration(req.getRegistrationId());
        requireLeader(reg.getProjectId(), expertId);
        requireNegotiationProject(reg.getProjectId());

        SecondRoundQuote q = new SecondRoundQuote();
        q.setProjectId(reg.getProjectId());
        q.setRegistrationId(reg.getId());
        q.setInitiatorId(expertId);
        q.setContent(req.getContent());
        q.setStatus(0);
        q.setCreateBy(expertId);
        secondRoundQuoteMapper.insert(q);

        updateEvalNode(reg.getProjectId(), "SECOND_QUOTE");
        notifySecondQuote(reg, projectMapper.selectById(reg.getProjectId()), q.getId());
        operationLogService.recordProject(OperationModule.EVALUATION, OperationAction.SECOND_QUOTE,
                reg.getProjectId(), "quoteId=" + q.getId() + ", registrationId=" + reg.getId());
        return toSecondQuoteItem(q);
    }

    private void notifySecondQuote(BidRegistration reg, TenderProject project, Long quoteId) {
        String projectName = project != null ? project.getName() : "项目";
        notificationService.send(reg.getSupplierId(), NotificationType.NEGOTIATION,
                "二轮报价邀请",
                "「" + projectName + "」专家组长向您发起了二轮报价要求，请及时回复。",
                quoteId);
    }

    public List<SecondQuoteItemResponse> listSecondQuotes(Long registrationId) {
        requireOwnedRegistration(registrationId);
        return secondRoundQuoteMapper.selectList(new LambdaQueryWrapper<SecondRoundQuote>()
                        .eq(SecondRoundQuote::getRegistrationId, registrationId)
                        .orderByDesc(SecondRoundQuote::getCreateTime))
                .stream().map(this::toSecondQuoteItem).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public SecondQuoteItemResponse replySecondQuote(Long id, SecondQuoteReplyRequest req) {
        BidRegistration reg = requireOwnedRegistrationByQuote(id);
        SecondRoundQuote q = secondRoundQuoteMapper.selectById(id);
        if (q == null || !reg.getId().equals(q.getRegistrationId())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (q.getStatus() != null && q.getStatus() == 1) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "已回复，不可重复回复");
        }
        if (req.getPrice() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "price 不能为空");
        }
        q.setReplyPrice(req.getPrice());
        q.setReplyDuration(req.getDuration());
        q.setReplyAttachId(req.getAttachId());
        q.setReplyTime(LocalDateTime.now());
        q.setStatus(1);
        secondRoundQuoteMapper.updateById(q);
        updateEvalNode(q.getProjectId(), "REVIEWING");
        return toSecondQuoteItem(q);
    }

    private BidRegistration requireRegistration(Long registrationId) {
        BidRegistration reg = registrationMapper.selectById(registrationId);
        if (reg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "报名记录不存在");
        }
        return reg;
    }

    private BidRegistration requireOwnedRegistration(Long registrationId) {
        BidRegistration reg = requireRegistration(registrationId);
        if (!SecurityUtils.getUserId().equals(reg.getSupplierId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return reg;
    }

    private BidRegistration requireOwnedRegistrationByNegotiation(Long negotiationId) {
        Negotiation n = negotiationMapper.selectById(negotiationId);
        if (n == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return requireOwnedRegistration(n.getRegistrationId());
    }

    private BidRegistration requireOwnedRegistrationByQuote(Long quoteId) {
        SecondRoundQuote q = secondRoundQuoteMapper.selectById(quoteId);
        if (q == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return requireOwnedRegistration(q.getRegistrationId());
    }

    private Long requireExpert() {
        if (!"EXPERT".equals(SecurityUtils.requireLoginUser().getActiveRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请切换为专家身份");
        }
        return SecurityUtils.getUserId();
    }

    private void requireLeader(Long projectId, Long expertId) {
        ExpertAssignment assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getProjectId, projectId)
                .eq(ExpertAssignment::getExpertId, expertId)
                .last("LIMIT 1"));
        if (assignment == null || !"SIGNED".equals(assignment.getStatus())) {
            throw new BusinessException(ResultCode.EXPERT_NOT_SIGNED);
        }
        if (assignment.getIsLeader() == null || assignment.getIsLeader() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅专家组长可发起");
        }
    }

    private void requireNegotiationProject(Long projectId) {
        TenderProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        List<String> allowedTypes = Arrays.asList("NEGOTIATION", "CONSULTATION");
        if (!allowedTypes.contains(project.getProcurementType())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "仅竞谈/竞磋项目支持该操作");
        }
        if (!"REVIEWING".equals(project.getEvalNode())
                && !"NEGOTIATING".equals(project.getEvalNode())
                && !"SECOND_QUOTE".equals(project.getEvalNode())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "当前评审节点不允许");
        }
    }

    private void updateEvalNode(Long projectId, String node) {
        TenderProject project = projectMapper.selectById(projectId);
        if (project != null) {
            project.setEvalNode(node);
            projectMapper.updateById(project);
        }
    }

    private NegotiationItemResponse toNegotiationItem(Negotiation n) {
        return NegotiationItemResponse.builder()
                .id(n.getId())
                .projectId(n.getProjectId())
                .registrationId(n.getRegistrationId())
                .initiatorId(n.getInitiatorId())
                .content(n.getContent())
                .attachId(n.getAttachId())
                .status(n.getStatus())
                .replyContent(n.getReplyContent())
                .replyAttachId(n.getReplyAttachId())
                .replyTime(n.getReplyTime())
                .createTime(n.getCreateTime())
                .build();
    }

    private SecondQuoteItemResponse toSecondQuoteItem(SecondRoundQuote q) {
        return SecondQuoteItemResponse.builder()
                .id(q.getId())
                .projectId(q.getProjectId())
                .registrationId(q.getRegistrationId())
                .initiatorId(q.getInitiatorId())
                .content(q.getContent())
                .status(q.getStatus())
                .replyPrice(q.getReplyPrice())
                .replyDuration(q.getReplyDuration())
                .replyAttachId(q.getReplyAttachId())
                .replyTime(q.getReplyTime())
                .createTime(q.getCreateTime())
                .build();
    }
}
