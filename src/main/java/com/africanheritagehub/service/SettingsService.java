package com.africanheritagehub.service;

import com.africanheritagehub.domain.PlatformSettings;
import com.africanheritagehub.repo.PlatformSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private final PlatformSettingsRepository repo;

    public SettingsService(PlatformSettingsRepository repo) {
        this.repo = repo;
    }

    public PlatformSettings get() {
        return repo.findById(PlatformSettings.SINGLETON_ID).orElseGet(() -> repo.save(new PlatformSettings()));
    }

    public PlatformSettings save(PlatformSettings s) {
        s.setId(PlatformSettings.SINGLETON_ID);
        return repo.save(s);
    }
}
