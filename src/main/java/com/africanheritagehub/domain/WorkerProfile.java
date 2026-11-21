package com.africanheritagehub.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class WorkerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    private String headline;

    @Column(length = 2000)
    private String bio;

    private String city;
    private String region;

    @Column(precision = 12, scale = 2)
    private BigDecimal rate;

    @Enumerated(EnumType.STRING)
    private RateUnit rateUnit = RateUnit.DAY;

    private int yearsExperience;
    private boolean offersCasual = true;
    private boolean offersPermanent = false;

    /** Free-text specifics: vehicle make/capacity/plate, fabrics sewn, licence number, etc. */
    @Column(length = 2000)
    private String details;

    /** File names stored under the upload directory. */
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> photos = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private AccountStatus status = AccountStatus.ACTIVE;

    /** UNPAID_COMMISSION (automatic) or ADMIN (manual). */
    private String lockReason;

    private boolean verified;
    private double ratingAvg;
    private int ratingCount;

    public boolean isComplete() {
        return rate != null && headline != null && !headline.isBlank();
    }

    public boolean isLocked() { return status == AccountStatus.LOCKED; }

    public int getStars() { return (int) Math.round(ratingAvg); }

    public String getCover() { return photos.isEmpty() ? null : photos.get(0); }
}
