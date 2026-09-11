-- ============================================================================
-- 盲派八字命理平台 - 数据库结构
-- MySQL 5.7+ / 8.0
-- 说明：索引名一律加表名前缀，避免跨表同名索引冲突
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `mingli` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `mingli`;

-- ---------------------------------------------------------------- 字典表

-- 1.1 禄神对照表
CREATE TABLE IF NOT EXISTS `dict_lushen` (
  `id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `gan`      VARCHAR(4)  NOT NULL COMMENT '日干',
  `zhi`      VARCHAR(4)  NOT NULL COMMENT '禄支',
  `note`     VARCHAR(255) DEFAULT NULL COMMENT '说明',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lushen_gan` (`gan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='禄神对照表（规则 1.1）';

-- 1.2 羊刃对照表
CREATE TABLE IF NOT EXISTS `dict_yangren` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `gan`       VARCHAR(4) NOT NULL COMMENT '阳干',
  `zhi`       VARCHAR(8) NOT NULL COMMENT '羊刃支',
  `doubtful`  TINYINT NOT NULL DEFAULT 0 COMMENT '是否存疑 0否 1是',
  `note`      VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_yangren_gan` (`gan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='羊刃对照表（规则 1.2）';

-- 1.3 驿马对照表
CREATE TABLE IF NOT EXISTS `dict_yima` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `group_zhi` VARCHAR(16) NOT NULL COMMENT '三合局支组，如 申子辰',
  `ma_zhi`    VARCHAR(16) NOT NULL COMMENT '驿马支',
  `wuxiang`   VARCHAR(255) DEFAULT NULL COMMENT '物象',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_yima_group` (`group_zhi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='驿马对照表（规则 1.3）';

-- 1.4 六甲空亡对照表
CREATE TABLE IF NOT EXISTS `dict_kongwang` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `xun_name`  VARCHAR(16) NOT NULL COMMENT '旬首',
  `gan_range` VARCHAR(32) NOT NULL COMMENT '该旬十干',
  `kong_zhi`  VARCHAR(16) NOT NULL COMMENT '空亡地支',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_kongwang_xun` (`xun_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='六甲空亡对照表（规则 1.4）';

-- 1.5 四柱宫位类象表
CREATE TABLE IF NOT EXISTS `dict_gongwei` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `dimension` VARCHAR(32) NOT NULL COMMENT '维度：六亲/时间/空间/人物/身体/物件/情商/表里',
  `pos_year`  VARCHAR(128) DEFAULT NULL COMMENT '年柱',
  `pos_month` VARCHAR(128) DEFAULT NULL COMMENT '月柱',
  `pos_day_gan` VARCHAR(128) DEFAULT NULL COMMENT '日干',
  `pos_day_zhi` VARCHAR(128) DEFAULT NULL COMMENT '日支',
  `pos_hour`  VARCHAR(128) DEFAULT NULL COMMENT '时柱',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_gongwei_dim` (`dimension`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='四柱宫位类象表（规则 1.5）';

-- 1.6 十神类象表
CREATE TABLE IF NOT EXISTS `dict_shishen` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `name`      VARCHAR(16) NOT NULL COMMENT '十神名',
  `positive`  VARCHAR(512) DEFAULT NULL COMMENT '正面心性',
  `negative`  VARCHAR(512) DEFAULT NULL COMMENT '过重负面/偏象',
  `careers`   VARCHAR(512) DEFAULT NULL COMMENT '可取职业',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shishen_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='十神类象表（规则 1.6）';

-- 1.7 十干物象表
CREATE TABLE IF NOT EXISTS `dict_gan_wuxiang` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `gan`     VARCHAR(4) NOT NULL,
  `wuxiang` VARCHAR(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ganwx_gan` (`gan`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='十干物象表（规则 1.7）';

-- 1.8 十二支物象表
CREATE TABLE IF NOT EXISTS `dict_zhi_wuxiang` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `zhi`     VARCHAR(4) NOT NULL,
  `wuxiang` VARCHAR(512) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_zhiwx_zhi` (`zhi`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='十二支物象表（规则 1.8）';

-- 1.9 墓库象对照表
CREATE TABLE IF NOT EXISTS `dict_muku` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `ku_type` VARCHAR(32) NOT NULL COMMENT '库的类别',
  `wuxiang` VARCHAR(255) DEFAULT NULL COMMENT '可取象',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_muku_type` (`ku_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='墓库象对照表（规则 1.9）';

-- 2.1 财富替代规则
CREATE TABLE IF NOT EXISTS `dict_qucai` (
  `id`       BIGINT NOT NULL AUTO_INCREMENT,
  `cond`     VARCHAR(255) NOT NULL COMMENT '原局条件',
  `method`   VARCHAR(128) NOT NULL COMMENT '取财方式',
  `xiji`     VARCHAR(512) DEFAULT NULL COMMENT '喜忌',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财富替代规则（规则 2.1）';

-- 2.2 取财方式判定表
CREATE TABLE IF NOT EXISTS `dict_qucai_mode` (
  `id`        BIGINT NOT NULL AUTO_INCREMENT,
  `feature`   VARCHAR(255) NOT NULL COMMENT '做功特征',
  `method`    VARCHAR(64)  NOT NULL COMMENT '取财方式',
  `industries` VARCHAR(255) DEFAULT NULL COMMENT '常见行业',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='取财方式判定表（规则 2.2）';

-- 规则条文（供前端展示规则出处）
CREATE TABLE IF NOT EXISTS `dict_rule` (
  `id`         BIGINT NOT NULL AUTO_INCREMENT,
  `rule_code`  VARCHAR(32) NOT NULL COMMENT '规则编号，如 1.1',
  `category`   VARCHAR(64) NOT NULL COMMENT '分类',
  `title`      VARCHAR(128) NOT NULL COMMENT '标题',
  `content`    TEXT COMMENT '规则内容',
  `doubtful`   TINYINT NOT NULL DEFAULT 0 COMMENT '存疑标记',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_rule_code` (`rule_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='规则条文表';

-- ---------------------------------------------------------------- 业务表

-- 排盘与命理分析记录
CREATE TABLE IF NOT EXISTS `bazi_record` (
  `id`           BIGINT NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(64)  DEFAULT NULL COMMENT '姓名',
  `gender`       VARCHAR(4)   DEFAULT NULL COMMENT 'M 男 / F 女',
  `birth_time`   VARCHAR(32)  NOT NULL COMMENT '出生时间 yyyy-MM-dd HH:mm',
  `calendar_type` VARCHAR(16) NOT NULL DEFAULT 'SOLAR' COMMENT 'SOLAR 公历 / LUNAR 农历',
  `tz_offset`    DECIMAL(4,1) NOT NULL DEFAULT 8.0 COMMENT '时区偏移',
  `longitude`    DECIMAL(8,4) NOT NULL DEFAULT 120.0 COMMENT '经度',
  `city_name`    VARCHAR(64)  DEFAULT NULL COMMENT '出生地',
  `true_solar`   TINYINT NOT NULL DEFAULT 1 COMMENT '是否使用真太阳时',
  `year_gz`      VARCHAR(8)   DEFAULT NULL COMMENT '年柱',
  `month_gz`     VARCHAR(8)   DEFAULT NULL COMMENT '月柱',
  `day_gz`       VARCHAR(8)   DEFAULT NULL COMMENT '日柱',
  `hour_gz`      VARCHAR(8)   DEFAULT NULL COMMENT '时柱',
  `day_master`   VARCHAR(4)   DEFAULT NULL COMMENT '日主',
  `chart_json`   LONGTEXT COMMENT '排盘结果 JSON',
  `analysis_json` LONGTEXT COMMENT '命理分析 JSON',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_record_create_time` (`create_time`),
  KEY `idx_record_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排盘记录表';
