package com.africanheritagehub.repo;

import com.africanheritagehub.domain.Booking;
import com.africanheritagehub.domain.BookingStatus;
import com.africanheritagehub.domain.User;
import com.africanheritagehub.domain.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByClientOrderByCreatedAtDesc(User client);
    List<Booking> findByWorkerOrderByCreatedAtDesc(WorkerProfile worker);
    long countByStatus(BookingStatus status);
}
