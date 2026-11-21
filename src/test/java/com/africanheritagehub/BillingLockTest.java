package com.africanheritagehub;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.repo.*;
import com.africanheritagehub.service.BillingService;
import com.africanheritagehub.service.BookingService;
import com.africanheritagehub.service.BusinessException;
import com.africanheritagehub.web.form.BookingForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:locktest;DB_CLOSE_DELAY=-1",
        "app.demo-data=true",
        "app.upload-dir=target/test-uploads"})
class BillingLockTest {
    @Autowired BookingService bookings;
    @Autowired BillingService billing;
    @Autowired UserRepository users;
    @Autowired WorkerProfileRepository workers;
    @Autowired InvoiceRepository invoices;

    private User client() {
        return users.findByEmailIgnoreCase("lock-client@test.gh").orElseGet(() -> {
            User u = new User();
            u.setEmail("lock-client@test.gh"); u.setFullName("Lock Client"); u.setPasswordHash("x");
            u.setRole(Role.CLIENT); u.setPhone("0200000000");
            return users.save(u);
        });
    }

    @Test
    void unpaidCommissionLocksWorkerAndPaymentUnlocks() {
        WorkerProfile w = workers.findAll().stream().filter(p -> p.isOffersPermanent()).findFirst().orElseThrow();
        BookingForm f = new BookingForm();
        f.setType(BookingType.CASUAL);
        f.setQuantity(2);
        f.setStartDate(LocalDate.now().plusDays(1));
        Booking b = bookings.create(client(), w.getId(), f);
        assertEquals(0, w.getRate().multiply(BigDecimal.valueOf(2)).compareTo(b.getAgreedAmount()));

        bookings.accept(w.getUser(), b.getId());
        List<Invoice> mine = invoices.findByPayerOrderByCreatedAtDesc(w.getUser());
        Invoice inv = mine.get(0);
        assertEquals(InvoiceKind.WORKER_COMMISSION, inv.getKind());
        assertEquals(0, inv.getAmount().compareTo(b.getAgreedAmount().multiply(new BigDecimal("0.10"))));
        // casual booking: client is not charged
        assertTrue(invoices.findByPayerOrderByCreatedAtDesc(b.getClient()).isEmpty());

        // not yet overdue -> stays active
        billing.enforceLocks();
        assertEquals(AccountStatus.ACTIVE, workers.findById(w.getId()).orElseThrow().getStatus());

        // overdue -> locked, hidden from search, cannot accept new work
        inv.setDueDate(LocalDate.now().minusDays(1));
        invoices.save(inv);
        billing.enforceLocks();
        WorkerProfile locked = workers.findById(w.getId()).orElseThrow();
        assertEquals(AccountStatus.LOCKED, locked.getStatus());
        assertThrows(BusinessException.class, () -> bookings.create(client(), w.getId(), f));

        // paying unlocks immediately
        billing.pay(inv.getId(), w.getUser(), "MTN MoMo", "0244000000");
        assertEquals(AccountStatus.ACTIVE, workers.findById(w.getId()).orElseThrow().getStatus());
    }

    @Test
    void permanentHireChargesClientFee() {
        WorkerProfile w = workers.findAll().stream().filter(p -> p.isOffersPermanent()).skip(1).findFirst().orElseThrow();
        BookingForm f = new BookingForm();
        f.setType(BookingType.PERMANENT);
        f.setOfferedAmount(new BigDecimal("1000"));
        f.setStartDate(LocalDate.now().plusDays(3));
        Booking b = bookings.create(client(), w.getId(), f);
        bookings.accept(w.getUser(), b.getId());
        Invoice fee = invoices.findByPayerOrderByCreatedAtDesc(b.getClient()).stream()
                .filter(i -> i.getBooking().getId().equals(b.getId())).findFirst().orElseThrow();
        assertEquals(InvoiceKind.CLIENT_FEE, fee.getKind());
        assertEquals(0, fee.getAmount().compareTo(new BigDecimal("150.00")));
    }
}
