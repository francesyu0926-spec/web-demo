package com.guandian.bidding.module.bidder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.bidder.dto.*;
import com.guandian.bidding.module.manager.dto.ProgressStepDto;
import com.guandian.bidding.module.manager.dto.RegistrationDetailResponse;
import com.guandian.bidding.module.tender.entity.*;
import com.guandian.bidding.module.tender.mapper.*;
import com.guandian.bidding.module.user.entity.SupplierProfile;
import com.guandian.bidding.module.user.entity.UserPreference;
import com.guandian.bidding.module.user.mapper.SupplierProfileMapper;
import com.guandian.bidding.module.user.mapper.UserPreferenceMapper;
import com.guandian.bidding.module.tender.dto.AnnouncementItemResponse;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BidderService {

    private final BidRegistrationMapper registrationMapper;
    private final TenderProjectMapper projectMapper;
    private final BidDocumentMapper bidDocumentMapper;
    private final PaymentOrderMapper paymentOrderMapper;
    private final PaymentOrderItemMapper paymentOrderItemMapper;
    private final AwardMapper awardMapper;
    private final ExpertAssignmentMapper expertAssignmentMapper;
    private final SupplierProfileMapper supplierProfileMapper;
    private final UserPreferenceMapper preferenceMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public RegistrationDetailResponse submitRegistration(Long projectId, RegistrationSubmitRequest req) {
        requireBidder();
        Long userId = SecurityUtils.getUserId();

        TenderProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!"BIDDING".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID);
        }
        validateRegistrationWindow(project);

        Long exists = registrationMapper.selectCount(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getProjectId, projectId)
                        .eq(BidRegistration::getSupplierId, userId));
        if (exists > 0) {
            throw new BusinessException(ResultCode.REGISTRATION_DUPLICATE);
        }

        String companyName = req.getCompanyName();
        if (!StringUtils.hasText(companyName)) {
            SupplierProfile profile = supplierProfileMapper.selectOne(
                    new LambdaQueryWrapper<SupplierProfile>().eq(SupplierProfile::getUserId, userId));
            if (profile != null) {
                companyName = profile.getCompanyName();
            }
        }
        if (!StringUtils.hasText(companyName)) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写企业名称或先完善企业资料");
        }

        BidRegistration reg = new BidRegistration();
        reg.setProjectId(projectId);
        reg.setSupplierId(userId);
        reg.setCompanyName(companyName);
        reg.setContactName(req.getContactName());
        reg.setContactPhone(req.getContactPhone());
        reg.setApplyFileId(req.getApplyFileId());
        reg.setAuditStatus(0);
        reg.setRegStatus("PENDING");
        reg.setBidStatus("NONE");
        reg.setRegTime(LocalDateTime.now());
        registrationMapper.insert(reg);

        return toRegistrationDetail(reg, project);
    }

    public PageResult<RegistrationDetailResponse> listMyRegistrations(String regStatus, String type,
                                                                       String keyword, long pageNum, long pageSize) {
        requireBidder();
        Long userId = SecurityUtils.getUserId();

        LambdaQueryWrapper<BidRegistration> wrapper = new LambdaQueryWrapper<BidRegistration>()
                .eq(BidRegistration::getSupplierId, userId)
                .eq(StringUtils.hasText(regStatus), BidRegistration::getRegStatus, regStatus)
                .orderByDesc(BidRegistration::getCreateTime);

        Page<BidRegistration> page = registrationMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, TenderProject> projectMap = loadProjects(page.getRecords());

        List<RegistrationDetailResponse> list = page.getRecords().stream()
                .map(reg -> {
                    TenderProject project = projectMap.get(reg.getProjectId());
                    if (StringUtils.hasText(type) && project != null
                            && !type.equals(project.getTenderType())) {
                        return null;
                    }
                    if (StringUtils.hasText(keyword) && project != null
                            && !project.getName().contains(keyword)) {
                        return null;
                    }
                    return toRegistrationDetail(reg, project);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public PageResult<BidderProjectResponse> listMyProjects(String projectStatus, long pageNum, long pageSize) {
        requireBidder();
        Long userId = SecurityUtils.getUserId();
        List<BidRegistration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getSupplierId, userId)
                        .ne(BidRegistration::getRegStatus, "CANCELLED")
                        .orderByDesc(BidRegistration::getCreateTime));
        Map<Long, TenderProject> projectMap = loadProjects(registrations);

        List<BidderProjectResponse> all = registrations.stream()
                .map(reg -> {
                    TenderProject p = projectMap.get(reg.getProjectId());
                    if (p == null) {
                        return null;
                    }
                    if (StringUtils.hasText(projectStatus) && !projectStatus.equals(p.getStatus())) {
                        return null;
                    }
                    return BidderProjectResponse.builder()
                            .projectId(p.getId())
                            .projectNo(p.getProjectNo())
                            .projectName(p.getName())
                            .tenderType(p.getTenderType())
                            .projectStatus(p.getStatus())
                            .evalNode(p.getEvalNode())
                            .registrationId(reg.getId())
                            .regStatus(reg.getRegStatus())
                            .bidStatus(reg.getBidStatus())
                            .bidOpenTime(p.getBidOpenTime())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int from = (int) Math.min((pageNum - 1) * pageSize, all.size());
        int to = (int) Math.min(from + pageSize, all.size());
        return PageResult.of(all.subList(from, to), all.size(), pageNum, pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public RegistrationDetailResponse cancelRegistration(Long id) {
        BidRegistration reg = requireOwnedRegistration(id);
        if (!"PENDING".equals(reg.getRegStatus()) && !"UNPAID".equals(reg.getRegStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "当前状态不可取消报名");
        }
        reg.setRegStatus("CANCELLED");
        registrationMapper.updateById(reg);
        return toRegistrationDetail(reg, projectMapper.selectById(reg.getProjectId()));
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentOrderResponse payRegistration(Long id, PaymentRequest req) {
        BidRegistration reg = requireOwnedRegistration(id);
        if (!"UNPAID".equals(reg.getRegStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "当前状态不可缴费");
        }
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        PaymentOrder existing = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getRegistrationId, id)
                        .eq(PaymentOrder::getBizType, "REGISTER")
                        .eq(PaymentOrder::getStatus, 0)
                        .last("LIMIT 1"));
        if (existing != null) {
            return completePayment(existing, reg, req.getPayChannel());
        }

        BigDecimal fileFee = defaultFee(project.getFileFee(), new BigDecimal("400"));
        BigDecimal platformFee = defaultFee(project.getPlatformFee(), new BigDecimal("200"));
        BigDecimal total = fileFee.add(platformFee);

        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(generateOrderNo());
        order.setProjectId(reg.getProjectId());
        order.setRegistrationId(reg.getId());
        order.setPayerId(SecurityUtils.getUserId());
        order.setBizType("REGISTER");
        order.setTotalAmount(total);
        order.setPayChannel(req.getPayChannel());
        order.setStatus(0);
        paymentOrderMapper.insert(order);

        insertOrderItem(order.getId(), "FILE", fileFee);
        insertOrderItem(order.getId(), "PLATFORM", platformFee);

        return completePayment(order, reg, req.getPayChannel());
    }

    public PaymentOrderResponse getBill(Long id) {
        BidRegistration reg = requireOwnedRegistration(id);
        if (!"SUCCESS".equals(reg.getRegStatus())) {
            throw new BusinessException(ResultCode.REGISTRATION_UNPAID);
        }
        PaymentOrder order = paymentOrderMapper.selectOne(
                new LambdaQueryWrapper<PaymentOrder>()
                        .eq(PaymentOrder::getRegistrationId, id)
                        .eq(PaymentOrder::getBizType, "REGISTER")
                        .eq(PaymentOrder::getStatus, 1)
                        .orderByDesc(PaymentOrder::getCreateTime)
                        .last("LIMIT 1"));
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "未找到缴费记录");
        }
        return toPaymentResponse(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public BidDocumentResponse submitBidDocument(Long registrationId, BidDocumentSubmitRequest req) {
        BidRegistration reg = requireOwnedRegistration(registrationId);
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        if (project == null || !"BIDDING".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID);
        }
        if (!"SUCCESS".equals(reg.getRegStatus())) {
            throw new BusinessException(ResultCode.REGISTRATION_UNPAID, "请先完成报名缴费");
        }

        BidDocument active = findActiveBidDocument(registrationId);
        if (active != null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "已有有效投标文件，请先撤回后再递交");
        }

        BidDocument doc = new BidDocument();
        doc.setRegistrationId(registrationId);
        doc.setAttachId(req.getAttachId());
        doc.setBidPrice(req.getBidPrice());
        doc.setDuration(req.getDuration());
        doc.setEncrypted(0);
        doc.setDecryptStatus(0);
        doc.setSubmitTime(LocalDateTime.now());
        bidDocumentMapper.insert(doc);

        reg.setBidStatus("BIDDING");
        registrationMapper.updateById(reg);
        return toBidDocumentResponse(doc);
    }

    @Transactional(rollbackFor = Exception.class)
    public BidDocumentResponse encryptBidDocument(Long docId, BidDocumentPwdRequest req) {
        BidDocument doc = requireOwnedBidDocument(docId);
        if (doc.getEncrypted() != null && doc.getEncrypted() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件已加密");
        }
        doc.setEncryptPwd(passwordEncoder.encode(req.getPwd()));
        doc.setEncrypted(1);
        bidDocumentMapper.updateById(doc);
        return toBidDocumentResponse(doc);
    }

    @Transactional(rollbackFor = Exception.class)
    public BidDocumentResponse withdrawBidDocument(Long docId) {
        BidDocument doc = requireOwnedBidDocument(docId);
        BidRegistration reg = registrationMapper.selectById(doc.getRegistrationId());
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        if (project == null || !"BIDDING".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "开标后不可撤回");
        }
        doc.setWithdrawTime(LocalDateTime.now());
        bidDocumentMapper.deleteById(docId);
        reg.setBidStatus("NONE");
        registrationMapper.updateById(reg);
        return toBidDocumentResponse(doc);
    }

    @Transactional(rollbackFor = Exception.class)
    public BidDocumentResponse decryptBidDocument(Long docId, BidDocumentPwdRequest req) {
        BidDocument doc = requireOwnedBidDocument(docId);
        BidRegistration reg = registrationMapper.selectById(doc.getRegistrationId());
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        if (project == null || !"OPENING".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "当前项目未处于开标中");
        }
        if (!hasSignedExpert(project.getId())) {
            throw new BusinessException(ResultCode.EXPERT_NOT_SIGNED);
        }
        if (!StringUtils.hasText(doc.getEncryptPwd())
                || !passwordEncoder.matches(req.getPwd(), doc.getEncryptPwd())) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "解密密码错误");
        }
        doc.setDecryptStatus(1);
        doc.setDecryptTime(LocalDateTime.now());
        doc.setSignImgId(req.getSignImgId());
        bidDocumentMapper.updateById(doc);
        return toBidDocumentResponse(doc);
    }

    public BidDetailResponse getBidDetail(Long registrationId) {
        BidRegistration reg = requireOwnedRegistration(registrationId);
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        BidDocument doc = findActiveBidDocument(registrationId);
        Award award = awardMapper.selectOne(
                new LambdaQueryWrapper<Award>()
                        .eq(Award::getRegistrationId, registrationId)
                        .eq(Award::getIsWinner, 1)
                        .last("LIMIT 1"));

        return BidDetailResponse.builder()
                .registrationId(reg.getId())
                .projectId(reg.getProjectId())
                .projectName(project != null ? project.getName() : null)
                .projectStatus(project != null ? project.getStatus() : null)
                .evalNode(project != null ? project.getEvalNode() : null)
                .regStatus(reg.getRegStatus())
                .bidStatus(reg.getBidStatus())
                .bidDocument(doc != null ? toBidDocumentResponse(doc) : null)
                .agencyFee(award != null ? award.getAgencyFee() : null)
                .agencyFeePaid(award != null ? award.getAgencyFeePaid() : null)
                .progress(buildProgress(reg))
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentOrderResponse payAgencyFee(Long registrationId, PaymentRequest req) {
        BidRegistration reg = requireOwnedRegistration(registrationId);
        Award award = awardMapper.selectOne(
                new LambdaQueryWrapper<Award>()
                        .eq(Award::getRegistrationId, registrationId)
                        .eq(Award::getIsWinner, 1));
        if (award == null) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "未中标或未推送代理费");
        }
        if (award.getAgencyFeePaid() != null && award.getAgencyFeePaid() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "代理费已缴纳");
        }
        BigDecimal amount = award.getAgencyFee() != null ? award.getAgencyFee() : BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "代理费金额无效");
        }

        PaymentOrder order = new PaymentOrder();
        order.setOrderNo(generateOrderNo());
        order.setProjectId(reg.getProjectId());
        order.setRegistrationId(registrationId);
        order.setPayerId(SecurityUtils.getUserId());
        order.setBizType("AGENCY");
        order.setTotalAmount(amount);
        order.setPayChannel(req.getPayChannel());
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        paymentOrderMapper.insert(order);
        insertOrderItem(order.getId(), "AGENCY", amount);

        award.setAgencyFeePaid(1);
        awardMapper.updateById(award);
        return toPaymentResponse(order);
    }

    private PaymentOrderResponse completePayment(PaymentOrder order, BidRegistration reg, String payChannel) {
        order.setStatus(1);
        order.setPayChannel(payChannel);
        order.setPayTime(LocalDateTime.now());
        paymentOrderMapper.updateById(order);

        reg.setRegStatus("SUCCESS");
        registrationMapper.updateById(reg);
        return toPaymentResponse(order);
    }

    private void insertOrderItem(Long orderId, String feeType, BigDecimal amount) {
        PaymentOrderItem item = new PaymentOrderItem();
        item.setOrderId(orderId);
        item.setFeeType(feeType);
        item.setAmount(amount);
        paymentOrderItemMapper.insert(item);
    }

    private PaymentOrderResponse toPaymentResponse(PaymentOrder order) {
        List<PaymentOrderItemResponse> items = paymentOrderItemMapper.selectList(
                        new LambdaQueryWrapper<PaymentOrderItem>().eq(PaymentOrderItem::getOrderId, order.getId()))
                .stream()
                .map(i -> PaymentOrderItemResponse.builder().feeType(i.getFeeType()).amount(i.getAmount()).build())
                .collect(Collectors.toList());
        return PaymentOrderResponse.builder()
                .orderNo(order.getOrderNo())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .payChannel(order.getPayChannel())
                .items(items)
                .build();
    }

    private BidDocument findActiveBidDocument(Long registrationId) {
        return bidDocumentMapper.selectOne(
                new LambdaQueryWrapper<BidDocument>()
                        .eq(BidDocument::getRegistrationId, registrationId)
                        .isNull(BidDocument::getWithdrawTime)
                        .orderByDesc(BidDocument::getSubmitTime)
                        .last("LIMIT 1"));
    }

    private BidDocument requireOwnedBidDocument(Long docId) {
        requireBidder();
        BidDocument doc = bidDocumentMapper.selectById(docId);
        if (doc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        requireOwnedRegistration(doc.getRegistrationId());
        return doc;
    }

    private BidRegistration requireOwnedRegistration(Long id) {
        requireBidder();
        BidRegistration reg = registrationMapper.selectById(id);
        if (reg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!SecurityUtils.getUserId().equals(reg.getSupplierId())) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return reg;
    }

    private void requireBidder() {
        if (!"BIDDER".equals(SecurityUtils.requireLoginUser().getActiveRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "请切换为投标人身份");
        }
    }

    private void validateRegistrationWindow(TenderProject project) {
        LocalDateTime now = LocalDateTime.now();
        if (project.getRegStart() != null && now.isBefore(project.getRegStart())) {
            throw new BusinessException(ResultCode.REGISTRATION_CLOSED, "报名尚未开始");
        }
        if (project.getRegEnd() != null && now.isAfter(project.getRegEnd())) {
            throw new BusinessException(ResultCode.REGISTRATION_CLOSED, "报名已截止");
        }
    }

    private boolean hasSignedExpert(Long projectId) {
        return expertAssignmentMapper.selectCount(
                new LambdaQueryWrapper<ExpertAssignment>()
                        .eq(ExpertAssignment::getProjectId, projectId)
                        .eq(ExpertAssignment::getStatus, "SIGNED")) > 0;
    }

    private Map<Long, TenderProject> loadProjects(List<BidRegistration> registrations) {
        if (registrations.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = registrations.stream().map(BidRegistration::getProjectId).distinct().collect(Collectors.toList());
        return projectMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(TenderProject::getId, Function.identity()));
    }

    private RegistrationDetailResponse toRegistrationDetail(BidRegistration reg, TenderProject project) {
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

    private BidDocumentResponse toBidDocumentResponse(BidDocument doc) {
        return BidDocumentResponse.builder()
                .id(doc.getId())
                .attachId(doc.getAttachId())
                .bidPrice(doc.getBidPrice())
                .duration(doc.getDuration())
                .encrypted(doc.getEncrypted())
                .decryptStatus(doc.getDecryptStatus())
                .submitTime(doc.getSubmitTime())
                .withdrawTime(doc.getWithdrawTime())
                .build();
    }

    private List<ProgressStepDto> buildProgress(BidRegistration reg) {
        List<ProgressStepDto> steps = new ArrayList<>();
        steps.add(ProgressStepDto.builder().step("提交报名").status("DONE")
                .time(reg.getRegTime() != null ? reg.getRegTime() : reg.getCreateTime()).build());
        String audit = reg.getAuditStatus() == 0 ? "PENDING" : (reg.getAuditStatus() == 1 ? "DONE" : "REJECTED");
        steps.add(ProgressStepDto.builder().step("报名审核").status(audit).time(reg.getUpdateTime()).build());
        String pay = "UNPAID".equals(reg.getRegStatus()) ? "PENDING"
                : ("SUCCESS".equals(reg.getRegStatus()) ? "DONE" : "WAIT");
        steps.add(ProgressStepDto.builder().step("报名缴费").status(pay).build());
        steps.add(ProgressStepDto.builder().step("递交投标文件")
                .status("NONE".equals(reg.getBidStatus()) ? "WAIT" : "DONE").build());
        return steps;
    }

    private BigDecimal defaultFee(BigDecimal fee, BigDecimal defaultVal) {
        return fee != null ? fee : defaultVal;
    }

    private String generateOrderNo() {
        return "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + ThreadLocalRandom.current().nextInt(100000, 999999);
    }

    public PageResult<MyAwardItemResponse> listMyAwards(String keyword, long pageNum, long pageSize) {
        requireBidder();
        Long userId = SecurityUtils.getUserId();
        LambdaQueryWrapper<BidRegistration> regWrapper = new LambdaQueryWrapper<BidRegistration>()
                .eq(BidRegistration::getSupplierId, userId);
        List<BidRegistration> regs = registrationMapper.selectList(regWrapper);
        if (regs.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0, pageNum, pageSize);
        }
        Map<Long, BidRegistration> regMap = regs.stream()
                .collect(Collectors.toMap(BidRegistration::getId, Function.identity(), (a, b) -> a));
        List<Long> regIds = regs.stream().map(BidRegistration::getId).collect(Collectors.toList());

        LambdaQueryWrapper<Award> awardWrapper = new LambdaQueryWrapper<Award>()
                .in(Award::getRegistrationId, regIds)
                .eq(Award::getIsWinner, 1)
                .orderByDesc(Award::getPublicityStart)
                .orderByDesc(Award::getId);
        List<Award> awards = awardMapper.selectList(awardWrapper);
        if (awards.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0, pageNum, pageSize);
        }

        List<Long> projectIds = awards.stream().map(Award::getProjectId).distinct().collect(Collectors.toList());
        Map<Long, TenderProject> projectMap = projectMapper.selectBatchIds(projectIds).stream()
                .collect(Collectors.toMap(TenderProject::getId, Function.identity()));

        List<MyAwardItemResponse> list = awards.stream().map(a -> {
            TenderProject p = projectMap.get(a.getProjectId());
            BidRegistration reg = regMap.get(a.getRegistrationId());
            if (p == null || reg == null) {
                return null;
            }
            if (StringUtils.hasText(keyword)
                    && !p.getName().contains(keyword)
                    && !p.getProjectNo().contains(keyword)) {
                return null;
            }
            return MyAwardItemResponse.builder()
                    .projectId(p.getId())
                    .projectNo(p.getProjectNo())
                    .projectName(p.getName())
                    .registrationId(reg.getId())
                    .rank(a.getRank())
                    .finalPrice(a.getFinalPrice())
                    .publicityStart(a.getPublicityStart())
                    .noticePublishTime(a.getNoticePublishTime())
                    .build();
        }).filter(Objects::nonNull).collect(Collectors.toList());

        int from = (int) Math.min((pageNum - 1) * pageSize, list.size());
        int to = (int) Math.min(from + pageSize, list.size());
        return PageResult.of(list.subList(from, to), list.size(), pageNum, pageSize);
    }

    public PageResult<AnnouncementItemResponse> listOpportunities(String keyword, String time,
                                                                  long pageNum, long pageSize) {
        requireBidder();
        Long userId = SecurityUtils.getUserId();
        UserPreference pref = preferenceMapper.selectOne(new LambdaQueryWrapper<UserPreference>()
                .eq(UserPreference::getUserId, userId).last("LIMIT 1"));

        LocalDateTime timeStart = parseOpportunityTimeStart(time);
        LambdaQueryWrapper<TenderProject> wrapper = new LambdaQueryWrapper<TenderProject>()
                .eq(TenderProject::getStatus, "BIDDING")
                .like(StringUtils.hasText(keyword), TenderProject::getName, keyword)
                .ge(timeStart != null, TenderProject::getBidOpenTime, timeStart)
                .orderByDesc(TenderProject::getBidOpenTime);

        if (pref != null) {
            if (StringUtils.hasText(pref.getRegions())) {
                List<String> regions = splitCsv(pref.getRegions());
                if (!regions.isEmpty()) {
                    wrapper.in(TenderProject::getRegion, regions);
                }
            }
            if (StringUtils.hasText(pref.getTypes())) {
                List<String> types = splitCsv(pref.getTypes());
                if (!types.isEmpty()) {
                    wrapper.in(TenderProject::getTenderType, types);
                }
            }
            if (StringUtils.hasText(pref.getIndustries())) {
                List<String> industries = splitCsv(pref.getIndustries());
                if (!industries.isEmpty()) {
                    wrapper.in(TenderProject::getIndustry, industries);
                }
            }
        }

        Page<TenderProject> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AnnouncementItemResponse> list = page.getRecords().stream()
                .map(p -> AnnouncementItemResponse.builder()
                        .projectId(p.getId())
                        .projectNo(p.getProjectNo())
                        .projectName(p.getName())
                        .procurementType(p.getProcurementType())
                        .tenderType(p.getTenderType())
                        .region(p.getRegion())
                        .budget(p.getBudget())
                        .publishTime(p.getRegStart())
                        .bidOpenTime(p.getBidOpenTime())
                        .regEnd(p.getRegEnd())
                        .build())
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    private LocalDateTime parseOpportunityTimeStart(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        switch (time) {
            case "week":
                return LocalDateTime.now().minusDays(7);
            case "month":
                return LocalDateTime.now().minusDays(30);
            default:
                return null;
        }
    }

    private List<String> splitCsv(String csv) {
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
