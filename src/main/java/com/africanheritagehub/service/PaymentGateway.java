package com.africanheritagehub.service;

import java.math.BigDecimal;

/**
 * Abstraction over a mobile-money / card provider (MTN MoMo, Telecel Cash, AirtelTigo Money,
 * Hubtel, Paystack, ...). Replace {@link SimulatedPaymentGateway} with a real implementation
 * before going live.
 */
public interface PaymentGateway {
    /** Charge the payer; return a provider reference, or throw BusinessException("error.payment.failed"). */
    String charge(String network, String phone, BigDecimal amount, String description);
}
