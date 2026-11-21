package com.africanheritagehub.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/** DEVELOPMENT ONLY: approves every payment without moving any money. */
@Component
public class SimulatedPaymentGateway implements PaymentGateway {
    @Override
    public String charge(String network, String phone, BigDecimal amount, String description) {
        if (phone == null || !phone.replaceAll("[\\s-]", "").matches("^(\\+233|0)\\d{9}$")) {
            throw new BusinessException("error.payment.phone");
        }
        return "SIM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
