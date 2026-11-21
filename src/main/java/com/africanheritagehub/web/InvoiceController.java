package com.africanheritagehub.web;

import com.africanheritagehub.domain.Invoice;
import com.africanheritagehub.domain.InvoiceStatus;
import com.africanheritagehub.service.BillingService;
import com.africanheritagehub.service.BusinessException;
import com.africanheritagehub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/invoices")
public class InvoiceController {
    public static final List<String> NETWORKS = List.of("MTN MoMo", "Telecel Cash", "AirtelTigo Money");

    private final BillingService billing;
    private final UserService users;

    public InvoiceController(BillingService billing, UserService users) {
        this.billing = billing;
        this.users = users;
    }

    @GetMapping("/{id}/pay")
    public String payForm(@PathVariable Long id, Authentication auth, Model m) {
        Invoice inv = billing.get(id);
        if (!inv.getPayer().getEmail().equalsIgnoreCase(auth.getName())) throw new BusinessException("error.forbidden");
        if (inv.getStatus() == InvoiceStatus.PAID) throw new BusinessException("error.invoice.paid");
        m.addAttribute("invoice", inv);
        m.addAttribute("networks", NETWORKS);
        return "pay";
    }

    @PostMapping("/{id}/pay")
    public String pay(@PathVariable Long id, @RequestParam String network, @RequestParam String phone,
                      Authentication auth, RedirectAttributes ra) {
        if (!NETWORKS.contains(network)) throw new BusinessException("error.payment.failed");
        billing.pay(id, users.byEmail(auth.getName()), network, phone);
        ra.addFlashAttribute("success", "msg.payment.ok");
        return AuthController.home(auth);
    }
}
