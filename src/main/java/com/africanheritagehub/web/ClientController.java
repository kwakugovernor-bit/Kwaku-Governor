package com.africanheritagehub.web;

import com.africanheritagehub.domain.User;
import com.africanheritagehub.service.BillingService;
import com.africanheritagehub.service.BookingService;
import com.africanheritagehub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientController {
    private final UserService users;
    private final BookingService bookings;
    private final BillingService billing;

    public ClientController(UserService users, BookingService bookings, BillingService billing) {
        this.users = users;
        this.bookings = bookings;
        this.billing = billing;
    }

    @GetMapping("/client")
    public String dashboard(Authentication auth, Model m) {
        User me = users.byEmail(auth.getName());
        m.addAttribute("bookings", bookings.forClient(me));
        m.addAttribute("invoices", billing.forUser(me));
        return "client-dashboard";
    }
}
