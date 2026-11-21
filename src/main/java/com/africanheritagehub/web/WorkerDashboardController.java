package com.africanheritagehub.web;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.service.*;
import com.africanheritagehub.web.form.WorkerForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/worker")
public class WorkerDashboardController {
    private final UserService users;
    private final WorkerService workers;
    private final BookingService bookings;
    private final BillingService billing;
    private final SettingsService settings;
    private final MessageSource messages;

    public WorkerDashboardController(UserService users, WorkerService workers, BookingService bookings,
                                     BillingService billing, SettingsService settings, MessageSource messages) {
        this.messages = messages;
        this.users = users;
        this.workers = workers;
        this.bookings = bookings;
        this.billing = billing;
        this.settings = settings;
    }

    @GetMapping
    public String dashboard(Authentication auth, Model m) {
        User me = users.byEmail(auth.getName());
        WorkerProfile p = workers.forUser(me);
        billing.evaluate(p); // refresh lock state on every visit as well as on the hourly job
        List<Booking> list = bookings.forWorker(p);
        List<Invoice> invoices = billing.forUser(me);
        BigDecimal due = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.DUE)
                .map(Invoice::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal earned = list.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.ACCEPTED)
                .map(Booking::getAgreedAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        m.addAttribute("p", p);
        m.addAttribute("bookings", list);
        m.addAttribute("invoices", invoices);
        m.addAttribute("amountDue", due);
        m.addAttribute("earned", earned);
        m.addAttribute("settings", settings.get());
        return "worker-dashboard";
    }

    /** Category-specific guidance for the "details" box, as JSON for the page script (e.g. vehicle info for truck owners). */
    private String hintsJson() {
        var locale = LocaleContextHolder.getLocale();
        Map<String, String> hints = new LinkedHashMap<>();
        hints.put("DEFAULT", messages.getMessage("hint.DEFAULT", null, locale));
        for (Category c : Category.values()) {
            String h = messages.getMessage("hint." + c.name(), null, null, locale);
            if (h != null) hints.put(c.name(), h);
        }
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(hints);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return "{}";
        }
    }

    @GetMapping("/profile")
    public String profileForm(Authentication auth, Model m) {
        WorkerProfile p = workers.forUser(users.byEmail(auth.getName()));
        if (!m.containsAttribute("form")) m.addAttribute("form", WorkerForm.from(p));
        m.addAttribute("p", p);
        m.addAttribute("hintsJson", hintsJson());
        return "worker-profile";
    }

    @PostMapping("/profile")
    public String saveProfile(@Valid @ModelAttribute("form") WorkerForm form, BindingResult br,
                              Authentication auth, Model m, RedirectAttributes ra) {
        User me = users.byEmail(auth.getName());
        if (!form.isOffersCasual() && !form.isOffersPermanent()) {
            br.rejectValue("offersCasual", "validation.offer.type", "Select at least one");
        }
        if (br.hasErrors()) {
            m.addAttribute("p", workers.forUser(me));
            m.addAttribute("hintsJson", hintsJson());
            return "worker-profile";
        }
        workers.saveProfile(me, form);
        ra.addFlashAttribute("success", "msg.profile.saved");
        return "redirect:/worker";
    }

    @PostMapping("/photos/delete")
    public String deletePhoto(@RequestParam String file, Authentication auth, RedirectAttributes ra) {
        workers.removePhoto(users.byEmail(auth.getName()), file);
        return "redirect:/worker/profile";
    }
}
