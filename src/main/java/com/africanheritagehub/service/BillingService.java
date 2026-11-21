package com.africanheritagehub.service;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.repo.InvoiceRepository;
import com.africanheritagehub.repo.UserRepository;
import com.africanheritagehub.repo.WorkerProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Commission invoicing, payment, and the automatic lock / unlock of worker accounts. */
@Service
public class BillingService {
    public static final String LOCK_UNPAID = "UNPAID_COMMISSION";
    public static final String LOCK_ADMIN = "ADMIN";
    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private final InvoiceRepository invoices;
    private final WorkerProfileRepository workers;
    private final UserRepository users;
    private final SettingsService settings;
    private final PaymentGateway gateway;

    public BillingService(InvoiceRepository invoices, WorkerProfileRepository workers, UserRepository users,
                          SettingsService settings, PaymentGateway gateway) {
        this.invoices = invoices;
        this.workers = workers;
        this.users = users;
        this.settings = settings;
        this.gateway = gateway;
    }

    private static BigDecimal pct(BigDecimal base, BigDecimal percent) {
        return base.multiply(percent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /** Called when a worker accepts a booking, i.e. when the hub has delivered a client. */
    @Transactional
    public void raiseFees(Booking b) {
        PlatformSettings s = settings.get();
        LocalDate due = LocalDate.now().plusDays(s.getGraceDays());

        // Every skilled worker, casual or permanent, pays a percentage for clients found on the hub.
        addInvoice(b.getWorker().getUser(), b, InvoiceKind.WORKER_COMMISSION, s.getWorkerCommissionPercent(), due);

        // Clients pay only to hire permanent workers (and for casual work if that is ever switched on).
        BigDecimal clientPct = b.getType() == BookingType.PERMANENT ? s.getPermanentHirePercent() : s.getClientCasualPercent();
        if (clientPct.signum() > 0) {
            addInvoice(b.getClient(), b, InvoiceKind.CLIENT_FEE, clientPct, due);
        }
    }

    private void addInvoice(User payer, Booking b, InvoiceKind kind, BigDecimal percent, LocalDate due) {
        Invoice i = new Invoice();
        i.setPayer(payer);
        i.setBooking(b);
        i.setKind(kind);
        i.setPercent(percent);
        i.setAmount(pct(b.getAgreedAmount(), percent));
        i.setDueDate(due);
        invoices.save(i);
    }

    public List<Invoice> forUser(User u) {
        return invoices.findByPayerOrderByCreatedAtDesc(u);
    }

    public Invoice get(Long id) {
        return invoices.findById(id).orElseThrow(() -> new BusinessException("error.notfound"));
    }

    @Transactional
    public void pay(Long invoiceId, User payer, String network, String phone) {
        Invoice inv = get(invoiceId);
        if (!inv.getPayer().getId().equals(payer.getId())) throw new BusinessException("error.forbidden");
        if (inv.getStatus() == InvoiceStatus.PAID) throw new BusinessException("error.invoice.paid");

        String ref = gateway.charge(network, phone, inv.getAmount(), "AHH invoice #" + inv.getId());
        inv.setStatus(InvoiceStatus.PAID);
        inv.setPaidAt(LocalDateTime.now());
        inv.setPaymentReference(ref);
        inv.setPaymentNetwork(network);
        invoices.save(inv);

        if (inv.getKind() == InvoiceKind.WORKER_COMMISSION) {
            workers.findByUser(payer).ifPresent(this::evaluate);
        }
    }

    /** Lock a worker with an overdue commission; unlock automatically once everything overdue is paid. */
    @Transactional
    public void evaluate(WorkerProfile w) {
        boolean overdue = invoices.existsByPayerAndKindAndStatusAndDueDateBefore(
                w.getUser(), InvoiceKind.WORKER_COMMISSION, InvoiceStatus.DUE, LocalDate.now());
        if (overdue && !w.isLocked()) {
            w.setStatus(AccountStatus.LOCKED);
            w.setLockReason(LOCK_UNPAID);
            workers.save(w);
            log.info("Locked worker {} for unpaid commission", w.getUser().getEmail());
        } else if (!overdue && w.isLocked() && LOCK_UNPAID.equals(w.getLockReason())) {
            w.setStatus(AccountStatus.ACTIVE);
            w.setLockReason(null);
            workers.save(w);
            log.info("Unlocked worker {} after payment", w.getUser().getEmail());
        }
    }

    /** True if the worker has any unpaid commission at all (used to warn before the deadline). */
    public boolean hasOutstanding(User worker) {
        return invoices.existsByPayerAndKindAndStatus(worker, InvoiceKind.WORKER_COMMISSION, InvoiceStatus.DUE);
    }

    /** Clients with an unpaid permanent-hire fee cannot start another permanent hire. */
    public boolean clientHasOverdueFee(User client) {
        return invoices.existsByPayerAndKindAndStatusAndDueDateBefore(
                client, InvoiceKind.CLIENT_FEE, InvoiceStatus.DUE, LocalDate.now());
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public int enforceLocks() {
        int before = (int) workers.countByStatus(AccountStatus.LOCKED);
        workers.findAll().forEach(this::evaluate);
        return (int) workers.countByStatus(AccountStatus.LOCKED) - before;
    }

    @Transactional
    public void adminLock(Long workerId) {
        WorkerProfile w = workers.findById(workerId).orElseThrow();
        w.setStatus(AccountStatus.LOCKED);
        w.setLockReason(LOCK_ADMIN);
        workers.save(w);
    }

    @Transactional
    public void adminUnlock(Long workerId) {
        WorkerProfile w = workers.findById(workerId).orElseThrow();
        w.setStatus(AccountStatus.ACTIVE);
        w.setLockReason(null);
        workers.save(w);
        evaluate(w); // re-locks immediately if commission is still overdue
    }

    public List<Invoice> all() { return invoices.findAllByOrderByCreatedAtDesc(); }
    public BigDecimal totalDue() { return invoices.totalByStatus(InvoiceStatus.DUE); }
    public BigDecimal totalPaid() { return invoices.totalByStatus(InvoiceStatus.PAID); }
}
