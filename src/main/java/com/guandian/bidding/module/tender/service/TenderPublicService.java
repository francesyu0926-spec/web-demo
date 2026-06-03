package com.guandian.bidding.module.tender.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.tender.dto.AnnouncementItemResponse;
import com.guandian.bidding.module.tender.dto.TenderDetailResponse;
import com.guandian.bidding.module.tender.dto.WinnerDetailResponse;
import com.guandian.bidding.module.tender.dto.WinnerItemResponse;
import com.guandian.bidding.module.tender.entity.Announcement;
import com.guandian.bidding.module.tender.entity.Award;
import com.guandian.bidding.module.tender.entity.BidRegistration;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.AnnouncementMapper;
import com.guandian.bidding.module.tender.mapper.AwardMapper;
import com.guandian.bidding.module.tender.mapper.BidRegistrationMapper;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenderPublicService {

    private static final Set<String> HIDDEN_STATUS = Set.of("DRAFT", "ABORTED");
    private static final List<String> PUBLIC_STATUS = Arrays.asList(
            "BIDDING", "OPENING", "OPENED", "AWARDED", "FINISHED", "ARCHIVED");

    private final AnnouncementMapper announcementMapper;
    private final TenderProjectMapper projectMapper;
    private final AwardMapper awardMapper;
    private final BidRegistrationMapper registrationMapper;

    public PageResult<AnnouncementItemResponse> listAnnouncements(long pageNum, long pageSize) {
        Page<Announcement> page = announcementMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getType, 1)
                        .orderByDesc(Announcement::getPublishTime)
                        .orderByDesc(Announcement::getId));
        return buildAnnouncementPage(page, pageNum, pageSize);
    }

    public PageResult<AnnouncementItemResponse> listHomeTenders(String category, String tenderType,
                                                                 String procurementType,
                                                                 long pageNum, long pageSize) {
        LambdaQueryWrapper<TenderProject> wrapper = new LambdaQueryWrapper<TenderProject>()
                .in(TenderProject::getStatus, PUBLIC_STATUS)
                .orderByDesc(TenderProject::getBidOpenTime)
                .orderByDesc(TenderProject::getId);

        if ("BIDDING".equalsIgnoreCase(category)) {
            wrapper.in(TenderProject::getProcurementType, Arrays.asList("PUBLIC", "INVITE"));
        } else if ("NON_BIDDING".equalsIgnoreCase(category)) {
            wrapper.in(TenderProject::getProcurementType,
                    Arrays.asList("INQUIRY", "SINGLE", "NEGOTIATION", "CONSULTATION"));
        }
        if (StringUtils.hasText(tenderType)) {
            wrapper.eq(TenderProject::getTenderType, tenderType);
        }
        if (StringUtils.hasText(procurementType)) {
            wrapper.eq(TenderProject::getProcurementType, procurementType);
        }

        Page<TenderProject> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AnnouncementItemResponse> list = page.getRecords().stream()
                .map(this::toAnnouncementItemFromProject)
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public PageResult<WinnerItemResponse> listWinners(long pageNum, long pageSize) {
        Page<Announcement> page = announcementMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getType, 2)
                        .orderByDesc(Announcement::getPublishTime)
                        .orderByDesc(Announcement::getId));
        List<Announcement> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page.getTotal(), pageNum, pageSize);
        }

        Map<Long, TenderProject> projectMap = loadProjects(records.stream()
                .map(Announcement::getProjectId).collect(Collectors.toList()));
        Map<Long, Award> winnerMap = loadWinnerAwards(records.stream()
                .map(Announcement::getProjectId).collect(Collectors.toList()));
        Map<Long, BidRegistration> regMap = loadRegistrations(winnerMap.values().stream()
                .map(Award::getRegistrationId).collect(Collectors.toList()));

        List<WinnerItemResponse> list = records.stream()
                .map(a -> {
                    TenderProject p = projectMap.get(a.getProjectId());
                    if (p == null || HIDDEN_STATUS.contains(p.getStatus())) {
                        return null;
                    }
                    Award award = winnerMap.get(a.getProjectId());
                    BidRegistration reg = award != null ? regMap.get(award.getRegistrationId()) : null;
                    return WinnerItemResponse.builder()
                            .id(a.getId())
                            .projectId(p.getId())
                            .projectNo(p.getProjectNo())
                            .projectName(p.getName())
                            .winnerCompany(reg != null ? reg.getCompanyName() : null)
                            .finalPrice(award != null ? award.getFinalPrice() : null)
                            .publicityStart(award != null ? award.getPublicityStart() : null)
                            .publicityEnd(award != null ? award.getPublicityEnd() : null)
                            .publishTime(a.getPublishTime())
                            .build();
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public PageResult<AnnouncementItemResponse> searchTenders(String keyword, String region,
                                                               String category, String time,
                                                               long pageNum, long pageSize) {
        LocalDateTime timeStart = parseTimeStart(time);
        LambdaQueryWrapper<TenderProject> wrapper = new LambdaQueryWrapper<TenderProject>()
                .in(TenderProject::getStatus, PUBLIC_STATUS)
                .like(StringUtils.hasText(keyword), TenderProject::getName, keyword)
                .eq(StringUtils.hasText(region), TenderProject::getRegion, region)
                .and(StringUtils.hasText(category), w -> w
                        .eq(TenderProject::getTenderType, category)
                        .or()
                        .eq(TenderProject::getIndustry, category))
                .ge(timeStart != null, TenderProject::getBidOpenTime, timeStart)
                .orderByDesc(TenderProject::getBidOpenTime)
                .orderByDesc(TenderProject::getId);

        Page<TenderProject> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<AnnouncementItemResponse> list = page.getRecords().stream()
                .map(this::toAnnouncementItemFromProject)
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public PageResult<WinnerItemResponse> searchWinners(String keyword, String region,
                                                         String category, String time,
                                                         long pageNum, long pageSize) {
        LocalDateTime timeStart = parseTimeStart(time);
        LambdaQueryWrapper<TenderProject> projectWrapper = new LambdaQueryWrapper<TenderProject>()
                .in(TenderProject::getStatus, Arrays.asList("AWARDED", "FINISHED", "ARCHIVED"))
                .like(StringUtils.hasText(keyword), TenderProject::getName, keyword)
                .eq(StringUtils.hasText(region), TenderProject::getRegion, region)
                .and(StringUtils.hasText(category), w -> w
                        .eq(TenderProject::getTenderType, category)
                        .or()
                        .eq(TenderProject::getIndustry, category));
        List<TenderProject> projects = projectMapper.selectList(projectWrapper);
        if (projects.isEmpty()) {
            return PageResult.of(Collections.emptyList(), 0, pageNum, pageSize);
        }

        List<Long> projectIds = projects.stream().map(TenderProject::getId).collect(Collectors.toList());
        Map<Long, TenderProject> projectMap = projects.stream()
                .collect(Collectors.toMap(TenderProject::getId, Function.identity()));

        LambdaQueryWrapper<Award> awardWrapper = new LambdaQueryWrapper<Award>()
                .in(Award::getProjectId, projectIds)
                .eq(Award::getIsWinner, 1)
                .ge(timeStart != null, Award::getPublicityStart, timeStart)
                .orderByDesc(Award::getPublicityStart)
                .orderByDesc(Award::getId);
        Page<Award> page = awardMapper.selectPage(new Page<>(pageNum, pageSize), awardWrapper);

        Map<Long, BidRegistration> regMap = loadRegistrations(page.getRecords().stream()
                .map(Award::getRegistrationId).collect(Collectors.toList()));

        List<WinnerItemResponse> list = page.getRecords().stream()
                .map(award -> {
                    TenderProject p = projectMap.get(award.getProjectId());
                    BidRegistration reg = regMap.get(award.getRegistrationId());
                    return WinnerItemResponse.builder()
                            .id(award.getId())
                            .projectId(p.getId())
                            .projectNo(p.getProjectNo())
                            .projectName(p.getName())
                            .winnerCompany(reg != null ? reg.getCompanyName() : null)
                            .finalPrice(award.getFinalPrice())
                            .publicityStart(award.getPublicityStart())
                            .publicityEnd(award.getPublicityEnd())
                            .publishTime(award.getNoticePublishTime() != null
                                    ? award.getNoticePublishTime() : award.getCreateTime())
                            .build();
                })
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public TenderDetailResponse getTenderDetail(Long id) {
        TenderProject project = projectMapper.selectById(id);
        if (project == null || HIDDEN_STATUS.contains(project.getStatus())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        return TenderDetailResponse.builder()
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
                .regStart(project.getRegStart())
                .regEnd(project.getRegEnd())
                .bidOpenTime(project.getBidOpenTime())
                .fileFee(project.getFileFee())
                .platformFee(project.getPlatformFee())
                .content(project.getContent())
                .bidFileId(project.getBidFileId())
                .build();
    }

    public WinnerDetailResponse getWinnerDetail(Long id) {
        Award award = awardMapper.selectById(id);
        if (award == null || award.getIsWinner() == null || award.getIsWinner() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        TenderProject project = projectMapper.selectById(award.getProjectId());
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        BidRegistration reg = registrationMapper.selectById(award.getRegistrationId());
        Announcement announcement = announcementMapper.selectOne(
                new LambdaQueryWrapper<Announcement>()
                        .eq(Announcement::getProjectId, project.getId())
                        .eq(Announcement::getType, 2)
                        .orderByDesc(Announcement::getPublishTime)
                        .last("LIMIT 1"));

        return WinnerDetailResponse.builder()
                .id(award.getId())
                .projectId(project.getId())
                .projectNo(project.getProjectNo())
                .projectName(project.getName())
                .winnerCompany(reg != null ? reg.getCompanyName() : null)
                .finalPrice(award.getFinalPrice())
                .finalScore(award.getFinalScore())
                .publicityStart(award.getPublicityStart())
                .publicityEnd(award.getPublicityEnd())
                .noticePublishTime(award.getNoticePublishTime())
                .content(announcement != null ? announcement.getContent() : null)
                .build();
    }

    private PageResult<AnnouncementItemResponse> buildAnnouncementPage(Page<Announcement> page,
                                                                        long pageNum, long pageSize) {
        List<Announcement> records = page.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(Collections.emptyList(), page.getTotal(), pageNum, pageSize);
        }
        Map<Long, TenderProject> projectMap = loadProjects(records.stream()
                .map(Announcement::getProjectId).collect(Collectors.toList()));
        List<AnnouncementItemResponse> list = records.stream()
                .map(a -> {
                    TenderProject p = projectMap.get(a.getProjectId());
                    if (p == null || HIDDEN_STATUS.contains(p.getStatus())) {
                        return null;
                    }
                    return AnnouncementItemResponse.builder()
                            .id(a.getId())
                            .projectId(p.getId())
                            .projectNo(p.getProjectNo())
                            .projectName(p.getName())
                            .region(p.getRegion())
                            .tenderType(p.getTenderType())
                            .procurementType(p.getProcurementType())
                            .budget(p.getBudget())
                            .publishTime(a.getPublishTime())
                            .bidOpenTime(p.getBidOpenTime())
                            .regEnd(p.getRegEnd())
                            .build();
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    private AnnouncementItemResponse toAnnouncementItemFromProject(TenderProject p) {
        return AnnouncementItemResponse.builder()
                .id(p.getId())
                .projectId(p.getId())
                .projectNo(p.getProjectNo())
                .projectName(p.getName())
                .region(p.getRegion())
                .tenderType(p.getTenderType())
                .procurementType(p.getProcurementType())
                .budget(p.getBudget())
                .publishTime(p.getCreateTime())
                .bidOpenTime(p.getBidOpenTime())
                .regEnd(p.getRegEnd())
                .build();
    }

    private Map<Long, TenderProject> loadProjects(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return projectMapper.selectBatchIds(projectIds.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(TenderProject::getId, Function.identity()));
    }

    private Map<Long, Award> loadWinnerAwards(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return awardMapper.selectList(new LambdaQueryWrapper<Award>()
                        .in(Award::getProjectId, projectIds)
                        .eq(Award::getIsWinner, 1))
                .stream().collect(Collectors.toMap(Award::getProjectId, Function.identity(), (a, b) -> a));
    }

    private Map<Long, BidRegistration> loadRegistrations(List<Long> registrationIds) {
        if (registrationIds == null || registrationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return registrationMapper.selectBatchIds(registrationIds.stream().distinct().collect(Collectors.toList()))
                .stream().collect(Collectors.toMap(BidRegistration::getId, Function.identity()));
    }

    private LocalDateTime parseTimeStart(String time) {
        if (!StringUtils.hasText(time)) {
            return null;
        }
        switch (time.toLowerCase()) {
            case "7d":
                return LocalDateTime.now().minusDays(7);
            case "30d":
                return LocalDateTime.now().minusDays(30);
            case "90d":
                return LocalDateTime.now().minusDays(90);
            default:
                return null;
        }
    }
}
