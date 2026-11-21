package com.africanheritagehub.domain;

public enum InvoiceKind {
    /** Percentage paid by a skilled worker for a client obtained through the hub. */
    WORKER_COMMISSION,
    /** Fee paid by a client who hires a permanent worker (or casual fee, once enabled). */
    CLIENT_FEE
}
