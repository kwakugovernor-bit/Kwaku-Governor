package com.africanheritagehub.repo;

import com.africanheritagehub.domain.AccountStatus;
import com.africanheritagehub.domain.User;
import com.africanheritagehub.domain.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long>, JpaSpecificationExecutor<WorkerProfile> {
    Optional<WorkerProfile> findByUser(User user);
    long countByStatus(AccountStatus status);
    List<WorkerProfile> findByStatus(AccountStatus status);
}
