package com.africanheritagehub.service;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.repo.BookingRepository;
import com.africanheritagehub.repo.ReviewRepository;
import com.africanheritagehub.repo.WorkerProfileRepository;
import com.africanheritagehub.web.form.BookingForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final WorkerProfileRepository workers;
    private final ReviewRepository reviews;
    private final BillingService billing;

    public BookingService(BookingRepository bookings, WorkerProfileRepository workers,
                          ReviewRepository reviews, BillingService billing) {
        this.bookings = bookings;
        this.workers = workers;
        this.reviews = reviews;
        this.billing = billing;
    }

    public List<Booking> forClient(User c) { return bookings.findByClientOrderByCreatedAtDesc(c); }
    public List<Booking> forWorker(WorkerProfile w) { return bookings.findByWorkerOrderByCreatedAtDesc(w); }

    private Booking load(Long id) {
        return bookings.findById(id).orElseThrow(() -> new BusinessException("error.notfound"));
    }

    @Transactional
    public Booking create(User client, Long workerId, BookingForm f) {
        WorkerProfile w = workers.findById(workerId).orElseThrow(() -> new BusinessException("error.notfound"));
        if (w.isLocked() || !w.isComplete()) throw new BusinessException("error.worker.unavailable");
        if (w.getUser().getId().equals(client.getId())) throw new BusinessException("error.self.booking");

        Booking b = new Booking();
        b.setClient(client);
        b.setWorker(w);
        b.setType(f.getType());
        b.setStartDate(f.getStartDate());
        b.setMessage(f.getMessage());

        if (f.getType() == BookingType.PERMANENT) {
            if (!w.isOffersPermanent()) throw new BusinessException("error.not.permanent");
            if (f.getOfferedAmount() == null || f.getOfferedAmount().signum() <= 0) throw new BusinessException("error.offer.required");
            if (billing.clientHasOverdueFee(client)) throw new BusinessException("error.client.overdue");
            b.setQuantity(1);
            b.setAgreedAmount(f.getOfferedAmount());
        } else {
            if (!w.isOffersCasual()) throw new BusinessException("error.not.casual");
            int qty = Math.max(1, f.getQuantity());
            b.setQuantity(qty);
            b.setAgreedAmount(w.getRate().multiply(BigDecimal.valueOf(qty)));
        }
        return bookings.save(b);
    }

    @Transactional
    public void accept(User workerUser, Long id) {
        Booking b = load(id);
        requireWorker(b, workerUser);
        if (b.getStatus() != BookingStatus.PENDING) throw new BusinessException("error.booking.state");
        if (b.getWorker().isLocked()) throw new BusinessException("error.locked");
        b.setStatus(BookingStatus.ACCEPTED);
        bookings.save(b);
        billing.raiseFees(b);
    }

    @Transactional
    public void decline(User workerUser, Long id) {
        Booking b = load(id);
        requireWorker(b, workerUser);
        if (b.getStatus() != BookingStatus.PENDING) throw new BusinessException("error.booking.state");
        b.setStatus(BookingStatus.DECLINED);
        bookings.save(b);
    }

    @Transactional
    public void cancel(User client, Long id) {
        Booking b = load(id);
        if (!b.getClient().getId().equals(client.getId())) throw new BusinessException("error.forbidden");
        if (b.getStatus() != BookingStatus.PENDING && b.getStatus() != BookingStatus.ACCEPTED)
            throw new BusinessException("error.booking.state");
        b.setStatus(BookingStatus.CANCELLED);
        bookings.save(b);
    }

    @Transactional
    public void complete(User user, Long id) {
        Booking b = load(id);
        boolean party = b.getClient().getId().equals(user.getId()) || b.getWorker().getUser().getId().equals(user.getId());
        if (!party) throw new BusinessException("error.forbidden");
        if (b.getStatus() != BookingStatus.ACCEPTED) throw new BusinessException("error.booking.state");
        b.setStatus(BookingStatus.COMPLETED);
        bookings.save(b);
    }

    @Transactional
    public void review(User client, Long id, int rating, String comment) {
        Booking b = load(id);
        if (!b.getClient().getId().equals(client.getId())) throw new BusinessException("error.forbidden");
        if (b.getStatus() != BookingStatus.COMPLETED || reviews.existsByBooking(b)) throw new BusinessException("error.booking.state");
        if (rating < 1 || rating > 5) throw new BusinessException("error.rating");

        Review r = new Review();
        r.setBooking(b);
        r.setRating(rating);
        r.setComment(comment);
        reviews.save(r);

        WorkerProfile w = b.getWorker();
        double total = w.getRatingAvg() * w.getRatingCount() + rating;
        w.setRatingCount(w.getRatingCount() + 1);
        w.setRatingAvg(Math.round(total / w.getRatingCount() * 10.0) / 10.0);
        workers.save(w);
    }

    public boolean isReviewed(Booking b) { return reviews.existsByBooking(b); }

    private void requireWorker(Booking b, User u) {
        if (!b.getWorker().getUser().getId().equals(u.getId())) throw new BusinessException("error.forbidden");
    }
}
