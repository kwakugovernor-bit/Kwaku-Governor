package com.africanheritagehub.web;

import com.africanheritagehub.domain.Role;
import com.africanheritagehub.domain.WorkerProfile;
import com.africanheritagehub.repo.UserRepository;
import com.africanheritagehub.service.SettingsService;
import com.africanheritagehub.service.WorkerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final WorkerService workers;
    private final SettingsService settings;
    private final UserRepository users;

    public HomeController(WorkerService workers, SettingsService settings, UserRepository users) {
        this.workers = workers;
        this.settings = settings;
        this.users = users;
    }

    @GetMapping("/")
    public String home(Model m) {
        List<WorkerProfile> featured = workers.featured(6);
        m.addAttribute("featured", featured);
        m.addAttribute("workerCount", users.countByRole(Role.WORKER));
        m.addAttribute("clientCount", users.countByRole(Role.CLIENT));
        return "home";
    }

    @GetMapping("/pricing")
    public String pricing(Model m) {
        m.addAttribute("settings", settings.get());
        return "pricing";
    }
}
