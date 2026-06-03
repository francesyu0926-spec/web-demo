-- =====================================================================
-- 观点科技电子招投标交易平台 - 数据库 Schema
-- 依据《研发需求文档 V1.4》更新
-- MySQL 8.0 / InnoDB / utf8mb4 ; 可重复执行(先删后建)
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `bidding`
  DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_general_ci;
USE `bidding`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `operation_log`;
DROP TABLE IF EXISTS `search_history`;
DROP TABLE IF EXISTS `search_hot_keyword`;
DROP TABLE IF EXISTS `invoice`;
DROP TABLE IF EXISTS `guarantee`;
DROP TABLE IF EXISTS `article`;
DROP TABLE IF EXISTS `article_category`;
DROP TABLE IF EXISTS `complaint`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `attachment`;
DROP TABLE IF EXISTS `award`;
DROP TABLE IF EXISTS `report_doc`;
DROP TABLE IF EXISTS `evaluation_report`;
DROP TABLE IF EXISTS `second_round_quote`;
DROP TABLE IF EXISTS `negotiation`;
DROP TABLE IF EXISTS `evaluation_score`;
DROP TABLE IF EXISTS `evaluation_item`;
DROP TABLE IF EXISTS `expert_assignment`;
DROP TABLE IF EXISTS `payment_order_item`;
DROP TABLE IF EXISTS `payment_order`;
DROP TABLE IF EXISTS `bid_document`;
DROP TABLE IF EXISTS `bid_registration`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `tender_project`;
DROP TABLE IF EXISTS `tenderer_invite`;
DROP TABLE IF EXISTS `role_application`;
DROP TABLE IF EXISTS `user_preference`;
DROP TABLE IF EXISTS `agency_company`;
DROP TABLE IF EXISTS `expert_profile`;
DROP TABLE IF EXISTS `supplier_profile`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_role`;
DROP TABLE IF EXISTS `sys_user`;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- 1. 用户、角色与组织
-- =====================================================================

CREATE TABLE `sys_user` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(64)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(128) NULL COMMENT 'BCrypt密码,微信注册可空',
  `real_name`   VARCHAR(64)  NULL,
  `phone`       VARCHAR(20)  NULL,
  `email`       VARCHAR(128) NULL,
  `wx_openid`   VARCHAR(64)  NULL,
  `wx_unionid`  VARCHAR(64)  NULL,
  `avatar`      VARCHAR(255) NULL,
  `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '0禁用 1正常',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_openid` (`wx_openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

CREATE TABLE `sys_role` (
  `id`   BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL COMMENT 'GUEST/BIDDER/TENDERER/MANAGER/EXPERT/SALES/ADMIN',
  `name` VARCHAR(32) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色';

CREATE TABLE `sys_user_role` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT NOT NULL,
  `role_id`      BIGINT NOT NULL,
  `audit_status` TINYINT NOT NULL DEFAULT 1 COMMENT '0待审核 1已通过(专家/经理角色需审核)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关系';

CREATE TABLE `supplier_profile` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT NOT NULL,
  `company_name`    VARCHAR(128) NOT NULL,
  `credit_code`     VARCHAR(32)  NOT NULL COMMENT '统一社会信用代码',
  `legal_person`    VARCHAR(64)  NULL,
  `address`         VARCHAR(255) NULL,
  `contact_name`    VARCHAR(64)  NULL,
  `contact_phone`   VARCHAR(20)  NULL,
  `bank_name`       VARCHAR(128) NULL COMMENT '收款银行',
  `bank_account`    VARCHAR(64)  NULL COMMENT '账户号',
  `license_file_id` BIGINT       NULL,
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
  `expert_no`   VARCHAR(32)  NULL,
  `major`       VARCHAR(128) NULL COMMENT '专业领域',
  `org`         VARCHAR(128) NULL COMMENT '所在单位',
  `title`       VARCHAR(64)  NULL COMMENT '职称',
  `id_card`     VARCHAR(32)  NULL,
  `sign_img_id` BIGINT       NULL COMMENT '电子签名图附件id',
  `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '0停用 1正常',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专家资料';

CREATE TABLE `agency_company` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(128) NOT NULL COMMENT '代理公司名称',
  `credit_code` VARCHAR(32)  NULL,
  `address`     VARCHAR(255) NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代理公司';

CREATE TABLE `user_preference` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT NOT NULL,
  `industries` VARCHAR(255) NULL COMMENT '关注行业(逗号分隔)',
  `regions`    VARCHAR(255) NULL COMMENT '关注地区',
  `types`      VARCHAR(255) NULL COMMENT '关注招标类型',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='我的偏好(商机推荐)';

CREATE TABLE `role_application` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT NOT NULL,
  `apply_role`   VARCHAR(32) NOT NULL COMMENT 'MANAGER/EXPERT',
  `major`        VARCHAR(128) NULL COMMENT '专家专业',
  `attach_id`    BIGINT NULL COMMENT '证明材料',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色申请(项目经理/专家)';

CREATE TABLE `tenderer_invite` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `inviter_id`      BIGINT NOT NULL COMMENT '邀请人(项目经理)',
  `invitee_user_id` BIGINT NULL,
  `invitee_name`    VARCHAR(64) NULL,
  `invitee_phone`   VARCHAR(20) NULL,
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0待接收 1已接收 2已拒绝',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_inviter` (`inviter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请招标人';

-- =====================================================================
-- 2. 招标项目
-- =====================================================================

CREATE TABLE `tender_project` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_no`     VARCHAR(40)  NOT NULL,
  `name`           VARCHAR(200) NOT NULL,
  `section`        VARCHAR(100) NULL COMMENT '标段',
  `procurement_type` VARCHAR(20) NOT NULL COMMENT 'PUBLIC/INVITE/INQUIRY/SINGLE/NEGOTIATION/CONSULTATION',
  `tender_type`    VARCHAR(16)  NULL COMMENT 'ENGINEER工程/GOODS货物/SERVICE服务',
  `industry`       VARCHAR(32)  NULL,
  `region`         VARCHAR(64)  NULL,
  `budget`         DECIMAL(15,2) NULL,
  `manager_id`     BIGINT NOT NULL COMMENT '项目经理user_id',
  `tenderer_id`    BIGINT NULL COMMENT '招标人user_id',
  `agency_id`      BIGINT NULL COMMENT '代理公司id',
  `file_fee`       DECIMAL(15,2) NULL COMMENT '文件费',
  `platform_fee`   DECIMAL(15,2) NULL COMMENT '平台使用费',
  `status`         VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
                   COMMENT 'DRAFT/BIDDING/OPENING/OPENED/AWARDED/FINISHED/ARCHIVED/ABORTED',
  `eval_node`      VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED'
                   COMMENT 'NOT_STARTED/REVIEWING/NEGOTIATING/SECOND_QUOTE/FINISHED',
  `reg_start`      DATETIME NULL,
  `reg_end`        DATETIME NULL,
  `bid_open_time`  DATETIME NULL COMMENT '开标时间',
  `eval_total_score` DECIMAL(8,2) NULL COMMENT '评审总分',
  `price_score_method` TINYINT NULL COMMENT '报价评分方式1/2/3',
  `archived`       TINYINT NOT NULL DEFAULT 0,
  `content`        MEDIUMTEXT NULL COMMENT '招标公告正文',
  `bid_file_id`    BIGINT NULL COMMENT '招标文件附件',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_no` (`project_no`),
  KEY `idx_status` (`status`),
  KEY `idx_manager` (`manager_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招标项目';

CREATE TABLE `announcement` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`   BIGINT NOT NULL,
  `type`         TINYINT NOT NULL COMMENT '1招标公告 2中标公示 3中标结果/通知书',
  `title`        VARCHAR(200) NULL,
  `content`      MEDIUMTEXT NULL,
  `attach_id`    BIGINT NULL,
  `publish_time` DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project_type` (`project_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告(招标/中标公示/通知书)';

-- =====================================================================
-- 3. 报名与缴费
-- =====================================================================

CREATE TABLE `bid_registration` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`    BIGINT NOT NULL,
  `supplier_id`   BIGINT NOT NULL COMMENT '投标人user_id',
  `company_name`  VARCHAR(128) NULL,
  `contact_name`  VARCHAR(64)  NULL,
  `contact_phone` VARCHAR(20)  NULL,
  `apply_file_id` BIGINT NULL COMMENT '报名文件',
  `audit_status`  TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1通过 2驳回',
  `audit_remark`  VARCHAR(255) NULL COMMENT '驳回原因',
  `reg_status`    VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                  COMMENT 'PENDING/UNPAID/SUCCESS/REJECTED/CANCELLED',
  `bid_status`    VARCHAR(20) NOT NULL DEFAULT 'NONE'
                  COMMENT 'NONE/BIDDING/OPENED/WON/LOST/INVALID',
  `reg_time`      DATETIME NULL,
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
  `attach_id`       BIGINT NOT NULL COMMENT '投标文件附件',
  `bid_price`       DECIMAL(15,2) NULL COMMENT '投标报价',
  `duration`        VARCHAR(64) NULL COMMENT '工期',
  `encrypt_pwd`     VARCHAR(128) NULL COMMENT '六位解密密码(加密存储)',
  `encrypted`       TINYINT NOT NULL DEFAULT 0,
  `decrypt_status`  TINYINT NOT NULL DEFAULT 0 COMMENT '0未解密 1已解密',
  `decrypt_time`    DATETIME NULL,
  `sign_img_id`     BIGINT NULL COMMENT '签字解密签名图',
  `submit_time`     DATETIME NULL,
  `withdraw_time`   DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_reg` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='投标文件(加密/撤回/解密)';

CREATE TABLE `payment_order` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no`        VARCHAR(40) NOT NULL,
  `project_id`      BIGINT NOT NULL,
  `registration_id` BIGINT NULL,
  `payer_id`        BIGINT NOT NULL,
  `biz_type`        VARCHAR(20) NOT NULL COMMENT 'REGISTER报名缴费/AGENCY代理费',
  `total_amount`    DECIMAL(15,2) NOT NULL,
  `pay_channel`     VARCHAR(16) NULL COMMENT 'WECHAT/ALIPAY/TRANSFER',
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',
  `pay_time`        DATETIME NULL,
  `fee_mode`        TINYINT NULL COMMENT '代理费:1固定 2标准(折扣)',
  `discount`        DECIMAL(5,2) NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order` (`order_no`),
  KEY `idx_reg` (`registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费订单(报名缴费/代理费)';

CREATE TABLE `payment_order_item` (
  `id`       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_id` BIGINT NOT NULL,
  `fee_type` VARCHAR(16) NOT NULL COMMENT 'FILE文件费/PLATFORM平台使用费/AGENCY代理费',
  `amount`   DECIMAL(15,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='缴费订单明细';

-- =====================================================================
-- 4. 开评标
-- =====================================================================

CREATE TABLE `expert_assignment` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`   BIGINT NOT NULL,
  `expert_id`    BIGINT NOT NULL,
  `is_leader`    TINYINT NOT NULL DEFAULT 0 COMMENT '是否评标组长',
  `eval_period`  VARCHAR(10) NULL COMMENT 'AM/PM/ALL',
  `report_place` VARCHAR(128) NULL COMMENT '报到地点',
  `draw_type`    TINYINT NULL COMMENT '1邀请 2随机抽取',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评标专家抽取/邀请';

CREATE TABLE `evaluation_item` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id` BIGINT NOT NULL,
  `type`       VARCHAR(20) NOT NULL
               COMMENT 'FORMAL/QUALIFY/RESPONSE/COMMERCE/TECH/PRICE',
  `name`       VARCHAR(128) NULL,
  `max_score`  DECIMAL(6,2) NULL,
  `sub_total`  DECIMAL(6,2) NULL COMMENT '该类总分',
  `sort`       INT NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评审项配置(六类评审)';

CREATE TABLE `evaluation_score` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`      BIGINT NOT NULL,
  `registration_id` BIGINT NOT NULL COMMENT '被评投标人',
  `expert_id`       BIGINT NOT NULL,
  `item_id`         BIGINT NOT NULL,
  `pass`            TINYINT NULL COMMENT '形式/资格/响应性:0否1是',
  `score`           DECIMAL(6,2) NULL COMMENT '商务/技术/报价评分',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分(专家×投标人×评审项)';

CREATE TABLE `negotiation` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`      BIGINT NOT NULL,
  `registration_id` BIGINT NOT NULL,
  `initiator_id`    BIGINT NOT NULL COMMENT '发起专家(组长)',
  `content`         TEXT NULL,
  `attach_id`       BIGINT NULL,
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0未回复 1已回复',
  `reply_content`   TEXT NULL,
  `reply_attach_id` BIGINT NULL,
  `reply_time`      DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_proj_reg` (`project_id`, `registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='谈判磋商';

CREATE TABLE `second_round_quote` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`      BIGINT NOT NULL,
  `registration_id` BIGINT NOT NULL,
  `initiator_id`    BIGINT NOT NULL COMMENT '发起专家(组长)',
  `content`         TEXT NULL,
  `status`          TINYINT NOT NULL DEFAULT 0 COMMENT '0未回复 1已回复',
  `reply_price`     DECIMAL(15,2) NULL,
  `reply_duration`  VARCHAR(64) NULL,
  `reply_attach_id` BIGINT NULL,
  `reply_time`      DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_proj_reg` (`project_id`, `registration_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='二轮报价';

CREATE TABLE `evaluation_report` (
  `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`       BIGINT NOT NULL,
  `total_docs`       INT NOT NULL DEFAULT 0,
  `generated_docs`   INT NOT NULL DEFAULT 0,
  `status`           TINYINT NOT NULL DEFAULT 0 COMMENT '0未出 1已完成',
  `purchase_content` TEXT NULL COMMENT '采购内容',
  `reject_note`      TEXT NULL COMMENT '否决情况说明',
  `clarify_note`     TEXT NULL COMMENT '澄清说明',
  `candidate_list`   TEXT NULL COMMENT '中标候选人名单',
  `export_attach_id` BIGINT NULL COMMENT '一键导出文件',
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评标报告';

CREATE TABLE `report_doc` (
  `id`        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `report_id` BIGINT NOT NULL,
  `doc_name`  VARCHAR(128) NULL,
  `status`    VARCHAR(16) NOT NULL DEFAULT 'NOT_GEN' COMMENT 'NOT_GEN/PENDING_SIGN/DONE',
  `attach_id` BIGINT NULL,
  `signed_by` BIGINT NULL,
  `sign_time` DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_report` (`report_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评标报告文档项';

CREATE TABLE `award` (
  `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `project_id`      BIGINT NOT NULL,
  `registration_id` BIGINT NOT NULL COMMENT '中标投标人',
  `rank`            INT NULL COMMENT '排名(1=第一)',
  `final_score`     DECIMAL(8,2) NULL,
  `final_price`     DECIMAL(15,2) NULL COMMENT '最终报价',
  `is_winner`       TINYINT NOT NULL DEFAULT 0,
  `publicity_start` DATETIME NULL,
  `publicity_end`   DATETIME NULL,
  `agency_fee`      DECIMAL(15,2) NULL COMMENT '应缴代理费',
  `agency_fee_paid` TINYINT NOT NULL DEFAULT 0,
  `notice_attach_id` BIGINT NULL COMMENT '中标通知书',
  `notice_publish_time` DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='中标候选人/定标/通知书';

-- =====================================================================
-- 5. 通用与内容
-- =====================================================================

CREATE TABLE `attachment` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `file_name`    VARCHAR(255) NULL,
  `file_key`     VARCHAR(255) NULL COMMENT '对象存储key',
  `file_size`    BIGINT NULL,
  `content_type` VARCHAR(128) NULL,
  `biz_type`     VARCHAR(32) NULL COMMENT 'bid_doc/apply/license/article/report',
  `create_by`    BIGINT NULL,
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`      TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件元数据';

CREATE TABLE `notification` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `type`        VARCHAR(32) NULL COMMENT 'AUDIT/INVITE/OPEN/NEGOTIATION/AWARD',
  `title`       VARCHAR(128) NULL,
  `content`     VARCHAR(500) NULL,
  `biz_id`      BIGINT NULL,
  `is_read`     TINYINT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知';

CREATE TABLE `complaint` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NULL,
  `project_id`  BIGINT NULL,
  `category`    VARCHAR(16) NULL COMMENT 'ADVICE意见/COMPLAINT投诉',
  `sub_type`    VARCHAR(32) NULL COMMENT '平台优化建议/平台报错问题/招标信息质疑/招标文件质疑/投标投诉质疑/中标投诉质疑',
  `title`       VARCHAR(200) NULL,
  `content`     TEXT NULL,
  `attach_id`   BIGINT NULL,
  `status`      TINYINT NOT NULL DEFAULT 0 COMMENT '0待处理 1处理中 2已回复',
  `reply`       TEXT NULL,
  `handler`     BIGINT NULL,
  `handle_time` DATETIME NULL,
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
  `code` VARCHAR(32) NOT NULL COMMENT 'guide/policy/case/download/news',
  `name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容栏目';

CREATE TABLE `article` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `category_id`  BIGINT NOT NULL,
  `title`        VARCHAR(200) NULL,
  `content`      MEDIUMTEXT NULL,
  `attach_id`    BIGINT NULL,
  `publish_time` DATETIME NULL,
  `create_by`   BIGINT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_by`   BIGINT NULL,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务指南/政策法规/案例解析/下载专区/信息动态';

CREATE TABLE `search_hot_keyword` (
  `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `keyword`      VARCHAR(64) NOT NULL,
  `search_count` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热门搜索统计';

CREATE TABLE `search_history` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT NOT NULL,
  `keyword`     VARCHAR(64) NOT NULL,
  `search_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `search_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户历史搜索';

-- 待补充模块占位表（需求文档标注待补充）
CREATE TABLE `guarantee` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`    BIGINT NOT NULL,
  `project_id` BIGINT NULL,
  `amount`     DECIMAL(15,2) NULL,
  `status`     TINYINT NOT NULL DEFAULT 0 COMMENT '保函状态(待补充)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`    TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='保函(待补充)';

CREATE TABLE `invoice` (
  `id`       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`  BIGINT NOT NULL,
  `order_id` BIGINT NULL,
  `amount`   DECIMAL(15,2) NULL,
  `title`    VARCHAR(128) NULL COMMENT '发票抬头',
  `status`   TINYINT NOT NULL DEFAULT 0 COMMENT '0申请中 1已开 2驳回',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`  TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发票申请(待补充)';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

-- =====================================================================
-- 6. 初始化数据
-- =====================================================================

INSERT INTO `sys_role` (`code`, `name`) VALUES
  ('GUEST',    '游客'),
  ('BIDDER',   '投标人'),
  ('TENDERER', '招标人'),
  ('MANAGER',  '项目经理'),
  ('EXPERT',   '评标专家'),
  ('SALES',    '业务员'),
  ('ADMIN',    '平台管理员');

INSERT INTO `article_category` (`code`, `name`) VALUES
  ('guide',    '业务指南'),
  ('policy',   '政策法规'),
  ('case',     '案例解析'),
  ('download', '下载专区'),
  ('news',     '信息动态');

INSERT INTO `sys_user` (`username`, `password`, `real_name`, `status`)
  VALUES ('admin', '$2a$10$REPLACE_WITH_BCRYPT_HASH', '系统管理员', 1);
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `audit_status`)
  SELECT u.id, r.id, 1 FROM `sys_user` u, `sys_role` r
  WHERE u.username = 'admin' AND r.code = 'ADMIN';
