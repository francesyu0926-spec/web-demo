-- =====================================================================
-- 观点科技电子招投标交易平台 - 数据库 Schema
-- MySQL 8.0 / InnoDB / utf8mb4
-- 可重复执行：先删后建。生产环境请谨慎执行 DROP。
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `bidding`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;
USE `bidding`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 按依赖反序删除
DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `article_category`;
DROP TABLE IF EXISTS `complaint`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `attachment`;
DROP TABLE IF EXISTS `award`;
DROP TABLE IF EXISTS `evaluation_score`;
DROP TABLE IF EXISTS `evaluation_item`;
DROP TABLE IF EXISTS `expert_assignment`;
DROP TABLE IF EXISTS `deposit`;
DROP TABLE IF EXISTS `bid_document`;
DROP TABLE IF EXISTS `bid_registration`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `tender_project`;
DROP TABLE IF EXISTS `expert_application`;
DROP TABLE IF EXISTS `expert_profile`;
DROP TABLE IF EXISTS `supplier_profile`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 1. 用户与权限
-- =====================================================================

CREATE TABLE `sys_user` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(64)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(128) NULL COMMENT 'BCrypt密码,微信注册可空',
  `real_name`   VARCHAR(64)  NULL COMMENT '姓名',
  `phone`       VARCHAR(20)  NULL COMMENT '手机号',
  `email`       VARCHAR(128) NULL COMMENT '邮箱',
  `wx_openid`   VARCHAR(64)  NULL COMMENT '微信openid',
  `wx_unionid`  VARCHAR(64)  NULL COMMENT '微信unionid',
  `avatar`      VARCHAR(255) NULL COMMENT '头像',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `create_by`   BIGINT       NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT       NULL,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_openid` (`wx_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

CREATE TABLE `sys_role` (
  `id`   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL COMMENT 'GUEST/BIDDER/AGENT/EXPERT/ADMIN',
  `name` VARCHAR(32) NOT NULL COMMENT '角色名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE `sys_user_role` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT NOT NULL,
  `role_id`      BIGINT NOT NULL,
  `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '0待审核 1已通过(专家角色需审核)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系';

CREATE TABLE `supplier_profile` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT NOT NULL,
  `company_name`    VARCHAR(128) NOT NULL COMMENT '企业名称',
  `credit_code`     VARCHAR(32)  NOT NULL COMMENT '统一社会信用代码',
  `legal_person`    VARCHAR(64)  NULL COMMENT '法人',
  `contact_name`    VARCHAR(64)  NULL COMMENT '联系人',
  `contact_phone`   VARCHAR(20)  NULL COMMENT '联系电话',
  `license_file_id` BIGINT       NULL COMMENT '营业执照附件id',
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0待认证 1已认证 2驳回',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_credit` (`credit_code`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投标人企业资料';

CREATE TABLE `expert_profile` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `expert_no`   VARCHAR(32)  NULL COMMENT '专家编号',
  `major`       VARCHAR(128) NULL COMMENT '专业领域',
  `title`       VARCHAR(64)  NULL COMMENT '职称',
  `id_card`     VARCHAR(32)  NULL COMMENT '身份证号',
  `sign_img_id` BIGINT       NULL COMMENT '电子签名图附件id(暂缓)',
  `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家资料';

CREATE TABLE `expert_application` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT NOT NULL,
  `major`        VARCHAR(128) NULL COMMENT '申请专业领域',
  `attach_id`    BIGINT NULL COMMENT '证明材料附件id',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审 1通过 2驳回',
  `audit_remark` VARCHAR(255) NULL,
  `audit_by`     BIGINT NULL,
  `audit_time`   DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_audit` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家入驻申请';

-- =====================================================================
-- 2. 招标侧
-- =====================================================================

CREATE TABLE `tender_project` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_no`     VARCHAR(40)  NOT NULL COMMENT '项目编号',
  `name`           VARCHAR(200) NOT NULL COMMENT '项目名称',
  `category`       VARCHAR(32)  NULL COMMENT '招标方式:公开/邀请/竞争性谈判',
  `industry`       VARCHAR(32)  NULL COMMENT '行业分类',
  `region`         VARCHAR(64)  NULL COMMENT '地区',
  `budget`         DECIMAL(15,2) NULL COMMENT '预算/控制价',
  `deposit_amount` DECIMAL(15,2) NULL COMMENT '保证金金额',
  `agent_id`       BIGINT NOT NULL COMMENT '招标代理(项目经理)user_id',
  `status`         VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                   COMMENT 'DRAFT/PUBLISHED/REG_CLOSED/OPENED/EVALUATING/WIN_PUBLICITY/FINISHED/ABORTED',
  `reg_start`      DATETIME NULL COMMENT '报名开始',
  `reg_end`        DATETIME NULL COMMENT '报名截止',
  `bid_open_time`  DATETIME NULL COMMENT '开标时间',
  `content`        MEDIUMTEXT NULL COMMENT '招标公告正文',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_no` (`project_no`),
  KEY `idx_status` (`status`),
  KEY `idx_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招标项目';

CREATE TABLE `announcement` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`   BIGINT NOT NULL,
  `type`         TINYINT NOT NULL COMMENT '1招标公告 2中标候选人公示 3中标结果',
  `title`        VARCHAR(200) NULL,
  `content`      MEDIUMTEXT NULL,
  `publish_time` DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_type` (`project_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告(招标/中标候选人/中标结果)';

-- =====================================================================
-- 3. 投标侧
-- =====================================================================

CREATE TABLE `bid_registration` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`   BIGINT NOT NULL,
  `supplier_id`  BIGINT NOT NULL COMMENT '投标人user_id',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
  `audit_remark` VARCHAR(255) NULL,
  `bid_status`   VARCHAR(20) NOT NULL DEFAULT 'REGISTERED'
                 COMMENT 'REGISTERED/SUBMITTED/WON/LOST/INVALID',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_supplier` (`project_id`, `supplier_id`),
  KEY `idx_supplier` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投标报名';

CREATE TABLE `bid_document` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `attach_id`       BIGINT NOT NULL COMMENT '投标文件附件id',
  `bid_price`       DECIMAL(15,2) NULL COMMENT '投标报价',
  `submit_time`     DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_reg` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投标文件(开标前不可下载)';

CREATE TABLE `deposit` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `registration_id` BIGINT NOT NULL,
  `amount`          DECIMAL(15,2) NULL,
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0未缴 1已缴 2已退',
  `pay_time`        DATETIME NULL,
  `refund_time`     DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_reg` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保证金(原型标注待定,先预留)';

-- =====================================================================
-- 4. 开评标
-- =====================================================================

CREATE TABLE `expert_assignment` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`   BIGINT NOT NULL,
  `expert_id`    BIGINT NOT NULL COMMENT '专家user_id',
  `status`       VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                 COMMENT 'PENDING/ACCEPTED/REJECTED/SIGNED',
  `respond_time` DATETIME NULL,
  `sign_time`    DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_expert` (`project_id`, `expert_id`),
  KEY `idx_expert` (`expert_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评标专家抽取';

CREATE TABLE `evaluation_item` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL,
  `type`       VARCHAR(20) NOT NULL
               COMMENT 'FORMAL形式/QUALIFY资格/RESPONSE响应性/COMMERCE商务/TECH技术',
  `name`       VARCHAR(128) NULL,
  `max_score`  DECIMAL(6,2) NULL,
  `weight`     DECIMAL(5,2) NULL COMMENT '权重%',
  `sort`       INT NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审项配置(五类评审)';

CREATE TABLE `evaluation_score` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`      BIGINT NOT NULL,
  `registration_id` BIGINT NOT NULL COMMENT '被评投标人',
  `expert_id`       BIGINT NOT NULL COMMENT '评分专家',
  `item_id`         BIGINT NOT NULL COMMENT '评审项',
  `pass`            TINYINT NULL COMMENT '形式/资格/响应性: 0否1是',
  `score`           DECIMAL(6,2) NULL COMMENT '商务/技术评分',
  `remark`          VARCHAR(255) NULL,
  `submitted`       TINYINT NOT NULL DEFAULT 0 COMMENT '0暂存 1已提交',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_score` (`registration_id`, `expert_id`, `item_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分记录(专家×投标人×评审项)';

CREATE TABLE `award` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`      BIGINT NOT NULL,
  `registration_id` BIGINT NOT NULL COMMENT '中标投标人',
  `rank`            INT NULL COMMENT '中标候选人排名(1=第一)',
  `final_score`     DECIMAL(8,2) NULL COMMENT '加权最终得分',
  `is_winner`       TINYINT NOT NULL DEFAULT 0 COMMENT '是否最终中标',
  `publicity_start` DATETIME NULL COMMENT '公示开始',
  `publicity_end`   DATETIME NULL COMMENT '公示结束',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中标候选人/定标';

-- =====================================================================
-- 5. 通用
-- =====================================================================

CREATE TABLE `attachment` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `file_name`    VARCHAR(255) NULL,
  `file_key`     VARCHAR(255) NULL COMMENT '对象存储key',
  `file_size`    BIGINT NULL,
  `content_type` VARCHAR(128) NULL,
  `biz_type`     VARCHAR(32) NULL COMMENT '业务类型:bid_doc/license/article',
  `create_by`    BIGINT NULL,
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件元数据';

CREATE TABLE `notification` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL COMMENT '接收人',
  `type`        VARCHAR(32) NULL COMMENT 'AUDIT/INVITE/OPEN/AWARD',
  `title`       VARCHAR(128) NULL,
  `content`     VARCHAR(500) NULL,
  `biz_id`      BIGINT NULL COMMENT '关联业务id',
  `is_read`     TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

CREATE TABLE `complaint` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NULL,
  `project_id`  BIGINT NULL,
  `type`        VARCHAR(32) NULL COMMENT '投诉/建议',
  `content`     TEXT NULL,
  `attach_id`   BIGINT NULL,
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1处理中 2已回复',
  `reply`       TEXT NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投诉建议';

CREATE TABLE `article_category` (
  `id`   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL COMMENT 'guide/policy/download',
  `name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容栏目';

CREATE TABLE `article` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id`  BIGINT NOT NULL,
  `title`        VARCHAR(200) NULL,
  `content`      MEDIUMTEXT NULL,
  `attach_id`    BIGINT NULL COMMENT '下载专区文件id',
  `publish_time` DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务指南/政策法规/下载专区';

CREATE TABLE `operation_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NULL,
  `role_code`   VARCHAR(32) NULL,
  `module`      VARCHAR(64) NULL,
  `action`      VARCHAR(64) NULL,
  `biz_id`      BIGINT NULL,
  `detail`      VARCHAR(1000) NULL,
  `ip`          VARCHAR(64) NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_biz` (`module`, `biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志(合规必备)';

-- =====================================================================
-- 6. 初始化数据
-- =====================================================================

INSERT INTO `sys_role` (`code`, `name`) VALUES
  ('GUEST',  '游客'),
  ('BIDDER', '投标人'),
  ('AGENT',  '项目经理'),
  ('EXPERT', '评标专家'),
  ('ADMIN',  '平台管理员');

INSERT INTO `article_category` (`code`, `name`) VALUES
  ('guide',    '业务指南'),
  ('policy',   '政策法规'),
  ('download', '下载专区');

-- 默认管理员 (密码占位,实际由后端用 BCrypt 生成后更新)
INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`)
  VALUES ('admin', '$2a$10$REPLACE_WITH_BCRYPT_HASH', '系统管理员', 1);
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `audit_status`)
  SELECT u.id, r.id, 1 FROM `sys_user` u, `sys_role` r
  WHERE u.username = 'admin' AND r.code = 'ADMIN';
