package com.africanheritagehub.service;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.repo.*;
import com.africanheritagehub.web.form.WorkerForm;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkerService {
    public static final int MAX_PHOTOS = 8;

    private final WorkerProfileRepository profiles;
    private final ReviewRepository reviews;
    private final FileStorageService files;

    public WorkerService(WorkerProfileRepository profiles, ReviewRepository reviews, FileStorageService files) {
        this.profiles = profiles;
        this.reviews = reviews;
        this.files = files;
    }

    /** Public search: only complete, active (not locked) profiles are ever listed. */
    public List<WorkerProfile> search(Category category, String region, String q, boolean permanentOnly) {
        Specification<WorkerProfile> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), AccountStatus.ACTIVE));
            ps.add(cb.isNotNull(root.get("rate")));
            ps.add(cb.isNotNull(root.get("headline")));
            if (category != null) ps.add(cb.equal(root.get("category"), category));
            if (region != null && !region.isBlank()) ps.add(cb.equal(root.get("region"), region));
            if (permanentOnly) ps.add(cb.isTrue(root.get("offersPermanent")));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("headline")), like),
                        cb.like(cb.lower(root.get("bio")), like),
                        cb.like(cb.lower(root.get("city")), like),
                        cb.like(cb.lower(root.get("details")), like),
                        cb.like(cb.lower(root.join("user").get("fullName")), like)));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return profiles.findAll(spec, Sort.by(Sort.Order.desc("verified"), Sort.Order.desc("ratingAvg")));
    }

    public List<WorkerProfile> featured(int n) {
        return search(null, null, null, false).stream().limit(n).toList();
    }

    public WorkerProfile get(Long id) {
        return profiles.findById(id).orElseThrow(() -> new BusinessException("error.notfound"));
    }

    public WorkerProfile forUser(User u) {
        return profiles.findByUser(u).orElseThrow(() -> new BusinessException("error.notfound"));
    }

    @Transactional
    public void saveProfile(User user, WorkerForm f) {
        WorkerProfile p = forUser(user);
        p.setCategory(f.getCategory());
        p.setHeadline(f.getHeadline().trim());
        p.setBio(f.getBio());
        p.setCity(f.getCity());
        p.setRegion(f.getRegion());
        p.setRate(f.getRate());
        p.setRateUnit(f.getRateUnit());
        p.setYearsExperience(f.getYearsExperience());
        p.setOffersCasual(f.isOffersCasual());
        p.setOffersPermanent(f.isOffersPermanent());
        p.setDetails(f.getDetails());

        if (f.getPhotos() != null) {
            for (MultipartFile file : f.getPhotos()) {
                if (file.isEmpty()) continue;
                if (p.getPhotos().size() >= MAX_PHOTOS) throw new BusinessException("error.photos.max");
                p.getPhotos().add(files.storeImage(file));
            }
        }
        profiles.save(p);
    }

    @Transactional
    public void removePhoto(User user, String name) {
        WorkerProfile p = forUser(user);
        if (p.getPhotos().remove(name)) {
            files.delete(name);
            profiles.save(p);
        }
    }

    public List<Review> reviewsFor(WorkerProfile p) {
        return reviews.findByBooking_WorkerOrderByCreatedAtDesc(p);
    }
}
