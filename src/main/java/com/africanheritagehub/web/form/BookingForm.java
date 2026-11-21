package com.africanheritagehub.web.form;

import com.africanheritagehub.domain.BookingType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class BookingForm {
    @NotNull(message = "{validation.required}")
    private BookingType type = BookingType.CASUAL;

    @NotNull(message = "{validation.required}")
    @FutureOrPresent(message = "{validation.date.future}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Min(value = 1, message = "{validation.quantity}")
    @Max(value = 365, message = "{validation.quantity}")
    private int quantity = 1;

    /** Monthly pay offered, for permanent hires. */
    @DecimalMax(value = "10000000.00", message = "{validation.rate}")
    private BigDecimal offeredAmount;

    @Size(max = 1500, message = "{validation.too.long}")
    private String message;
}
