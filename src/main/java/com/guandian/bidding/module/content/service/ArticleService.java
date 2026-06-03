package com.guandian.bidding.module.content.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guandian.bidding.common.api.PageResult;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.module.content.dto.ArticleDetailResponse;
import com.guandian.bidding.module.content.dto.ArticleSummaryResponse;
import com.guandian.bidding.module.content.entity.Article;
import com.guandian.bidding.module.content.entity.ArticleCategory;
import com.guandian.bidding.module.content.mapper.ArticleCategoryMapper;
import com.guandian.bidding.module.content.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper categoryMapper;

    public PageResult<ArticleSummaryResponse> list(String categoryCode, long pageNum, long pageSize) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .orderByDesc(Article::getPublishTime)
                .orderByDesc(Article::getId);
        if (StringUtils.hasText(categoryCode)) {
            ArticleCategory category = categoryMapper.selectOne(
                    new LambdaQueryWrapper<ArticleCategory>().eq(ArticleCategory::getCode, categoryCode));
            if (category == null) {
                return PageResult.of(Collections.emptyList(), 0, pageNum, pageSize);
            }
            wrapper.eq(Article::getCategoryId, category.getId());
        }

        Page<Article> page = articleMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        Map<Long, ArticleCategory> categoryMap = loadCategories(page.getRecords());
        List<ArticleSummaryResponse> list = page.getRecords().stream()
                .map(a -> {
                    ArticleCategory c = categoryMap.get(a.getCategoryId());
                    return ArticleSummaryResponse.builder()
                            .id(a.getId())
                            .categoryCode(c != null ? c.getCode() : null)
                            .title(a.getTitle())
                            .publishTime(a.getPublishTime())
                            .build();
                })
                .collect(Collectors.toList());
        return PageResult.of(list, page.getTotal(), pageNum, pageSize);
    }

    public ArticleDetailResponse getDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        ArticleCategory category = categoryMapper.selectById(article.getCategoryId());
        return ArticleDetailResponse.builder()
                .id(article.getId())
                .categoryCode(category != null ? category.getCode() : null)
                .categoryName(category != null ? category.getName() : null)
                .title(article.getTitle())
                .content(article.getContent())
                .attachId(article.getAttachId())
                .publishTime(article.getPublishTime())
                .build();
    }

    private Map<Long, ArticleCategory> loadCategories(List<Article> articles) {
        if (articles.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> ids = articles.stream().map(Article::getCategoryId).distinct().collect(Collectors.toList());
        return categoryMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(ArticleCategory::getId, c -> c));
    }
}
