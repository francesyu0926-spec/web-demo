package com.guandian.bidding.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.module.content.mapper.SearchHistoryMapper;
import com.guandian.bidding.module.content.mapper.SearchHotKeywordMapper;
import com.guandian.bidding.module.tender.service.TenderPublicService;
import com.guandian.bidding.security.LoginUser;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.guandian.bidding.module.content.entity.SearchHistory;
import com.guandian.bidding.module.content.entity.SearchHotKeyword;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final TenderPublicService tenderPublicService;
    private final SearchHotKeywordMapper hotKeywordMapper;
    private final SearchHistoryMapper historyMapper;

    @Transactional(rollbackFor = Exception.class)
    public PageResult<?> search(String type, String keyword, String region, String category,
                                String time, long pageNum, long pageSize) {
        if (StringUtils.hasText(keyword)) {
            recordKeyword(keyword.trim());
        }
        if ("winner".equalsIgnoreCase(type)) {
            return tenderPublicService.searchWinners(keyword, region, category, time, pageNum, pageSize);
        }
        return tenderPublicService.searchTenders(keyword, region, category, time, pageNum, pageSize);
    }

    public List<String> hotKeywords() {
        return hotKeywordMapper.selectList(new LambdaQueryWrapper<SearchHotKeyword>()
                        .orderByDesc(SearchHotKeyword::getSearchCount)
                        .last("LIMIT 10"))
                .stream().map(SearchHotKeyword::getKeyword).collect(Collectors.toList());
    }

    public List<String> searchHistory() {
        LoginUser user = SecurityUtils.requireLoginUser();
        return historyMapper.selectList(new LambdaQueryWrapper<SearchHistory>()
                        .eq(SearchHistory::getUserId, user.getUserId())
                        .orderByDesc(SearchHistory::getSearchTime)
                        .last("LIMIT 10"))
                .stream()
                .map(SearchHistory::getKeyword)
                .distinct()
                .limit(3)
                .collect(Collectors.toList());
    }

    private void recordKeyword(String keyword) {
        SearchHotKeyword hot = hotKeywordMapper.selectOne(
                new LambdaQueryWrapper<SearchHotKeyword>().eq(SearchHotKeyword::getKeyword, keyword));
        if (hot == null) {
            hot = new SearchHotKeyword();
            hot.setKeyword(keyword);
            hot.setSearchCount(1L);
            hotKeywordMapper.insert(hot);
        } else {
            hot.setSearchCount(hot.getSearchCount() + 1);
            hotKeywordMapper.updateById(hot);
        }

        LoginUser user = SecurityUtils.getLoginUser();
        if (user != null) {
            SearchHistory history = new SearchHistory();
            history.setUserId(user.getUserId());
            history.setKeyword(keyword);
            historyMapper.insert(history);
        }
    }
}
