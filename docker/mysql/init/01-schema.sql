-- 停车管理平台初始化脚本（容器首次启动时自动执行）

-- 后台账号：超级管理 / 普通管理（与小程序用户分表）
CREATE TABLE admin_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username      VARCHAR(64)  NOT NULL COMMENT '账号',
    password      VARCHAR(128) NOT NULL COMMENT '密码（加密存储）',
    real_name     VARCHAR(64)  NULL COMMENT '姓名',
    phone         VARCHAR(20)  NULL COMMENT '手机号',
    role          VARCHAR(20)  NOT NULL COMMENT '角色：SUPER_ADMIN/ADMIN',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) COMMENT '后台管理员';

-- 小程序用户（微信登录，无独立密码）
CREATE TABLE wx_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid        VARCHAR(64)  NOT NULL COMMENT '微信 openid',
    unionid       VARCHAR(64)  NULL COMMENT '微信 unionid',
    nickname      VARCHAR(64)  NULL COMMENT '昵称',
    avatar        VARCHAR(255) NULL COMMENT '头像',
    phone         VARCHAR(20)  NULL COMMENT '手机号（getPhoneNumber 获取）',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (openid)
) COMMENT '小程序用户';

-- 停车场
CREATE TABLE parking_lot (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(128) NOT NULL COMMENT '名称',
    location       VARCHAR(255) NOT NULL COMMENT '位置',
    total_spaces   INT          NOT NULL DEFAULT 0 COMMENT '车位数',
    bind_limit     INT          NULL COMMENT '最大可绑定车辆数（NULL 不限）',
    monthly_price  DECIMAL(10,2) NULL COMMENT '包月价格',
    quarterly_price DECIMAL(10,2) NULL COMMENT '包季价格',
    yearly_price   DECIMAL(10,2) NULL COMMENT '包年价格',
    -- 临停规则
    free_minutes   INT          NOT NULL DEFAULT 0 COMMENT '免费时长（分钟）',
    first_hours    INT          NOT NULL DEFAULT 1 COMMENT '首计费时段（小时）',
    first_price    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '首时段费用',
    hourly_price   DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '之后每小时费用',
    daily_max      DECIMAL(10,2) NULL COMMENT '单日封顶费用',
    qrcode_secret  VARCHAR(64)  NOT NULL COMMENT '二维码防伪签名密钥',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted        TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) COMMENT '停车场（套餐价格与临停规则按车场维度配置）';

-- 普通管理员与车场分配（一个管理员可分配多个车场）
CREATE TABLE admin_lot (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    admin_id   BIGINT   NOT NULL COMMENT 'admin_user.id',
    lot_id     BIGINT   NOT NULL COMMENT '车场ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_lot (admin_id, lot_id)
) COMMENT '管理员-车场分配';

-- 车辆（owner 为主绑定人，其他为授权使用人）
CREATE TABLE vehicle (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    plate_no    VARCHAR(16)  NOT NULL COMMENT '车牌号',
    owner_id    BIGINT       NOT NULL COMMENT '车主（主绑定用户）ID：wx_user.id',
    lot_id      BIGINT       NOT NULL COMMENT '默认绑定车场ID（扫码进入的车场）',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_plate (plate_no)
) COMMENT '车辆';

-- 小程序用户与车辆多对多绑定
CREATE TABLE user_vehicle (
    id         BIGINT   NOT NULL AUTO_INCREMENT,
    user_id    BIGINT   NOT NULL COMMENT 'wx_user.id',
    vehicle_id BIGINT   NOT NULL,
    is_owner   TINYINT  NOT NULL DEFAULT 0 COMMENT '是否车主',
    bind_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_vehicle (user_id, vehicle_id),
    KEY idx_vehicle (vehicle_id)
) COMMENT '小程序用户-车辆绑定';

-- 套餐订阅（未到期续费：new_expire = GREATEST(current_expire, now) + 时长）
CREATE TABLE subscription (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    vehicle_id    BIGINT        NOT NULL,
    lot_id        BIGINT        NOT NULL,
    user_id       BIGINT        NOT NULL COMMENT '付费用户：wx_user.id',
    plan_type     VARCHAR(16)   NOT NULL COMMENT '套餐：MONTH/QUARTER/YEAR',
    price         DECIMAL(10,2) NOT NULL COMMENT '成交价（元）',
    start_time    DATETIME      NOT NULL,
    expire_time   DATETIME      NOT NULL COMMENT '到期时间',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_vehicle (vehicle_id),
    KEY idx_expire (expire_time)
) COMMENT '套餐订阅记录';

-- 临停进出记录（需配合道闸/车牌识别）
CREATE TABLE temp_parking_record (
    id            BIGINT        NOT NULL AUTO_INCREMENT,
    lot_id        BIGINT        NOT NULL,
    plate_no      VARCHAR(16)   NOT NULL,
    entry_time    DATETIME      NOT NULL,
    exit_time     DATETIME      NULL,
    fee           DECIMAL(10,2) NULL COMMENT '应收费用（元）',
    pay_status    TINYINT       NOT NULL DEFAULT 0 COMMENT '0未支付 1已支付',
    PRIMARY KEY (id),
    KEY idx_lot_plate (lot_id, plate_no)
) COMMENT '临停记录';

-- 支付订单（微信支付，统一下单后回调驱动状态流转）
CREATE TABLE pay_order (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    order_no        VARCHAR(32)  NOT NULL COMMENT '商户订单号',
    user_id         BIGINT       NOT NULL COMMENT '付款用户：wx_user.id',
    biz_type        VARCHAR(16)  NOT NULL COMMENT '业务类型：SUBSCRIPTION套餐 / TEMP_PARKING临停',
    biz_id          BIGINT       NOT NULL COMMENT '关联业务ID（SUBSCRIPTION=vehicle.id / TEMP_PARKING=记录id）',
    plan_type       VARCHAR(16)  NULL COMMENT '套餐类型（biz_type=SUBSCRIPTION 时）',
    amount          INT          NOT NULL COMMENT '支付金额（分）',
    status          VARCHAR(16)  NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/PENDING/PAID/CLOSED/REFUNDED',
    prepay_id       VARCHAR(64)  NULL COMMENT '微信预支付ID',
    transaction_id  VARCHAR(64)  NULL COMMENT '微信支付单号',
    paid_time       DATETIME     NULL COMMENT '支付完成时间',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user (user_id),
    KEY idx_biz (biz_type, biz_id)
) COMMENT '支付订单';

-- 初始超级管理员（密码 Admin@123456，首次登录后请立即修改）
INSERT INTO admin_user (username, password, real_name, role)
VALUES ('admin', '$2a$10$F0Qq7gPTogofsWPBuC6QeOXWretWqmgPYnhgWiJSzrpdoOqvhh7YS', '超级管理员', 'SUPER_ADMIN');
