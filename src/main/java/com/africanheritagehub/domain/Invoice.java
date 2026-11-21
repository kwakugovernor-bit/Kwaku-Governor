package com.africanheritagehub.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User payer;

    @ManyToOne(optional = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    private InvoiceKind kind;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.DUE;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    /** Percentage applied when the invoice was raised (kept for audit, settings may change later). */
    @Column(precision = 5, scale = 2)
    private BigDecimal percent;

    private LocalDate dueDate;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime paidAt;
    private String paymentReference;
    private String paymentNetwork;

    public boolean isOverdue() {
        return status == InvoiceStatus.DUE && dueDate != null && dueDate.isBefore(LocalDate.now());
    }
}
