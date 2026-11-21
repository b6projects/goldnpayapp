package com.gps.cs.entity;

public enum OtpPurpose {
    ACTIVATION, PASSWORD_RESET,
    /** Internal: proof that a PASSWORD_RESET otp was verified. */
    PASSWORD_RESET_GRANT
}
