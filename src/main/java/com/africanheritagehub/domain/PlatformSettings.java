package com.africanheritagehub.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Single-row table (id = 1) holding the fee rules an administrator can change at any time. */
@Entity
@Getter @Setter
public class PlatformSettings {
    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    /** % of the agreed amount a worker pays the hub for every client obtained here. */
    @Column(precision = 5, scale = 2)
    private BigDecimal workerCommissionPercent = new BigDecimal("10.00");

    /** % of one month's pay a client pays to hire a permanent worker. */
    @Column(precision = 5, scale = 2)
    private BigDecimal permanentHirePercent = new BigDecimal("15.00");

    /** % clients pay for casual bookings. Free for now; can be switched on later. */
    @Column(precision = 5, scale = 2)
    private BigDecimal clientCasualPercent = BigDecimal.ZERO;

    /** Days a worker has to pay before the account is locked. */
    private int graceDays = 7;
}
