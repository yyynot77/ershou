-- 校园二手交易市场数据库初始化脚本
-- 与 application.yml 中 spring.datasource.url 的数据库名保持一致（当前为 2023011345）
CREATE DATABASE IF NOT EXISTS `2023011345` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `2023011345`;

-- 用户表（普通用户/商家/管理员）
DROP TABLE IF EXISTS buyer_blacklist;
DROP TABLE IF EXISTS merchant_ban;
DROP TABLE IF EXISTS banner;
DROP TABLE IF EXISTS buyer_merchant_review;
DROP TABLE IF EXISTS product_review;
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS cart_item;
DROP TABLE IF EXISTS product_image;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS wallet_transaction;
DROP TABLE IF EXISTS wallet;
DROP TABLE IF EXISTS user_points_log;
DROP TABLE IF EXISTS user_points;
DROP TABLE IF EXISTS merchant_info;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录名',
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    city VARCHAR(50),
    gender TINYINT DEFAULT 0 COMMENT '0未知1男2女',
    bank_account VARCHAR(16) COMMENT '银行账号16位',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'USER/MERCHANT/ADMIN',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
    avatar VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统用户';

CREATE TABLE merchant_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    shop_name VARCHAR(100) NOT NULL COMMENT '店铺名',
    business_license VARCHAR(255) COMMENT '营业执照图片',
    id_card_image VARCHAR(255) COMMENT '身份证图片',
    merchant_level INT DEFAULT 1 COMMENT '商家等级1-5',
    satisfaction_rate DECIMAL(5,2) DEFAULT 100.00 COMMENT '好评率',
    total_sales DECIMAL(12,2) DEFAULT 0 COMMENT '累计交易额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '商家扩展信息';

CREATE TABLE category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    sort_order INT DEFAULT 0
) COMMENT '商品类别';

CREATE TABLE product (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL,
    category_id BIGINT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    original_price DECIMAL(10,2),
    price DECIMAL(10,2) NOT NULL,
    size_info VARCHAR(50) COMMENT '尺寸',
    condition_level VARCHAR(20) COMMENT '全新/九成新/八成新等',
    allow_bargain TINYINT DEFAULT 0,
    stock INT DEFAULT 1,
    sold_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/PUBLISHED/LOCKED/SOLD/OFF_SHELF/REJECTED',
    avg_rating DECIMAL(3,2) DEFAULT 5.00,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (merchant_id) REFERENCES sys_user(id),
    FOREIGN KEY (category_id) REFERENCES category(id)
) COMMENT '商品';

CREATE TABLE product_image (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) COMMENT '商品图片';

CREATE TABLE cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
) COMMENT '购物车';

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    buyer_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    points_deduct DECIMAL(10,2) DEFAULT 0 COMMENT '积分抵扣金额',
    pay_amount DECIMAL(12,2) NOT NULL COMMENT '实付',
    platform_fee DECIMAL(10,2) DEFAULT 0 COMMENT '平台手续费',
    status VARCHAR(20) DEFAULT 'PAID' COMMENT 'PAID/SHIPPED/RECEIVED/COMPLETED/RETURN_REQUEST/RETURNED/CANCELLED',
    meet_time VARCHAR(100) COMMENT '线下交易时间',
    meet_place VARCHAR(200) COMMENT '线下交易地点',
    receive_time DATETIME COMMENT '确认收货时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (buyer_id) REFERENCES sys_user(id)
) COMMENT '订单主表';

CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    product_name VARCHAR(200),
    price DECIMAL(10,2),
    quantity INT,
    subtotal DECIMAL(12,2),
    settle_status VARCHAR(20) DEFAULT 'ESCROW' COMMENT 'ESCROW/SETTLED/REFUNDED',
    settle_time DATETIME,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES product(id)
) COMMENT '订单明细';

CREATE TABLE wallet (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(12,2) DEFAULT 0,
    frozen DECIMAL(12,2) DEFAULT 0 COMMENT '冻结金额',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '用户钱包';

CREATE TABLE wallet_transaction (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    type VARCHAR(30) NOT NULL COMMENT 'RECHARGE/PAY/REFUND/SETTLE/FEE',
    related_order_id BIGINT,
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '钱包流水';

CREATE TABLE user_points (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    points INT DEFAULT 0,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
) COMMENT '用户积分';

CREATE TABLE user_points_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    points INT NOT NULL,
    type VARCHAR(20) COMMENT 'EARN/REDEEM',
    related_order_id BIGINT,
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '积分流水';

CREATE TABLE product_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL COMMENT '1-5星',
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES product(id)
) COMMENT '商品评价';

CREATE TABLE buyer_merchant_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    rating INT NOT NULL COMMENT '服务态度1-5',
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '买家对商家服务评价';

CREATE TABLE merchant_buyer_review (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    merchant_id BIGINT NOT NULL,
    buyer_id BIGINT NOT NULL,
    rating INT NOT NULL,
    content TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '商家对买家评价';

CREATE TABLE banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100),
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255),
    merchant_id BIGINT,
    product_id BIGINT,
    sort_order INT DEFAULT 0,
    enabled TINYINT DEFAULT 1
) COMMENT '首页轮播';

CREATE TABLE merchant_ban (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    merchant_id BIGINT NOT NULL,
    ban_type VARCHAR(20) COMMENT 'PUBLISH_BAN/SHOP_CLOSE',
    reason VARCHAR(200),
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '商家封禁';

CREATE TABLE buyer_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    buyer_id BIGINT NOT NULL,
    merchant_id BIGINT COMMENT 'NULL表示全平台拉黑',
    reason VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '购买者黑名单';

-- 平台中间账户（系统托管）
CREATE TABLE platform_account (
    id INT PRIMARY KEY DEFAULT 1,
    escrow_balance DECIMAL(14,2) DEFAULT 0,
    fee_income DECIMAL(14,2) DEFAULT 0
);
INSERT INTO platform_account(id) VALUES(1);

-- 初始数据
INSERT INTO category(name, sort_order) VALUES
('数码电子',1),('图书教材',2),('生活用品',3),('服饰鞋包',4),('运动户外',5),('其他',6);

-- 管理员账号由后端 DataInitializer 首次启动时自动创建: admin / admin123
