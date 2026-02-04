-- =============================================
-- 春节2026 AI视频生成 - 数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `spring_2026_festival` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `spring_2026_festival`;

-- =============================================
-- 1. 模板表
-- =============================================
DROP TABLE IF EXISTS `spring_2026_template`;
CREATE TABLE `spring_2026_template` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `template_id` varchar(64) NOT NULL COMMENT '模板唯一标识',
  `activity_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '活动类型',

  -- 基本信息
  `name` varchar(64) NOT NULL COMMENT '模板名称',
  `cover_url` varchar(255) NOT NULL COMMENT '封面图URL',
  `template_url` varchar(255) NOT NULL COMMENT '模板资源URL',

  -- 任务流程配置（JSON）
  `task_config` text COMMENT '任务配置',

  -- 统计
  `used_times` int(11) DEFAULT '0' COMMENT '使用次数',

  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_id` (`template_id`),
  KEY `idx_activity_type` (`activity_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='春节活动模板表';

-- =============================================
-- 2. 用户创作记录表
-- =============================================
DROP TABLE IF EXISTS `spring_2026_creation_record`;
CREATE TABLE `spring_2026_creation_record` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `record_id` varchar(64) NOT NULL COMMENT '记录唯一标识',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `activity_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '活动类型',

  -- 模板信息
  `template_id` varchar(64) NOT NULL COMMENT '模板ID',

  -- 用户素材（JSON格式）
  `user_materials` text NOT NULL COMMENT '用户上传的素材',

  -- 整体任务状态
  `status` tinyint(4) DEFAULT '0' COMMENT '0=排队 1=生成中 2=已完成 3=失败',
  `progress` int(11) DEFAULT '0' COMMENT '整体进度 0-100',

  -- 任务执行详情（JSON格式）
  `task_execution` text COMMENT '任务执行详情',

  -- 最终结果
  `result_url` varchar(255) DEFAULT NULL COMMENT '最终视频URL',
  `result_thumbnail_url` varchar(255) DEFAULT NULL COMMENT '缩略图',
  `result_duration` decimal(10,2) DEFAULT NULL COMMENT '时长(秒)',
  `result_file_size` bigint(20) DEFAULT NULL COMMENT '文件大小',

  -- 错误信息
  `error_info` text COMMENT '错误信息(JSON)',

  -- 审核信息
  `audit_info` text COMMENT '审核信息(JSON)',

  -- 重试控制
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `max_retry` int(11) DEFAULT '3' COMMENT '最大重试次数',

  -- 时间
  `start_time` timestamp NULL DEFAULT NULL COMMENT '开始生成时间',
  `complete_time` timestamp NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  -- 扩展字段
  `extra_data` text COMMENT '扩展数据',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_id` (`record_id`),
  KEY `idx_user_id` (`user_id`, `create_time`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户创作记录表';

-- =============================================
-- 3. 用户信息表
-- =============================================
DROP TABLE IF EXISTS `spring_2026_user`;
CREATE TABLE `spring_2026_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `activity_type` tinyint(4) NOT NULL DEFAULT '1' COMMENT '活动类型',
  `source` varchar(32) NOT NULL DEFAULT 'in_app' COMMENT '来源',

  -- 配额
  `total_quota` int(11) NOT NULL COMMENT '总配额',
  `used_quota` int(11) DEFAULT '0' COMMENT '已使用配额',
  `remaining_quota` int(11) NOT NULL COMMENT '剩余配额',
  `can_retry` tinyint(4) DEFAULT '1' COMMENT '能否重试',

  -- 账号状态
  `account_status` tinyint(4) DEFAULT '1' COMMENT '账号状态 1=正常 0=禁用',
  `can_upload` tinyint(4) DEFAULT '1' COMMENT '能否上传',
  `can_create_video` tinyint(4) DEFAULT '1' COMMENT '能否创建视频',

  -- 限制信息
  `restriction_reason` varchar(255) DEFAULT NULL COMMENT '限制原因',
  `restriction_end_time` timestamp NULL DEFAULT NULL COMMENT '限制结束时间',
  `ban_reason` varchar(255) DEFAULT NULL COMMENT '封禁原因',
  `ban_end_time` timestamp NULL DEFAULT NULL COMMENT '封禁结束时间',

  -- 统计
  `violation_count` int(11) DEFAULT '0' COMMENT '违规次数',
  `success_count` int(11) DEFAULT '0' COMMENT '成功次数',
  `failed_count` int(11) DEFAULT '0' COMMENT '失败次数',
  `last_use_time` timestamp NULL DEFAULT NULL COMMENT '最后使用时间',

  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  `extra_data` text COMMENT '扩展数据',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_activity` (`user_id`, `activity_type`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- =============================================
-- 初始化测试数据
-- =============================================

-- 插入测试模板
INSERT INTO `spring_2026_template` (`template_id`, `activity_type`, `name`, `cover_url`, `template_url`, `task_config`, `used_times`)
VALUES
('tpl_001', 1, '新春祝福', 'https://cdn.example.com/covers/tpl_001.jpg', 'https://cdn.example.com/templates/tpl_001.mp4', '{"steps":["face_swap","voice_lipsync"],"estimated_time":120}', 0),
('tpl_002', 1, '拜年视频', 'https://cdn.example.com/covers/tpl_002.jpg', 'https://cdn.example.com/templates/tpl_002.mp4', '{"steps":["face_detect","background_remove","face_swap","voice_clone","lipsync","effects"],"estimated_time":180}', 0),
('tpl_003', 1, '简单祝福', 'https://cdn.example.com/covers/tpl_003.jpg', 'https://cdn.example.com/templates/tpl_003.mp4', '{"steps":["direct_composite"],"estimated_time":60}', 0);

-- 插入测试用户
INSERT INTO `spring_2026_user` (`user_id`, `activity_type`, `source`, `total_quota`, `used_quota`, `remaining_quota`)
VALUES
('test_user_001', 1, 'in_app', 10, 0, 10),
('test_user_002', 1, 'in_app', 10, 3, 7);