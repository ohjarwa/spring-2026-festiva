-- =============================================
-- 添加用户素材表
-- =============================================

USE `spring_2026_festival`;

-- 创建用户素材表
DROP TABLE IF EXISTS `spring_2026_user_material`;
CREATE TABLE `spring_2026_user_material` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `material_id` varchar(64) NOT NULL COMMENT '素材唯一标识',
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `material_type` varchar(32) NOT NULL COMMENT '素材类型: photo/audio',
  `file_url` varchar(512) NOT NULL COMMENT '文件URL',
  `original_filename` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint(20) DEFAULT NULL COMMENT '文件大小(字节)',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态: 1=正常 0=删除',
  `upload_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_id` (`material_id`),
  KEY `idx_user_type` (`user_id`, `material_type`),
  KEY `idx_user_id` (`user_id`, `upload_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户素材表';