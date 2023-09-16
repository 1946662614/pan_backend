/*
 Navicat Premium Data Transfer

 Source Server         : a
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : localhost:3306
 Source Schema         : easypan

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 16/09/2023 19:18:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for email_code
-- ----------------------------
DROP TABLE IF EXISTS `email_code`;
CREATE TABLE `email_code`  (
  `email` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `code` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编号 ',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0: 未使用 1: 已使用',
  PRIMARY KEY (`email`, `code`) USING BTREE,
  UNIQUE INDEX `email_code_pk`(`code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '邮箱验证码' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of email_code
-- ----------------------------
INSERT INTO `email_code` VALUES ('18359389968@163.com', '27449', '2023-08-25 11:24:26', 1);
INSERT INTO `email_code` VALUES ('18359389968@163.com', '80121', '2023-08-23 17:29:39', 1);
INSERT INTO `email_code` VALUES ('1946662614@qq.com', '21278', '2023-08-25 11:15:06', 0);

-- ----------------------------
-- Table structure for file_info
-- ----------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info`  (
  `file_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件ID',
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `file_md5` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件MD5值',
  `file_pid` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父级ID',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  `file_cover` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件封面',
  `file_path` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件路径',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最新更新时间',
  `folder_type` tinyint(1) NULL DEFAULT NULL COMMENT '0: 文件 1: 目录',
  `file_category` tinyint(1) NULL DEFAULT NULL COMMENT '文件分类 1:视频 2:音频 3:图片 4:文档 5:其他 ',
  `file_type` tinyint(1) NULL DEFAULT NULL COMMENT '1:视频 2:音频 3:图片 4:pdf 5:doc 6:excel 7:txt 8:code 9:zip 10:其他',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0:转码中 1:转码失败 2:转码成功',
  `recovery_time` datetime NULL DEFAULT NULL COMMENT '进入回收站时间',
  `del_flag` tinyint(1) NULL DEFAULT NULL COMMENT '标记删除： 0:删除 1:回收站 2:正常 ',
  PRIMARY KEY (`file_id`, `user_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_md5`(`file_md5` ASC) USING BTREE,
  INDEX `idx_file_pid`(`file_pid` ASC) USING BTREE,
  INDEX `idx_del_flag`(`del_flag` ASC) USING BTREE,
  INDEX `idx_recovery_time`(`recovery_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_info
-- ----------------------------
INSERT INTO `file_info` VALUES ('1aEDI28rJW', '8343491440', '051454faa9fbcb53808c7823cc4f1188', 'Tun28R7mQU', 8591102, 'TG-2023-06-11-000657225.mp4', '202309/8343491441VgmxayuFyA.jpg', '202309/8343491441VgmxayuFyA.mp4', '2023-09-16 17:44:40', '2023-09-16 17:44:40', 0, 1, 1, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('8r2m0EDXaF', '8343491441', 'cfacc3995753499d415eb6c67f8dad65', '48oUNApFfn', 98983, '93d1603bly1gg90qnuga2j20u01bghdt.jpg', '202309/83434914418r2m0EDXaF_.jpg', '202309/83434914418r2m0EDXaF.jpg', '2023-09-14 14:02:52', '2023-09-14 14:02:52', 0, 3, 3, 2, NULL, 0);
INSERT INTO `file_info` VALUES ('bltng8cacJ', '8343491441', 'cff4de259b11f01d0c17d8e824501c99', 'E9WGVKaAHP', 580, '账号密码.txt', NULL, '202309/8343491441bltng8cacJ.txt', '2023-09-12 14:44:51', '2023-09-12 14:44:51', 0, 4, 7, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('ChCePvQa9c', '8343491441', '02e2a623c95c757761ac4383b98c2139', '0', 24836864, 'DSC05741.ARW', NULL, '202309/8343491441ChCePvQa9c.ARW', '2023-09-14 14:26:14', '2023-09-14 14:26:14', 0, 5, 10, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('E9WGVKaAHP', '8343491441', NULL, '0', NULL, '我的文档02', NULL, NULL, '2023-09-14 14:07:00', '2023-09-14 14:07:00', 1, NULL, NULL, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('icYAy2tDAX', '8343491440', 'cff4de259b11f01d0c17d8e824501c99', 'Tun28R7mQU', 580, '账号密码.txt', NULL, '202309/8343491441bltng8cacJ.txt', '2023-09-16 17:44:40', '2023-09-16 17:44:40', 0, 4, 7, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('JfytFMWObt', '8343491441', '4a0dd366bd016ec0d92a39a9d14140ec', '0', 1462227, 'the-paris-photographer-wckmGjiX_pY-unsplash (2).jpg', '202309/8343491441JfytFMWObt_.jpg', '202309/8343491441JfytFMWObt.jpg', '2023-09-14 10:53:23', '2023-09-14 11:30:32', 0, 3, 3, 2, '2023-09-14 10:53:39', 2);
INSERT INTO `file_info` VALUES ('jjIm9Z4hKX', '8343491441', NULL, '0', NULL, '我的文档', NULL, NULL, '2023-09-13 09:44:48', '2023-09-13 09:44:48', 1, NULL, NULL, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('KIdHf0U4Gv', '8343491440', '4a0dd366bd016ec0d92a39a9d14140ec', '0', 1462227, 'the-paris-photographer-wckmGjiX_pY-unsplash (2).jpg', '202309/8343491441JfytFMWObt_.jpg', '202309/8343491441JfytFMWObt.jpg', '2023-09-16 18:03:50', '2023-09-16 18:03:50', 0, 3, 3, 2, '2023-09-14 10:53:39', 2);
INSERT INTO `file_info` VALUES ('m9VRx99Ifu', '8343491441', '7962a5613613f6077d21aa10e5679c3d', 'jjIm9Z4hKX', 14657, '综测计算流程.docx', NULL, '202309/8343491441m9VRx99Ifu.docx', '2023-09-12 14:50:36', '2023-09-13 10:38:16', 0, 4, 5, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('MS4IBMXpwP', '8343491441', 'b48d2e116b01c69f61609f268dfe387d', '0', 6452, '简历投递汇总.md', NULL, '202309/8343491441MS4IBMXpwP.md', '2023-09-12 14:50:23', '2023-09-12 14:50:23', 0, 5, 8, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('QSAJayU9vz', '8343491441', NULL, 'VM1iv9U3u9', NULL, '001', NULL, NULL, '2023-09-14 10:19:00', '2023-09-14 10:19:00', 1, NULL, NULL, 2, '2023-09-14 10:19:39', 0);
INSERT INTO `file_info` VALUES ('rmtnqGXweP', '8343491441', '7962a5613613f6077d21aa10e5679c3d', 'QSAJayU9vz', 14657, '综测计算流程_92935.docx', NULL, '202309/8343491441m9VRx99Ifu.docx', '2023-09-13 11:27:53', '2023-09-13 11:28:00', 0, 4, 5, 2, '2023-09-14 10:19:39', 0);
INSERT INTO `file_info` VALUES ('Tun28R7mQU', '8343491440', NULL, 'jjIm9Z4hKX', NULL, '我的文档02', NULL, NULL, '2023-09-16 17:44:40', '2023-09-16 17:44:40', 1, NULL, NULL, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('UPsPlKTx05', '8343491441', 'd3027054224ec80512ec606b254f8da2', '0', 13838060, 'DSC05937.jpg', '202309/8343491441UPsPlKTx05_.jpg', '202309/8343491441UPsPlKTx05.jpg', '2023-09-14 14:27:07', '2023-09-14 14:27:07', 0, 3, 3, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('VgmxayuFyA', '8343491441', '051454faa9fbcb53808c7823cc4f1188', 'E9WGVKaAHP', 8591102, 'TG-2023-06-11-000657225.mp4', '202309/8343491441VgmxayuFyA.jpg', '202309/8343491441VgmxayuFyA.mp4', '2023-09-12 14:38:34', '2023-09-12 14:38:34', 0, 1, 1, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('VM1iv9U3u9', '8343491441', NULL, '0', NULL, '我的资源', NULL, NULL, '2023-09-13 09:44:12', '2023-09-13 09:44:12', 1, NULL, NULL, 2, '2023-09-14 10:23:44', 1);
INSERT INTO `file_info` VALUES ('xyCteCeDIp', '8343491441', 'ec48bae05d65f8c40bb4e057b594304e', 'E9WGVKaAHP', 53644, 'EC48BAE05D65F8C40BB4E057B594304E.jpg', '202308/8343491441xyCteCeDIp_.jpg', '202308/8343491441xyCteCeDIp.jpg', '2023-08-30 15:22:43', '2023-08-30 15:22:43', 0, 3, 3, 2, NULL, 2);
INSERT INTO `file_info` VALUES ('Xz7YcTiW11', '8343491440', 'ec48bae05d65f8c40bb4e057b594304e', 'Tun28R7mQU', 53644, 'EC48BAE05D65F8C40BB4E057B594304E.jpg', '202308/8343491441xyCteCeDIp_.jpg', '202308/8343491441xyCteCeDIp.jpg', '2023-09-16 17:44:40', '2023-09-16 17:44:40', 0, 3, 3, 2, NULL, 2);

-- ----------------------------
-- Table structure for file_share
-- ----------------------------
DROP TABLE IF EXISTS `file_share`;
CREATE TABLE `file_share`  (
  `share_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分享id',
  `file_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件id',
  `user_id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户id',
  `valid_type` tinyint(1) NULL DEFAULT NULL COMMENT '有效期类型 0:1天 1:七天 2:30天 3:永久有效',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
  `share_time` datetime NULL DEFAULT NULL COMMENT '分享时间',
  `code` varchar(5) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '提取码',
  `show_count` int NULL DEFAULT 0 COMMENT '查看次数',
  PRIMARY KEY (`share_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '分享信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of file_share
-- ----------------------------
INSERT INTO `file_share` VALUES ('cwYwQ1LKz3OZ7IO2FhVO', 'jjIm9Z4hKX', '8343491441', 2, '2023-10-16 15:37:54', '2023-09-16 15:37:54', 'jf75f', 0);
INSERT INTO `file_share` VALUES ('GcWq4qxou0eqwtLN1oqk', 'jjIm9Z4hKX', '8343491441', 2, '2023-10-16 15:39:37', '2023-09-16 15:39:37', 'lm9EM', 2);
INSERT INTO `file_share` VALUES ('IiZKGLUSAIN7FOj1PqbN', 'E9WGVKaAHP', '8343491441', 2, '2023-10-16 17:38:22', '2023-09-16 17:38:22', 'iL32F', 1);

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `user_id` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户id',
  `nick_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `email` varchar(155) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `qq_open_id` varchar(35) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'qqOpenId',
  `qq_avatar` varchar(150) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'qq头像',
  `password` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `join_time` datetime NULL DEFAULT NULL COMMENT '注册时间',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登陆时间',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '0：禁用 1：启用',
  `use_space` bigint NULL DEFAULT NULL COMMENT '使用空间 单位：byte ',
  `total_space` bigint NULL DEFAULT NULL COMMENT '总空间 单位：byte ',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `key_email`(`email` ASC) USING BTREE COMMENT '邮箱索引',
  UNIQUE INDEX `key_qq_open_id`(`qq_open_id` ASC) USING BTREE COMMENT 'qq open id索引',
  UNIQUE INDEX `key_nick_name`(`nick_name` ASC) USING BTREE COMMENT '昵称索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_info
-- ----------------------------
INSERT INTO `user_info` VALUES ('8343491440', 'test001', '18359389968@163.com', NULL, NULL, '47ec2dd791e31e2ef2076caf64ed9b3d', '2023-08-23 17:30:15', '2023-09-16 18:00:32', 1, 10107553, 230686720);
INSERT INTO `user_info` VALUES ('8343491441', 'admin', 'test@qq.com', NULL, '', '47ec2dd791e31e2ef2076caf64ed9b3d', '2023-08-23 17:30:15', '2023-09-16 14:44:02', 1, 48917226, 999999999);

SET FOREIGN_KEY_CHECKS = 1;
