package com.africanheritagehub.config;

import com.africanheritagehub.service.FileStorageService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    public static final List<Locale> LANGUAGES = List.of(Locale.ENGLISH, Locale.FRENCH, new Locale("es"));

    private final FileStorageService storage;
    private final MessageSource messageSource;

    public WebConfig(FileStorageService storage, MessageSource messageSource) {
        this.storage = storage;
        this.messageSource = messageSource;
    }

    /** English by default; the choice (?lang=en|fr|es) is remembered in a cookie for a year. */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver r = new CookieLocaleResolver("AHH_LANG");
        r.setDefaultLocale(Locale.ENGLISH);
        r.setCookieMaxAge(Duration.ofDays(365));
        return r;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LocaleChangeInterceptor i = new LocaleChangeInterceptor();
        i.setParamName("lang");
        registry.addInterceptor(i);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(storage.getRoot().toUri().toString())
                .setCachePeriod(3600);
    }

    /** Lets @NotBlank(message = "{validation.required}") resolve from messages_xx.properties. */
    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean v = new LocalValidatorFactoryBean();
        v.setValidationMessageSource(messageSource);
        return v;
    }
}
