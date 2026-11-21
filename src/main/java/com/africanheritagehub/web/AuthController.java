package com.africanheritagehub.web;

import com.africanheritagehub.domain.Role;
import com.africanheritagehub.service.UserService;
import com.africanheritagehub.web.form.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService users;

    public AuthController(UserService users) {
        this.users = users;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(@RequestParam(required = false) Role as, Model m) {
        RegistrationForm f = new RegistrationForm();
        if (as == Role.WORKER) f.setRole(Role.WORKER);
        m.addAttribute("form", f);
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult br,
                           RedirectAttributes ra) {
        if (br.hasErrors()) return "register";
        users.register(form);
        ra.addFlashAttribute("success", "msg.registered");
        return "redirect:/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        return home(auth);
    }

    /** Redirect straight to the role's dashboard (keeps flash messages, unlike bouncing through /dashboard). */
    public static String home(Authentication auth) {
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) return "redirect:/admin";
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_WORKER"))) return "redirect:/worker";
        return "redirect:/client";
    }
}
