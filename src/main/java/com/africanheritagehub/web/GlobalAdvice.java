package com.africanheritagehub.web;

import com.africanheritagehub.config.WebConfig;
import com.africanheritagehub.domain.*;
import com.africanheritagehub.service.BusinessException;
import com.africanheritagehub.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@ControllerAdvice
public class GlobalAdvice {
    private final UserService users;

    public GlobalAdvice(UserService users) {
        this.users = users;
    }

    @ModelAttribute("me")
    public User me(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
        return users.byEmail(auth.getName());
    }

    @ModelAttribute("categories")
    public Category[] categories() { return Category.values(); }

    @ModelAttribute("regions")
    public List<String> regions() { return Regions.ALL; }

    @ModelAttribute("languages")
    public List<Locale> languages() { return WebConfig.LANGUAGES; }

    /** Business-rule failures return the user to the page they came from with a translated message. */
    @ExceptionHandler(BusinessException.class)
    public String business(BusinessException e, HttpServletRequest req, RedirectAttributes ra) {
        ra.addFlashAttribute("error", e.getMessage());
        String back = "/";
        String ref = req.getHeader("Referer");
        if (ref != null) {
            try {
                URI u = URI.create(ref);
                if (u.getHost() != null && u.getHost().equalsIgnoreCase(req.getServerName()) && u.getPath() != null) {
                    back = u.getPath() + (u.getQuery() != null ? "?" + u.getQuery() : "");
                }
            } catch (IllegalArgumentException ignored) {
                // fall back to home
            }
        }
        return "redirect:" + back;
    }
}
