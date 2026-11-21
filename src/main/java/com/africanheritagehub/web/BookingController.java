package com.africanheritagehub.web;

import com.africanheritagehub.service.BookingService;
import com.africanheritagehub.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings")
public class BookingController {
    private final BookingService bookings;
    private final UserService users;

    public BookingController(BookingService bookings, UserService users) {
        this.bookings = bookings;
        this.users = users;
    }

    @PostMapping("/{id}/accept")
    public String accept(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        bookings.accept(users.byEmail(a.getName()), id);
        ra.addFlashAttribute("success", "msg.booking.accepted");
        return "redirect:/worker";
    }

    @PostMapping("/{id}/decline")
    public String decline(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        bookings.decline(users.byEmail(a.getName()), id);
        ra.addFlashAttribute("success", "msg.booking.declined");
        return "redirect:/worker";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        bookings.cancel(users.byEmail(a.getName()), id);
        ra.addFlashAttribute("success", "msg.booking.cancelled");
        return "redirect:/client";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, Authentication a, RedirectAttributes ra) {
        bookings.complete(users.byEmail(a.getName()), id);
        ra.addFlashAttribute("success", "msg.booking.completed");
        return AuthController.home(a);
    }

    @PostMapping("/{id}/review")
    public String review(@PathVariable Long id, @RequestParam int rating, @RequestParam(required = false) String comment,
                         Authentication a, RedirectAttributes ra) {
        bookings.review(users.byEmail(a.getName()), id, rating, comment);
        ra.addFlashAttribute("success", "msg.review.thanks");
        return "redirect:/client";
    }
}
