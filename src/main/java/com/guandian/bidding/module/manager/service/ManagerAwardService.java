package com.guandian.bidding.module.manager.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.evaluation.dto.FinalScoreResponse;
import com.guandian.bidding.module.evaluation.dto.ScoreSummaryResponse;
import com.guandian.bidding.module.evaluation.service.EvaluationResultService;
import com.guandian.bidding.module.manager.dto.*;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.module.tender.entity.*;
import com.guandian.bidding.module.tender.mapper.*;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerAwardService {

    private final ManagerProjectGuard projectGuard;
    private final TenderProjectMapper projectMapper;
    private final AwardMapper awardMapper;
    private final AnnouncementMapper announcementMapper;
    private final BidRegistrationMapper registrationMapper;
    private final BidDocumentMapper bidDocumentMapper;
    private final EvaluationResultService resultService;

    @Transactional(rollbackFor = Exception.class)
    public void publishAward(Long projectId, AwardPublishRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        if (!"FINISHED".equals(project.getEvalNode())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "评审未完成，不可发布中标公示");
        }

        int rank = 1;
        for (AwardPublishRequest.WinnerItem winner : req.getWinners()) {
            BidRegistration reg = registrationMapper.selectById(winner.getRegistrationId());
            if (reg == null || !projectId.equals(reg.getProjectId())) {
                throw new BusinessException(ResultCode.NOT_FOUND, "中标投标人不存在");
            }
            BigDecimal finalPrice = winner.getFinalPrice();
            if (finalPrice == null) {
                finalPrice = loadBidPrice(reg.getId());
            }
            BigDecimal finalScore = loadFinalScore(projectId, reg.getId());

            Award existing = awardMapper.selectOne(new LambdaQueryWrapper<Award>()
                    .eq(Award::getRegistrationId, reg.getId())
                    .last("LIMIT 1"));
            if (existing == null) {
                existing = new Award();
                existing.setProjectId(projectId);
                existing.setRegistrationId(reg.getId());
                existing.setCreateBy(SecurityUtils.getUserId());
            }
            existing.setRank(rank++);
            existing.setFinalScore(finalScore);
            existing.setFinalPrice(finalPrice);
            existing.setIsWinner(1);
            existing.setPublicityStart(LocalDateTime.now());
            existing.setPublicityEnd(LocalDateTime.now().plusDays(7));
            if (existing.getId() == null) {
                awardMapper.insert(existing);
            } else {
                awardMapper.updateById(existing);
            }
            reg.setBidStatus("WON");
            registrationMapper.updateById(reg);
        }

        Announcement announcement = new Announcement();
        announcement.setProjectId(projectId);
        announcement.setType(2);
        announcement.setTitle(req.getTitle());
        announcement.setContent(StringUtils.hasText(req.getContent()) ? req.getContent() : req.getTitle());
        announcement.setAttachId(req.getAttachId());
        announcement.setPublishTime(LocalDateTime.now());
        announcement.setCreateBy(SecurityUtils.getUserId());
        announcementMapper.insert(announcement);

        project.setStatus("AWARDED");
        projectMapper.updateById(project);
    }

    @Transactional(rollbackFor = Exception.class)
    public void pushAgencyFee(Long projectId, AgencyFeePushRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        if (!"AWARDED".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "项目未定标，不可推送代理费");
        }
        List<Award> winners = awardMapper.selectList(new LambdaQueryWrapper<Award>()
                .eq(Award::getProjectId, projectId)
                .eq(Award::getIsWinner, 1));
        if (winners.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "暂无中标人");
        }
        for (Award award : winners) {
            if (award.getAgencyFeePaid() != null && award.getAgencyFeePaid() == 1) {
                continue;
            }
            award.setAgencyFee(calculateAgencyFee(project, req));
            awardMapper.updateById(award);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateAgencyFee(Long projectId, AgencyFeeUpdateRequest req) {
        projectGuard.requireOwnedProject(projectId);
        Award award = awardMapper.selectOne(new LambdaQueryWrapper<Award>()
                .eq(Award::getProjectId, projectId)
                .eq(Award::getRegistrationId, req.getRegistrationId())
                .eq(Award::getIsWinner, 1)
                .last("LIMIT 1"));
        if (award == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "中标记录不存在");
        }
        if (award.getAgencyFeePaid() != null && award.getAgencyFeePaid() == 1) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "代理费已缴纳，不可修改");
        }
        award.setAgencyFee(req.getAmount());
        awardMapper.updateById(award);
    }

    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(Long projectId, NoticePublishRequest req) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        List<Award> winners = awardMapper.selectList(new LambdaQueryWrapper<Award>()
                .eq(Award::getProjectId, projectId)
                .eq(Award::getIsWinner, 1));
        if (winners.isEmpty()) {
            throw new BusinessException(ResultCode.NOT_FOUND, "暂无中标人");
        }
        for (Award award : winners) {
            if (award.getAgencyFeePaid() == null || award.getAgencyFeePaid() != 1) {
                throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "存在中标人未缴纳代理费");
            }
            award.setNoticeAttachId(req.getAttachId());
            award.setNoticePublishTime(LocalDateTime.now());
            awardMapper.updateById(award);
        }

        Announcement notice = new Announcement();
        notice.setProjectId(projectId);
        notice.setType(3);
        notice.setTitle(StringUtils.hasText(req.getTitle()) ? req.getTitle() : project.getName() + "中标通知书");
        notice.setContent(StringUtils.hasText(req.getContent()) ? req.getContent() : "中标通知书已发布");
        notice.setAttachId(req.getAttachId());
        notice.setPublishTime(LocalDateTime.now());
        notice.setCreateBy(SecurityUtils.getUserId());
        announcementMapper.insert(notice);

        project.setStatus("FINISHED");
        projectMapper.updateById(project);
    }

    @Transactional(rollbackFor = Exception.class)
    public void archiveProject(Long projectId) {
        TenderProject project = projectGuard.requireOwnedProject(projectId);
        if (!"FINISHED".equals(project.getStatus()) && !"AWARDED".equals(project.getStatus())) {
            throw new BusinessException(ResultCode.TENDER_STATUS_INVALID, "项目未结束，不可归档");
        }
        project.setStatus("ARCHIVED");
        project.setArchived(1);
        projectMapper.updateById(project);
    }

    private BigDecimal calculateAgencyFee(TenderProject project, AgencyFeePushRequest req) {
        if (req.getFeeMode() != null && req.getFeeMode() == 1) {
            if (req.getAmount() == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "固定模式需指定 amount");
            }
            return req.getAmount();
        }
        BigDecimal budget = project.getBudget() != null ? project.getBudget() : BigDecimal.ZERO;
        BigDecimal discount = req.getDiscount() != null ? req.getDiscount() : new BigDecimal("1.5");
        return budget.multiply(discount).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal loadBidPrice(Long registrationId) {
        BidDocument doc = bidDocumentMapper.selectOne(new LambdaQueryWrapper<BidDocument>()
                .eq(BidDocument::getRegistrationId, registrationId)
                .isNull(BidDocument::getWithdrawTime)
                .orderByDesc(BidDocument::getSubmitTime)
                .last("LIMIT 1"));
        return doc != null ? doc.getBidPrice() : null;
    }

    private BigDecimal loadFinalScore(Long projectId, Long registrationId) {
        ScoreSummaryResponse summary = resultService.buildScoreSummary(projectId, registrationId, false);
        if (summary.getBidders().isEmpty()) {
            FinalScoreResponse finalScore = resultService.buildFinalScore(projectId);
            return finalScore.getCandidates().stream()
                    .filter(c -> registrationId.equals(c.getRegistrationId()))
                    .map(FinalScoreResponse.CandidateItem::getTotalScore)
                    .findFirst().orElse(null);
        }
        return summary.getBidders().get(0).getTotalScore();
    }
}
