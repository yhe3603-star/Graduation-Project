#!/usr/bin/env bash
# setup-ssl.sh - Provision Let's Encrypt SSL certificates for Dong Medicine
# Usage: ./setup-ssl.sh <domain> [email]
# Example: ./setup-ssl.sh dongmedicine.com admin@dongmedicine.com

set -euo pipefail

DOMAIN="${1:?Usage: $0 <domain> [email]}"
EMAIL="${2:-admin@${DOMAIN}}"
SSL_DIR="/etc/nginx/ssl"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*"
}

error() {
    log "ERROR: $*" >&2
    exit 1
}

# Install certbot if not present
install_certbot() {
    if command -v certbot &>/dev/null; then
        log "certbot is already installed: $(certbot --version 2>&1)"
        return
    fi

    log "Installing certbot..."
    if command -v apt-get &>/dev/null; then
        apt-get update -qq
        apt-get install -y -qq certbot python3-certbot-nginx
    elif command -v yum &>/dev/null; then
        yum install -y -q certbot python3-certbot-nginx
    elif command -v dnf &>/dev/null; then
        dnf install -y -q certbot python3-certbot-nginx
    else
        error "Unsupported package manager. Install certbot manually."
    fi
    log "certbot installed successfully"
}

# Ensure SSL directory exists
prepare_ssl_dir() {
    log "Preparing SSL directory: ${SSL_DIR}"
    mkdir -p "${SSL_DIR}"
    chmod 700 "${SSL_DIR}"
}

# Request certificate using certbot standalone mode
request_certificate() {
    log "Requesting Let's Encrypt certificate for ${DOMAIN}..."

    # Stop nginx temporarily if running to free port 80
    if docker ps --format '{{.Names}}' 2>/dev/null | grep -q 'dong-medicine-frontend'; then
        log "Stopping frontend container temporarily for domain verification..."
        docker stop dong-medicine-frontend 2>/dev/null || true
        local stopped_frontend=true
    fi

    certbot certonly \
        --standalone \
        --non-interactive \
        --agree-tos \
        --email "${EMAIL}" \
        -d "${DOMAIN}" \
        --preferred-challenges http

    # Restart frontend if we stopped it
    if [[ "${stopped_frontend:-}" == "true" ]]; then
        log "Restarting frontend container..."
        docker start dong-medicine-frontend 2>/dev/null || true
    fi

    log "Certificate obtained successfully"
}

# Copy certificates to nginx SSL directory
copy_certificates() {
    local letsencrypt_dir="/etc/letsencrypt/live/${DOMAIN}"

    if [[ ! -f "${letsencrypt_dir}/fullchain.pem" ]]; then
        error "Certificate files not found at ${letsencrypt_dir}"
    fi

    log "Copying certificates to ${SSL_DIR}..."
    cp "${letsencrypt_dir}/fullchain.pem" "${SSL_DIR}/fullchain.pem"
    cp "${letsencrypt_dir}/privkey.pem"   "${SSL_DIR}/privkey.pem"
    chmod 600 "${SSL_DIR}/privkey.pem"
    chmod 644 "${SSL_DIR}/fullchain.pem"
    log "Certificates copied to ${SSL_DIR}"
}

# Reload nginx inside the frontend container
reload_nginx() {
    log "Reloading nginx configuration..."
    if docker ps --format '{{.Names}}' 2>/dev/null | grep -q 'dong-medicine-frontend'; then
        docker exec dong-medicine-frontend nginx -s reload
        log "nginx reloaded successfully"
    else
        log "WARNING: frontend container is not running. Start it with: docker compose up -d frontend"
    fi
}

# Set up auto-renewal cron job
setup_cron() {
    local cron_job="0 3 * * 1 certbot renew --quiet --deploy-hook '${SCRIPT_DIR}/setup-ssl.sh --renew ${DOMAIN}'"

    # Check if cron job already exists
    if crontab -l 2>/dev/null | grep -qF 'certbot renew'; then
        log "Renewal cron job already exists"
        return
    fi

    log "Adding auto-renewal cron job..."
    (crontab -l 2>/dev/null || true; echo "${cron_job}") | crontab -
    log "Auto-renewal cron job added (runs every Monday at 3:00 AM)"
}

# Handle renewal deployment (called by certbot --deploy-hook)
handle_renewal() {
    DOMAIN="${2:?Usage: $0 --renew <domain>}"
    copy_certificates
    reload_nginx
    log "Certificate renewal completed for ${DOMAIN}"
    exit 0
}

# Main
main() {
    if [[ "${1:-}" == "--renew" ]]; then
        handle_renewal "$@"
    fi

    log "=== Dong Medicine SSL Setup ==="
    log "Domain: ${DOMAIN}"
    log "Email:  ${EMAIL}"

    install_certbot
    prepare_ssl_dir
    request_certificate
    copy_certificates
    reload_nginx
    setup_cron

    log "=== SSL setup complete ==="
    log "Certificates are in: ${SSL_DIR}"
    log "Auto-renewal is configured via cron"
}

main "$@"
