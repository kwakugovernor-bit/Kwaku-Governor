# Deploying African Heritage Hub

Runs on any small Linux server (2 GB RAM is plenty to start) with Docker. Examples: DigitalOcean, Hetzner, AWS Lightsail, Vultr.

1. **Create a server** (Ubuntu 22.04/24.04) and install Docker: `curl -fsSL https://get.docker.com | sh`
2. **Point your domain** (e.g. `hub.yourdomain.com`) at the server's IP with an `A` record. Open ports 80 and 443 in the firewall.
3. **Get the code**: `git clone <your repo> && cd Kwaku-Governor && git checkout claude/nifty-lovelace-2gzbny`
4. **Configure**: `cp .env.example .env` and edit it (domain, database password, admin email and a strong admin password).
5. **Start**: `docker compose up -d --build`. Caddy fetches the HTTPS certificate automatically. Open `https://your-domain`.
6. **Update later**: `git pull && docker compose up -d --build`.

The app refuses to start in production with the default admin password.

## Backups (do this before real users arrive)
- Database: `docker compose exec db pg_dump -U ahhub ahhub > backup-$(date +%F).sql` (run daily with cron, copy off the server).
- Photos: the `uploads` Docker volume.

## Before accepting real money
- Connect a real mobile-money provider in place of `SimulatedPaymentGateway`.
- Add a privacy policy and terms page (required by the app stores and by Ghana's Data Protection Act).
- Consider Flyway database migrations once real data exists (`ddl-auto=update` is fine to start but not for big schema changes).

## Then publish the phone apps
Put the domain in `mobile/capacitor.config.json` and follow `mobile/README.md`.
