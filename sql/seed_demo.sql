-- Demo data for home/search/content APIs (idempotent-ish for local dev)
INSERT INTO `tender_project` (`project_no`, `name`, `procurement_type`, `tender_type`, `industry`, `region`, `budget`, `manager_id`, `status`, `reg_start`, `reg_end`, `bid_open_time`, `file_fee`, `platform_fee`, `content`)
SELECT 'TP2026001', '城市通智慧停车系统采购项目', 'PUBLIC', 'GOODS', '信息化', '北京市', 500000.00, 1, 'BIDDING', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 300.00, 300.00, '本项目采购智慧停车管理平台及配套硬件。'
WHERE NOT EXISTS (SELECT 1 FROM `tender_project` WHERE `project_no` = 'TP2026001');

INSERT INTO `tender_project` (`project_no`, `name`, `procurement_type`, `tender_type`, `industry`, `region`, `budget`, `manager_id`, `status`, `bid_open_time`, `content`)
SELECT 'TP2026002', '市政道路养护工程', 'PUBLIC', 'ENGINEER', '市政', '上海市', 1200000.00, 1, 'AWARDED', DATE_SUB(NOW(), INTERVAL 5 DAY), '市政道路日常养护工程招标。'
WHERE NOT EXISTS (SELECT 1 FROM `tender_project` WHERE `project_no` = 'TP2026002');

INSERT INTO `announcement` (`project_id`, `type`, `title`, `content`, `publish_time`)
SELECT p.id, 1, CONCAT(p.name, '招标公告'), p.content, NOW()
FROM `tender_project` p
WHERE p.project_no = 'TP2026001'
  AND NOT EXISTS (SELECT 1 FROM `announcement` a WHERE a.project_id = p.id AND a.type = 1);

INSERT INTO `announcement` (`project_id`, `type`, `title`, `content`, `publish_time`)
SELECT p.id, 2, CONCAT(p.name, '中标公示'), '中标单位：观点科技有限公司', DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM `tender_project` p
WHERE p.project_no = 'TP2026002'
  AND NOT EXISTS (SELECT 1 FROM `announcement` a WHERE a.project_id = p.id AND a.type = 2);

INSERT INTO `bid_registration` (`project_id`, `supplier_id`, `company_name`, `audit_status`, `reg_status`, `bid_status`, `reg_time`)
SELECT p.id, 2, '观点科技有限公司', 1, 'SUCCESS', 'WON', NOW()
FROM `tender_project` p
WHERE p.project_no = 'TP2026002'
  AND NOT EXISTS (SELECT 1 FROM `bid_registration` r WHERE r.project_id = p.id AND r.supplier_id = 2);

INSERT INTO `award` (`project_id`, `registration_id`, `rank`, `final_price`, `is_winner`, `publicity_start`, `publicity_end`, `notice_publish_time`)
SELECT p.id, r.id, 1, 1150000.00, 1, DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)
FROM `tender_project` p
JOIN `bid_registration` r ON r.project_id = p.id AND r.supplier_id = 2
WHERE p.project_no = 'TP2026002'
  AND NOT EXISTS (SELECT 1 FROM `award` a WHERE a.project_id = p.id AND a.is_winner = 1);

INSERT INTO `article` (`category_id`, `title`, `content`, `publish_time`)
SELECT c.id, '投标人操作指南', '本文介绍投标人注册、报名、递交投标文件流程。', NOW()
FROM `article_category` c
WHERE c.code = 'guide'
  AND NOT EXISTS (SELECT 1 FROM `article` a WHERE a.title = '投标人操作指南');

INSERT INTO `search_hot_keyword` (`keyword`, `search_count`)
SELECT '智慧停车', 10
WHERE NOT EXISTS (SELECT 1 FROM `search_hot_keyword` WHERE `keyword` = '智慧停车');
