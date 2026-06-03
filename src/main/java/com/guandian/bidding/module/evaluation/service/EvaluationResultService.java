package com.guandian.bidding.module.evaluation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.module.evaluation.dto.ComplianceResultResponse;
import com.guandian.bidding.module.evaluation.dto.FinalScoreResponse;
import com.guandian.bidding.module.evaluation.dto.ScoreSummaryResponse;
import com.guandian.bidding.module.tender.entity.BidDocument;
import com.guandian.bidding.module.tender.entity.BidRegistration;
import com.guandian.bidding.module.tender.entity.EvaluationItem;
import com.guandian.bidding.module.tender.entity.EvaluationScore;
import com.guandian.bidding.module.tender.entity.ExpertAssignment;
import com.guandian.bidding.module.tender.mapper.BidDocumentMapper;
import com.guandian.bidding.module.tender.mapper.BidRegistrationMapper;
import com.guandian.bidding.module.tender.mapper.EvaluationItemMapper;
import com.guandian.bidding.module.tender.mapper.EvaluationScoreMapper;
import com.guandian.bidding.module.tender.mapper.ExpertAssignmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationResultService {

    private static final Set<String> REVIEW_TYPES = new HashSet<>(Arrays.asList("FORMAL", "QUALIFY", "RESPONSE"));
    private static final Set<String> SCORE_TYPES = new HashSet<>(Arrays.asList("COMMERCE", "TECH", "PRICE"));

    private final EvaluationItemMapper itemMapper;
    private final EvaluationScoreMapper scoreMapper;
    private final BidRegistrationMapper registrationMapper;
    private final BidDocumentMapper bidDocumentMapper;
    private final ExpertAssignmentMapper assignmentMapper;

    public ComplianceResultResponse buildCompliance(Long projectId, Long registrationId, boolean includeExpertVotes) {
        List<BidRegistration> regs = listDecryptedRegistrations(projectId, registrationId);
        List<EvaluationItem> reviewItems = listItems(projectId, REVIEW_TYPES);
        Map<Long, List<EvaluationScore>> scoreMap = loadScores(projectId, reviewItems, null);

        List<ComplianceResultResponse.BidderCompliance> bidders = new ArrayList<>();
        for (BidRegistration reg : regs) {
            List<ComplianceResultResponse.ItemResult> itemResults = new ArrayList<>();
            boolean allPassed = true;
            for (EvaluationItem item : reviewItems) {
                List<EvaluationScore> votes = scoreMap.getOrDefault(key(reg.getId(), item.getId()), Collections.emptyList());
                int passCount = (int) votes.stream().filter(v -> v.getPass() != null && v.getPass() == 1).count();
                int failCount = (int) votes.stream().filter(v -> v.getPass() != null && v.getPass() == 0).count();
                boolean passed = !votes.isEmpty() && failCount == 0 && passCount == votes.size();
                if (!passed) {
                    allPassed = false;
                }
                ComplianceResultResponse.ItemResult.ItemResultBuilder builder = ComplianceResultResponse.ItemResult.builder()
                        .itemId(item.getId())
                        .type(item.getType())
                        .name(item.getName())
                        .passed(passed)
                        .passCount(passCount)
                        .failCount(failCount);
                if (includeExpertVotes) {
                    builder.expertVotes(votes.stream()
                            .map(v -> ComplianceResultResponse.ExpertVote.builder()
                                    .expertId(v.getExpertId())
                                    .pass(v.getPass())
                                    .build())
                            .collect(Collectors.toList()));
                }
                itemResults.add(builder.build());
            }
            bidders.add(ComplianceResultResponse.BidderCompliance.builder()
                    .registrationId(reg.getId())
                    .companyName(reg.getCompanyName())
                    .passed(allPassed && !reviewItems.isEmpty())
                    .items(itemResults)
                    .build());
        }
        return ComplianceResultResponse.builder().projectId(projectId).bidders(bidders).build();
    }

    public ScoreSummaryResponse buildScoreSummary(Long projectId, Long registrationId, boolean includeExpertDetails) {
        ComplianceResultResponse compliance = buildCompliance(projectId, registrationId, false);
        Map<Long, Boolean> qualifiedMap = compliance.getBidders().stream()
                .collect(Collectors.toMap(ComplianceResultResponse.BidderCompliance::getRegistrationId,
                        b -> Boolean.TRUE.equals(b.getPassed()), (a, b) -> a));

        List<BidRegistration> regs = listDecryptedRegistrations(projectId, registrationId);
        List<EvaluationItem> scoreItems = listItems(projectId, SCORE_TYPES);
        Map<Long, List<EvaluationScore>> scoreMap = loadScores(projectId, scoreItems, null);

        List<ScoreSummaryResponse.BidderScore> bidders = new ArrayList<>();
        for (BidRegistration reg : regs) {
            if (!Boolean.TRUE.equals(qualifiedMap.get(reg.getId()))) {
                continue;
            }
            BigDecimal commerce = sumTypeScore(projectId, reg.getId(), scoreItems, scoreMap, "COMMERCE");
            BigDecimal tech = sumTypeScore(projectId, reg.getId(), scoreItems, scoreMap, "TECH");
            BigDecimal price = sumTypeScore(projectId, reg.getId(), scoreItems, scoreMap, "PRICE");
            BigDecimal total = commerce.add(tech).add(price);

            ScoreSummaryResponse.BidderScore.BidderScoreBuilder builder = ScoreSummaryResponse.BidderScore.builder()
                    .registrationId(reg.getId())
                    .companyName(reg.getCompanyName())
                    .commerceScore(commerce)
                    .techScore(tech)
                    .priceScore(price)
                    .totalScore(total);

            if (includeExpertDetails) {
                builder.expertDetails(buildExpertDetails(reg.getId(), scoreItems, scoreMap));
            }
            bidders.add(builder.build());
        }

        bidders.sort((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()));
        for (int i = 0; i < bidders.size(); i++) {
            bidders.get(i).setRank(i + 1);
        }
        return ScoreSummaryResponse.builder().projectId(projectId).bidders(bidders).build();
    }

    public FinalScoreResponse buildFinalScore(Long projectId) {
        ScoreSummaryResponse summary = buildScoreSummary(projectId, null, false);
        ComplianceResultResponse compliance = buildCompliance(projectId, null, false);
        Map<Long, Boolean> qualifiedMap = compliance.getBidders().stream()
                .collect(Collectors.toMap(ComplianceResultResponse.BidderCompliance::getRegistrationId,
                        b -> Boolean.TRUE.equals(b.getPassed()), (a, b) -> a));

        List<FinalScoreResponse.CandidateItem> candidates = summary.getBidders().stream()
                .map(b -> FinalScoreResponse.CandidateItem.builder()
                        .registrationId(b.getRegistrationId())
                        .companyName(b.getCompanyName())
                        .qualified(qualifiedMap.getOrDefault(b.getRegistrationId(), false))
                        .commerceScore(b.getCommerceScore())
                        .techScore(b.getTechScore())
                        .priceScore(b.getPriceScore())
                        .totalScore(b.getTotalScore())
                        .rank(b.getRank())
                        .build())
                .collect(Collectors.toList());

        return FinalScoreResponse.builder().projectId(projectId).candidates(candidates).build();
    }

    public boolean isRegistrationQualified(Long projectId, Long registrationId) {
        ComplianceResultResponse compliance = buildCompliance(projectId, registrationId, false);
        if (compliance.getBidders().isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(compliance.getBidders().get(0).getPassed());
    }

    private List<ScoreSummaryResponse.ExpertScoreDetail> buildExpertDetails(
            Long registrationId, List<EvaluationItem> scoreItems, Map<Long, List<EvaluationScore>> scoreMap) {
        Set<Long> expertIds = scoreItems.stream()
                .flatMap(item -> scoreMap.getOrDefault(key(registrationId, item.getId()), Collections.emptyList()).stream())
                .map(EvaluationScore::getExpertId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<ScoreSummaryResponse.ExpertScoreDetail> details = new ArrayList<>();
        for (Long expertId : expertIds) {
            BigDecimal commerce = sumExpertType(registrationId, expertId, scoreItems, scoreMap, "COMMERCE");
            BigDecimal tech = sumExpertType(registrationId, expertId, scoreItems, scoreMap, "TECH");
            BigDecimal price = sumExpertType(registrationId, expertId, scoreItems, scoreMap, "PRICE");
            boolean submitted = scoreItems.stream()
                    .flatMap(item -> scoreMap.getOrDefault(key(registrationId, item.getId()), Collections.emptyList()).stream())
                    .filter(s -> expertId.equals(s.getExpertId()))
                    .anyMatch(s -> s.getSubmitted() != null && s.getSubmitted() == 1);
            details.add(ScoreSummaryResponse.ExpertScoreDetail.builder()
                    .expertId(expertId)
                    .commerceScore(commerce)
                    .techScore(tech)
                    .priceScore(price)
                    .totalScore(commerce.add(tech).add(price))
                    .submitted(submitted)
                    .build());
        }
        return details;
    }

    private BigDecimal sumTypeScore(Long projectId, Long registrationId, List<EvaluationItem> items,
                                    Map<Long, List<EvaluationScore>> scoreMap, String type) {
        Long leaderExpertId = resolveLeaderExpertId(projectId);
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (EvaluationItem item : items) {
            if (!type.equals(item.getType())) {
                continue;
            }
            List<EvaluationScore> votes = scoreMap.getOrDefault(key(registrationId, item.getId()), Collections.emptyList())
                    .stream()
                    .filter(s -> s.getSubmitted() != null && s.getSubmitted() == 1)
                    .filter(s -> s.getScore() != null)
                    .collect(Collectors.toList());
            if ("TECH".equals(type)) {
                if (votes.isEmpty()) {
                    continue;
                }
                BigDecimal avg = votes.stream()
                        .map(EvaluationScore::getScore)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(votes.size()), 2, RoundingMode.HALF_UP);
                sum = sum.add(avg);
                count++;
            } else {
                if (leaderExpertId == null) {
                    continue;
                }
                Long finalLeaderId = leaderExpertId;
                Optional<EvaluationScore> leaderScore = votes.stream()
                        .filter(s -> finalLeaderId.equals(s.getExpertId()))
                        .findFirst();
                if (leaderScore.isPresent()) {
                    sum = sum.add(leaderScore.get().getScore());
                    count++;
                }
            }
        }
        return count == 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : sum.setScale(2, RoundingMode.HALF_UP);
    }

    private Long resolveLeaderExpertId(Long projectId) {
        ExpertAssignment leader = assignmentMapper.selectOne(new LambdaQueryWrapper<ExpertAssignment>()
                .eq(ExpertAssignment::getProjectId, projectId)
                .eq(ExpertAssignment::getIsLeader, 1)
                .last("LIMIT 1"));
        return leader != null ? leader.getExpertId() : null;
    }

    private BigDecimal sumExpertType(Long registrationId, Long expertId, List<EvaluationItem> items,
                                     Map<Long, List<EvaluationScore>> scoreMap, String type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (EvaluationItem item : items) {
            if (!type.equals(item.getType())) {
                continue;
            }
            Optional<EvaluationScore> found = scoreMap.getOrDefault(key(registrationId, item.getId()), Collections.emptyList())
                    .stream()
                    .filter(s -> expertId.equals(s.getExpertId()) && s.getScore() != null)
                    .findFirst();
            if (found.isPresent()) {
                sum = sum.add(found.get().getScore());
            }
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }

    private Map<Long, List<EvaluationScore>> loadScores(Long projectId, List<EvaluationItem> items, Long expertId) {
        if (items.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> itemIds = items.stream().map(EvaluationItem::getId).collect(Collectors.toList());
        LambdaQueryWrapper<EvaluationScore> wrapper = new LambdaQueryWrapper<EvaluationScore>()
                .eq(EvaluationScore::getProjectId, projectId)
                .in(EvaluationScore::getItemId, itemIds);
        if (expertId != null) {
            wrapper.eq(EvaluationScore::getExpertId, expertId);
        }
        return scoreMapper.selectList(wrapper).stream()
                .collect(Collectors.groupingBy(s -> key(s.getRegistrationId(), s.getItemId())));
    }

    private List<EvaluationItem> listItems(Long projectId, Set<String> types) {
        return itemMapper.selectList(new LambdaQueryWrapper<EvaluationItem>()
                        .eq(EvaluationItem::getProjectId, projectId)
                        .in(EvaluationItem::getType, types)
                        .orderByAsc(EvaluationItem::getSort))
                .stream().collect(Collectors.toList());
    }

    private List<BidRegistration> listDecryptedRegistrations(Long projectId, Long registrationId) {
        LambdaQueryWrapper<BidRegistration> wrapper = new LambdaQueryWrapper<BidRegistration>()
                .eq(BidRegistration::getProjectId, projectId)
                .eq(BidRegistration::getRegStatus, "SUCCESS")
                .eq(registrationId != null, BidRegistration::getId, registrationId)
                .orderByAsc(BidRegistration::getId);
        List<BidRegistration> regs = registrationMapper.selectList(wrapper);
        return regs.stream().filter(this::hasDecryptedDocument).collect(Collectors.toList());
    }

    private boolean hasDecryptedDocument(BidRegistration reg) {
        BidDocument doc = bidDocumentMapper.selectOne(new LambdaQueryWrapper<BidDocument>()
                .eq(BidDocument::getRegistrationId, reg.getId())
                .isNull(BidDocument::getWithdrawTime)
                .orderByDesc(BidDocument::getSubmitTime)
                .last("LIMIT 1"));
        return doc != null && doc.getDecryptStatus() != null && doc.getDecryptStatus() == 1;
    }

    private static long key(Long registrationId, Long itemId) {
        return Objects.hash(registrationId, itemId);
    }
}
