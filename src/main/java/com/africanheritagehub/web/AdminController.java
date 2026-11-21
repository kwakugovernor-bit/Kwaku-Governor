package com.africanheritagehub.web;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.repo.*;
import com.africanheritagehub.service.BillingService;
import com.africanheritagehub.service.BusinessException;
import com.africanheritagehub.service.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserRepository users;
    private final WorkerProfileRepository workers;
    private final BookingRepository bookings;
    private final BillingService billing;
    private final SettingsService settings;

    public AdminController(UserRepository users, WorkerProfileRepository workers, BookingRepository bookings,
                           BillingService billing, SettingsService settings) {
        this.users = users;
        this.workers = workers;
        this.bookings = bookings;
        this.billing = billing;
        this.settings = settings;
    }

    @GetMapping
    public String dashboard(Model m) {
        m.addAttribute("workerCount", users.countByRole(Role.WORKER));
        m.addAttribute("clientCount", users.countByRole(Role.CLIENT));
        m.addAttribute("lockedCount", workers.countByStatus(AccountStatus.LOCKED));
        m.addAttribute("bookingCount", bookings.count());
        m.addAttribute("totalDue", billing.totalDue());
        m.addAttribute("totalPaid", billing.totalPaid());
        m.addAttribute("locked", workers.findByStatus(AccountStatus.LOCKED));
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String users(Model m) {
        m.addAttribute("workers", workers.findAll());
        m.addAttribute("clients", users.findAll().stream().filter(u -> u.getRole() == Role.CLIENT).toList());
        return "admin-users";
    }

    @GetMapping("/invoices")
    public String invoices(Model m) {
        m.addAttribute("invoices", billing.all());
        return "admin-invoices";
    }

    @PostMapping("/workers/{id}/lock")
    public String lock(@PathVariable Long id, RedirectAttributes ra) {
        billing.adminLock(id);
        ra.addFlashAttribute("success", "msg.admin.done");
        return "redirect:/admin/users";
    }

    @PostMapping("/workers/{id}/unlock")
    public String unlock(@PathVariable Long id, RedirectAttributes ra) {
        billing.adminUnlock(id);
        ra.addFlashAttribute("success", "msg.admin.done");
        return "redirect:/admin/users";
    }

    @PostMapping("/workers/{id}/verify")
    public String verify(@PathVariable Long id, RedirectAttributes ra) {
        WorkerProfile w = workers.findById(id).orElseThrow(() -> new BusinessException("error.notfound"));
        w.setVerified(!w.isVerified());
        workers.save(w);
        ra.addFlashAttribute("success", "msg.admin.done");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra) {
        User u = users.findById(id).orElseThrow(() -> new BusinessException("error.notfound"));
        if (u.getRole() == Role.ADMIN) throw new BusinessException("error.forbidden");
        u.setEnabled(!u.isEnabled());
        users.save(u);
        ra.addFlashAttribute("success", "msg.admin.done");
        return "redirect:/admin/users";
    }

    @PostMapping("/run-lock-check")
    public String runLockCheck(RedirectAttributes ra) {
        billing.enforceLocks();
        ra.addFlashAttribute("success", "msg.admin.done");
        return "redirect:/admin";
    }

    @GetMapping("/settings")
    public String settingsForm(Model m) {
        m.addAttribute("s", settings.get());
        return "admin-settings";
    }

    @PostMapping("/settings")
    public String saveSettings(@RequestParam BigDecimal workerCommissionPercent,
                               @RequestParam BigDecimal permanentHirePercent,
                               @RequestParam BigDecimal clientCasualPercent,
                               @RequestParam int graceDays, RedirectAttributes ra) {
        for (BigDecimal p : new BigDecimal[]{workerCommissionPercent, permanentHirePercent, clientCasualPercent}) {
            if (p.signum() < 0 || p.compareTo(BigDecimal.valueOf(100)) > 0) throw new BusinessException("error.percent");
        }
        if (graceDays < 0 || graceDays > 90) throw new BusinessException("error.percent");
        PlatformSettings s = settings.get();
        s.setWorkerCommissionPercent(workerCommissionPercent);
        s.setPermanentHirePercent(permanentHirePercent);
        s.setClientCasualPercent(clientCasualPercent);
        s.setGraceDays(graceDays);
        settings.save(s);
        ra.addFlashAttribute("success", "msg.settings.saved");
        return "redirect:/admin/settings";
    }
}
