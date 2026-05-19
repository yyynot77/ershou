package com.campus.ershou.common;

public final class Constants {
    private Constants() {}

    public static final String ROLE_USER = "USER";
    public static final String ROLE_MERCHANT = "MERCHANT";
    public static final String ROLE_ADMIN = "ADMIN";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    public static final String PRODUCT_PENDING = "PENDING";
    public static final String PRODUCT_APPROVED = "APPROVED";
    public static final String PRODUCT_PUBLISHED = "PUBLISHED";
    public static final String PRODUCT_LOCKED = "LOCKED";
    public static final String PRODUCT_SOLD = "SOLD";
    public static final String PRODUCT_OFF_SHELF = "OFF_SHELF";

    public static final double[] MERCHANT_FEE_RATE = {0, 0.001, 0.002, 0.005, 0.0075, 0.01};

    public static final int POINTS_PER_YUAN = 1;
    public static final int POINTS_REDEEM_UNIT = 100;
}
