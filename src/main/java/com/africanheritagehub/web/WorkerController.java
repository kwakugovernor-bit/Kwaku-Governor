package com.africanheritagehub.web;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.service.BookingService;
import com.africanheritagehub.service.BusinessException;
import com.africanheritagehub.service.SettingsService;
import com.africanheritagehub.service.WorkerService;
import com.africanheritagehub.web.form.BookingForm;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WorkerController {
    private final WorkerService workers;
    private final BookingService bookings;
    private final SettingsService settings;
    private final com.africanheritagehub.service.UserService users;

    public WorkerController(WorkerService workers, BookingService bookings, SettingsService settings,
                            com.africanheritagehub.service.UserService users) {
        this.workers = workers;
        this.bookings = bookings;
        this.settings = settings;
        this.users = users;
    }

    @GetMapping("/workers")
    public String list(@RequestParam(required = false) Category category,
                       @RequestParam(required = false) String region,
                       @RequestParam(required = false) String q,
                       @RequestParam(defaultValue = "false") boolean permanent,
                       Model m) {
        m.addAttribute("results", workers.search(category, region, q, permanent));
        m.addAttribute("selCategory", category);
        m.addAttribute("selRegion", region);
        m.addAttribute("q", q);
        m.addAttribute("permanent", permanent);
        return "workers";
    }

    @GetMapping("/workers/{id}")
    public String detail(@PathVariable Long id, Model m) {
        WorkerProfile w = workers.get(id);
        if (w.isLocked() || !w.isComplete()) throw new BusinessException("error.worker.unavailable");
        m.addAttribute("w", w);
        m.addAttribute("reviews", workers.reviewsFor(w));
        m.addAttribute("settings", settings.get());
        if (!m.containsAttribute("form")) m.addAttribute("form", new BookingForm());
        return "worker-detail";
    }

    @GetMapping("/workers/{id}/book")
    public String bookRedirect(@PathVariable Long id) { return "redirect:/workers/" + id; }

    @PostMapping("/workers/{id}/book")
    public String book(@PathVariable Long id, @Valid @ModelAttribute("form") BookingForm form, BindingResult br,
                       Authentication auth, Model m, RedirectAttributes ra) {
        if (br.hasErrors()) return detail(id, m);
        bookings.create(users.byEmail(auth.getName()), id, form);
        ra.addFlashAttribute("success", "msg.booking.sent");
        return "redirect:/client";
    }
}
