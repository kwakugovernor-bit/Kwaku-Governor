package com.africanheritagehub.web.form;

import com.africanheritagehub.domain.Category;
import com.africanheritagehub.domain.RateUnit;
import com.africanheritagehub.domain.WorkerProfile;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter @Setter
public class WorkerForm {
    @NotNull(message = "{validation.required}")
    private Category category;

    @NotBlank(message = "{validation.required}")
    @Size(max = 120, message = "{validation.too.long}")
    private String headline;

    @Size(max = 2000, message = "{validation.too.long}")
    private String bio;

    @Size(max = 80, message = "{validation.too.long}")
    private String city;

    @NotBlank(message = "{validation.required}")
    private String region;

    @NotNull(message = "{validation.required}")
    @DecimalMin(value = "1.00", message = "{validation.rate}")
    @DecimalMax(value = "1000000.00", message = "{validation.rate}")
    private BigDecimal rate;

    @NotNull(message = "{validation.required}")
    private RateUnit rateUnit = RateUnit.DAY;

    @Min(value = 0, message = "{validation.years}")
    @Max(value = 70, message = "{validation.years}")
    private int yearsExperience;

    private boolean offersCasual = true;
    private boolean offersPermanent;

    @Size(max = 2000, message = "{validation.too.long}")
    private String details;

    private MultipartFile[] photos;

    public static WorkerForm from(WorkerProfile p) {
        WorkerForm f = new WorkerForm();
        f.category = p.getCategory();
        f.headline = p.getHeadline();
        f.bio = p.getBio();
        f.city = p.getCity();
        f.region = p.getRegion();
        f.rate = p.getRate();
        f.rateUnit = p.getRateUnit() == null ? RateUnit.DAY : p.getRateUnit();
        f.yearsExperience = p.getYearsExperience();
        f.offersCasual = p.isOffersCasual();
        f.offersPermanent = p.isOffersPermanent();
        f.details = p.getDetails();
        return f;
    }
}
