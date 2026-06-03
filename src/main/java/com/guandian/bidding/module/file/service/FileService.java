package com.guandian.bidding.module.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.guandian.bidding.common.api.ResultCode;
import com.guandian.bidding.common.exception.BusinessException;
import com.guandian.bidding.config.FileStorageProperties;
import com.guandian.bidding.module.file.dto.FileUploadResponse;
import com.guandian.bidding.module.file.storage.FileStorageService;
import com.guandian.bidding.module.tender.entity.Attachment;
import com.guandian.bidding.module.tender.entity.BidDocument;
import com.guandian.bidding.module.tender.entity.BidRegistration;
import com.guandian.bidding.module.tender.entity.ExpertAssignment;
import com.guandian.bidding.module.tender.entity.TenderProject;
import com.guandian.bidding.module.tender.mapper.AttachmentMapper;
import com.guandian.bidding.module.tender.mapper.BidDocumentMapper;
import com.guandian.bidding.module.tender.mapper.BidRegistrationMapper;
import com.guandian.bidding.module.tender.mapper.ExpertAssignmentMapper;
import com.guandian.bidding.module.tender.mapper.TenderProjectMapper;
import com.guandian.bidding.security.LoginUser;
import com.guandian.bidding.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Set<String> OPENING_STATUS = new HashSet<>(
            Arrays.asList("OPENING", "OPENED", "AWARDED", "FINISHED", "ARCHIVED"));

    private final AttachmentMapper attachmentMapper;
    private final BidDocumentMapper bidDocumentMapper;
    private final BidRegistrationMapper registrationMapper;
    private final TenderProjectMapper projectMapper;
    private final ExpertAssignmentMapper assignmentMapper;
    private final FileStorageService fileStorageService;
    private final FileStorageProperties fileStorageProperties;

    public FileUploadResponse upload(MultipartFile file, String bizType) {
        LoginUser user = SecurityUtils.requireLoginUser();
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "文件不能为空");
        }
        long maxBytes = (long) fileStorageProperties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ResultCode.PARAM_ERROR,
                    "文件大小不能超过 " + fileStorageProperties.getMaxSizeMb() + "MB");
        }

        String normalizedBizType = normalizeBizType(bizType);
        String originalFilename = file.getOriginalFilename();
        String contentType = StringUtils.hasText(file.getContentType())
                ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        FileStorageService.StoredFile stored;
        try {
            stored = fileStorageService.store(originalFilename, contentType, file.getInputStream(), file.getSize());
        } catch (IOException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR, "文件保存失败");
        }

        Attachment attachment = new Attachment();
        attachment.setFileName(originalFilename);
        attachment.setFileKey(stored.getFileKey());
        attachment.setFileSize(stored.getSize());
        attachment.setContentType(contentType);
        attachment.setBizType(normalizedBizType);
        attachment.setCreateBy(user.getUserId());
        attachmentMapper.insert(attachment);

        return FileUploadResponse.builder()
                .attachId(attachment.getId())
                .fileName(attachment.getFileName())
                .url("/api/files/" + attachment.getId())
                .fileSize(attachment.getFileSize())
                .contentType(attachment.getContentType())
                .bizType(attachment.getBizType())
                .build();
    }

    public ResponseEntity<Resource> download(Long attachId) {
        LoginUser user = SecurityUtils.requireLoginUser();
        Attachment attachment = attachmentMapper.selectById(attachId);
        if (attachment == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "附件不存在");
        }
        assertDownloadPermission(attachment, user);

        Resource resource;
        try {
            resource = fileStorageService.loadAsResource(attachment.getFileKey());
        } catch (IOException e) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在或已损坏");
        }

        String contentType = StringUtils.hasText(attachment.getContentType())
                ? attachment.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(attachment.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(attachment.getFileSize() != null ? attachment.getFileSize() : -1)
                .body(resource);
    }

    private void assertDownloadPermission(Attachment attachment, LoginUser user) {
        if ("ADMIN".equals(user.getActiveRole())) {
            return;
        }
        if (user.getUserId().equals(attachment.getCreateBy())) {
            return;
        }

        BidDocument bidDoc = bidDocumentMapper.selectOne(new LambdaQueryWrapper<BidDocument>()
                .eq(BidDocument::getAttachId, attachment.getId())
                .isNull(BidDocument::getWithdrawTime)
                .orderByDesc(BidDocument::getSubmitTime)
                .last("LIMIT 1"));
        if (bidDoc != null || "bid_doc".equals(attachment.getBizType())) {
            assertBidDocumentAccess(bidDoc, attachment.getId(), user);
            return;
        }

        BidRegistration applyReg = registrationMapper.selectOne(new LambdaQueryWrapper<BidRegistration>()
                .eq(BidRegistration::getApplyFileId, attachment.getId())
                .last("LIMIT 1"));
        if (applyReg != null) {
            assertRegistrationFileAccess(applyReg, user);
            return;
        }

        if ("MANAGER".equals(user.getActiveRole())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权下载该文件");
    }

    private void assertBidDocumentAccess(BidDocument bidDoc, Long attachId, LoginUser user) {
        if (bidDoc == null) {
            bidDoc = bidDocumentMapper.selectOne(new LambdaQueryWrapper<BidDocument>()
                    .eq(BidDocument::getAttachId, attachId)
                    .isNull(BidDocument::getWithdrawTime)
                    .orderByDesc(BidDocument::getSubmitTime)
                    .last("LIMIT 1"));
        }
        if (bidDoc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "投标文件不存在");
        }
        BidRegistration reg = registrationMapper.selectById(bidDoc.getRegistrationId());
        if (reg == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        if (project == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        if ("BIDDER".equals(user.getActiveRole()) && user.getUserId().equals(reg.getSupplierId())) {
            return;
        }

        if (!OPENING_STATUS.contains(project.getStatus())) {
            throw new BusinessException(ResultCode.BID_FILE_LOCKED);
        }
        if (bidDoc.getDecryptStatus() == null || bidDoc.getDecryptStatus() != 1) {
            throw new BusinessException(ResultCode.BID_FILE_LOCKED);
        }

        if ("MANAGER".equals(user.getActiveRole()) && user.getUserId().equals(project.getManagerId())) {
            return;
        }
        if ("EXPERT".equals(user.getActiveRole())) {
            ExpertAssignment assignment = assignmentMapper.selectOne(new LambdaQueryWrapper<ExpertAssignment>()
                    .eq(ExpertAssignment::getProjectId, project.getId())
                    .eq(ExpertAssignment::getExpertId, user.getUserId())
                    .last("LIMIT 1"));
            if (assignment != null && "SIGNED".equals(assignment.getStatus())) {
                return;
            }
        }
        throw new BusinessException(ResultCode.BID_FILE_LOCKED);
    }

    private void assertRegistrationFileAccess(BidRegistration reg, LoginUser user) {
        if ("BIDDER".equals(user.getActiveRole()) && user.getUserId().equals(reg.getSupplierId())) {
            return;
        }
        TenderProject project = projectMapper.selectById(reg.getProjectId());
        if (project != null && "MANAGER".equals(user.getActiveRole())
                && user.getUserId().equals(project.getManagerId())) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权下载该报名文件");
    }

    private String normalizeBizType(String bizType) {
        if (!StringUtils.hasText(bizType)) {
            return "general";
        }
        return bizType.trim().toLowerCase();
    }
}
