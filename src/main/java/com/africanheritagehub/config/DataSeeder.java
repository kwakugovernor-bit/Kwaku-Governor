package com.africanheritagehub.config;

import com.africanheritagehub.domain.*;
import com.africanheritagehub.repo.UserRepository;
import com.africanheritagehub.repo.WorkerProfileRepository;
import com.africanheritagehub.service.SettingsService;
import com.africanheritagehub.service.UserService;
import com.africanheritagehub.web.form.RegistrationForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Creates the admin account, default fee settings and (optionally) a few demo workers on first start. */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final UserService userService;
    private final WorkerProfileRepository profiles;
    private final SettingsService settings;
    private final String adminEmail;
    private final String adminPassword;
    private final boolean demo;

    public DataSeeder(UserRepository users, UserService userService, WorkerProfileRepository profiles,
                      SettingsService settings,
                      @Value("${app.admin.email}") String adminEmail,
                      @Value("${app.admin.password}") String adminPassword,
                      @Value("${app.demo-data:false}") boolean demo, Environment env) {
        if (env.acceptsProfiles(Profiles.of("prod")) && "ChangeMe123!".equals(adminPassword)) {
            throw new IllegalStateException("Refusing to start in production with the default admin password. Set ADMIN_PASSWORD.");
        }
        this.users = users;
        this.userService = userService;
        this.profiles = profiles;
        this.settings = settings;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.demo = demo;
    }

    @Override
    public void run(String... args) {
        settings.get();
        if (!users.existsByEmailIgnoreCase(adminEmail)) {
            userService.createAdmin(adminEmail, adminPassword);
            log.warn("Admin account created: {}  (set ADMIN_PASSWORD to change the default password!)", adminEmail);
        }
        if (demo && users.countByRole(Role.WORKER) == 0) seedDemo();
    }

    private void seedDemo() {
        demoWorker("Ama Serwaa", "ama@demo.gh", Category.SEAMSTRESS, "Kente & bridal dressmaker", "Kumasi", "Ashanti",
                "120", RateUnit.DAY, 9, false, "Specialises in kente, kaba & slit, and bridal wear. Fittings at my shop in Adum.",
                "Sewing machines: 3 industrial. Fabrics: kente, wax print, lace. Turnaround: 5–7 days.");
        demoWorker("Kojo Mensah", "kojo@demo.gh", Category.TRUCK_OWNER, "10-tonne tipper truck for hire", "Tema", "Greater Accra",
                "1800", RateUnit.DAY, 12, true, "Sand, stone and building material haulage across Greater Accra and Eastern region.",
                "Vehicle: Howo 10-tonne tipper, 2019. Plate: GT 4821-19. Insured. Driver and fuel included on request.");
        demoWorker("Efua Quaye", "efua@demo.gh", Category.NURSE, "Registered nurse – home care", "Accra", "Greater Accra",
                "45", RateUnit.HOUR, 7, true, "Wound dressing, injections, post-operative care and elderly home visits.",
                "Registered General Nurse (NMC Ghana). First-aid certified. Night shifts available.");
        demoWorker("Yaw Boateng", "yaw@demo.gh", Category.CARPENTER, "Furniture & roofing carpenter", "Takoradi", "Western",
                "150", RateUnit.DAY, 15, false, "Custom wardrobes, doors, roofing timber and kitchen cabinets in local hardwood.",
                "Own tools and a small workshop. Materials can be supplied at cost.");
        demoWorker("Abena Owusu", "abena@demo.gh", Category.TEACHER, "Maths & Science tutor (JHS/SHS)", "Cape Coast", "Central",
                "35", RateUnit.HOUR, 6, true, "BECE and WASSCE preparation, small groups or one-to-one, in person or online.",
                "B.Ed Mathematics, University of Cape Coast. Past-question drills included.");
        demoWorker("Issah Mahama", "issah@demo.gh", Category.MASON, "Block-work and plastering mason", "Tamale", "Northern",
                "130", RateUnit.DAY, 11, false, "Foundations, block laying, plastering and tiling. Team of four available.",
                "Team of 4. References from two completed estates in Tamale.");
        log.info("Demo workers created (password: Demo12345 – disable with DEMO_DATA=false)");
    }

    private void demoWorker(String name, String email, Category cat, String headline, String city, String region,
                            String rate, RateUnit unit, int years, boolean permanent, String bio, String details) {
        RegistrationForm f = new RegistrationForm();
        f.setFullName(name);
        f.setEmail(email);
        f.setPhone("0244000000");
        f.setPassword("Demo12345");
        f.setRegion(region);
        f.setRole(Role.WORKER);
        f.setCategory(cat);
        f.setAcceptTerms(true);
        User u = userService.register(f);
        WorkerProfile p = profiles.findByUser(u).orElseThrow();
        p.setHeadline(headline);
        p.setCity(city);
        p.setBio(bio);
        p.setDetails(details);
        p.setRate(new BigDecimal(rate));
        p.setRateUnit(unit);
        p.setYearsExperience(years);
        p.setOffersPermanent(permanent);
        p.setVerified(true);
        profiles.save(p);
    }
}
