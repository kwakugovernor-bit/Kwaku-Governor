# African Heritage Hub

A marketplace connecting skilled workers in Ghana (nurses, teachers, masons, carpenters, caregivers, seamstresses, truck owners and more) with people who need them.
Built with Java 17, Spring Boot 3, Thymeleaf, Spring Security and JPA. UI languages: English (default), French, Spanish.

## Run
```
mvn spring-boot:run          # then open http://localhost:8080
```
- Admin login: `admin@africanheritagehub.com` / `ChangeMe123!` (override with `ADMIN_EMAIL`, `ADMIN_PASSWORD`).
- Demo workers (password `Demo12345`) are created on first start; disable with `DEMO_DATA=false`.
- Data lives in `./data` (H2) and photos in `./uploads`. Switch to PostgreSQL/MySQL in `application.properties` for production.

## Business rules
- Clients: free. Workers create a profile with photos, details and a rate per hour/day/job.
- Every worker pays a commission (default 10%) on each booking they accept (casual or permanent); invoice is due in 7 days.
- Permanent hires: the client pays a one-time fee (default 15% of one month's pay).
- Unpaid commission past its due date automatically locks the worker (hidden from search, cannot accept work) until paid. Hourly job + check on each dashboard visit.
- Phone numbers are shared only after the worker accepts.
- Admin can change all percentages, lock/unlock, verify workers, view invoices.

## Before going live
- Replace `SimulatedPaymentGateway` with a real mobile-money provider (MTN MoMo, Telecel, AirtelTigo, Hubtel, Paystack). It currently moves no money.
- Replace `src/main/resources/static/images/logo.svg` with your logo (keep the name, or update `layout.html`).
- Change the admin password; use HTTPS and a production database.
