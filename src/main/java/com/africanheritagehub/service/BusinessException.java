package com.africanheritagehub.service;

/** A rule violation whose message is an i18n key (error.*) shown to the user in their language. */
public class BusinessException extends RuntimeException {
    public BusinessException(String messageKey) {
        super(messageKey);
    }
}
