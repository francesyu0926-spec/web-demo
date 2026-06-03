package com.guandian.bidding.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.evaluation.dto.*;
import com.guandian.bidding.module.manager.support.ManagerProjectGuard;
import com.guandian.bidding.module.tender.entity.BidDocument;
import com.guandian.bidding.module.tender.entity.BidRegistration;
import com.guandian.bidding.module.tender.entity.EvaluationItem;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.BidDocumentMapper;
import com.guandian.bidding.module.tender.mapper.BidRegistrationMapper;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ManagerEvaluationService {

    private final ManagerProjectGuard projectGuard;
    private final EvaluationResultService resultService;
    private final BidRegistrationMapper registrationMapper;
    private final BidDocumentMapper bidDocumentMapper;

    public PriceConfirmResponse getPriceConfirm(Long projectId) {
        projectGuard.requireOwnedProject(projectId);
        List<BidRegistration> regs = registrationMapper.selectList(
                new LambdaQueryWrapper<BidRegistration>()
                        .eq(BidRegistration::getProjectId, projectId)
                        .eq(BidRegistration::getRegStatus, "SUCCESS")
                        .orderByAsc(BidRegistration::getId));

        List<PriceConfirmResponse.PriceItem> items = new ArrayList<>();
        for (BidRegistration reg : regs) {
            BidDocument doc = bidDocumentMapper.selectOne(new LambdaQueryWrapper<BidDocument>()
                    .eq(BidDocument::getRegistrationId, reg.getId())
                    .isNull(BidDocument::getWithdrawTime)
                    .orderByDesc(BidDocument::getSubmitTime)
                    .last("LIMIT 1"));
            if (doc == null) {
                continue;
            }
            boolean ready = doc.getDecryptStatus() != null && doc.getDecryptStatus() == 1;
            items.add(PriceConfirmResponse.PriceItem.builder()
                    .registrationId(reg.getId())
                    .companyName(reg.getCompanyName())
                    .bidPrice(doc.getBidPrice())
                    .decryptStatus(doc.getDecryptStatus())
                    .ready(ready)
                    .build());
        }
        return PriceConfirmResponse.builder().projectId(projectId).items(items).build();
    }

    public ComplianceResultResponse getCompliance(Long projectId, Long registrationId) {
        projectGuard.requireOwnedProject(projectId);
        return resultService.buildCompliance(projectId, registrationId, registrationId != null);
    }

    public ScoreSummaryResponse getScores(Long projectId, Long registrationId) {
        projectGuard.requireOwnedProject(projectId);
        return resultService.buildScoreSummary(projectId, registrationId, registrationId != null);
    }
}
