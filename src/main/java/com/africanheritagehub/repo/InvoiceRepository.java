package com.africanheritagehub.repo;

import com.africanheritagehub.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPayerOrderByCreatedAtDesc(User payer);
    List<Invoice> findAllByOrderByCreatedAtDesc();
    boolean existsByPayerAndKindAndStatusAndDueDateBefore(User payer, InvoiceKind kind, InvoiceStatus status, LocalDate date);
    boolean existsByPayerAndKindAndStatus(User payer, InvoiceKind kind, InvoiceStatus status);

    @Query("select coalesce(sum(i.amount), 0) from Invoice i where i.status = :status")
    BigDecimal totalByStatus(InvoiceStatus status);
}
