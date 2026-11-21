package com.africanheritagehub.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User client;

    @ManyToOne(optional = false)
    private WorkerProfile worker;

    @Enumerated(EnumType.STRING)
    private BookingType type;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private LocalDate startDate;

    /** Number of hours/days/jobs for casual work (ignored for permanent). */
    private int quantity = 1;

    /** Casual: rate x quantity. Permanent: the monthly pay offered. */
    @Column(precision = 12, scale = 2)
    private BigDecimal agreedAmount;

    @Column(length = 1500)
    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();

    /** Phone numbers are only shared once the worker accepts, so the hub is the source of the lead. */
    public boolean isContactVisible() {
        return status == BookingStatus.ACCEPTED || status == BookingStatus.COMPLETED;
    }
}
