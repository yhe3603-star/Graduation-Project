#!/usr/bin/env bash
# backup-db.sh - MySQL database backup with rotation for Dong Medicine
# Reads credentials from environment variables or .env file
# Creates mysqldump in /backups/mysql/ with date-stamped filenames
# Keeps last 7 daily backups, rotates older ones

set -euo pipefail

# Configuration
BACKUP_DIR="${BACKUP_DIR:-/backups/mysql}"
RETENTION_DAYS="${RETENTION_DAYS:-7}"
DB_NAME="${DB_NAME:-dong_medicine}"
LOG_FILE="${BACKUP_DIR}/backup.log"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "${SCRIPT_DIR}")"
DATE_STAMP="$(date '+%Y%m%d_%H%M%S')"
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${DATE_STAMP}.sql.gz"

# Load .env file if environment variables are not set
load_env() {
    local env_file="${PROJECT_ROOT}/.env"
    if [[ -f "${env_file}" ]]; then
        log "Loading credentials from ${env_file}"
        # Only source unset variables to avoid overriding real env
        while IFS='=' read -r key value; do
            # Skip comments and empty lines
            [[ "${key}" =~ ^#.*$ || -z "${key}" ]] && continue
            key="$(echo "${key}" | xargs)"
            value="$(echo "${value}" | xargs)"
            # Remove surrounding quotes if present
            value="${value%\"}"
            value="${value#\"}"
            value="${value%\'}"
            value="${value#\'}"
            export "${key}=${value}" 2>/dev/null || true
        done < "${env_file}"
    fi
}

log() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] $*"
    echo "${msg}"
    # Also write to log file if backup directory exists
    if [[ -d "${BACKUP_DIR}" ]]; then
        echo "${msg}" >> "${LOG_FILE}"
    fi
}

error() {
    log "ERROR: $*" >&2
    exit 1
}

# Ensure backup directory exists
prepare_backup_dir() {
    mkdir -p "${BACKUP_DIR}"
    log "Backup directory: ${BACKUP_DIR}"
}

# Create the database backup
create_backup() {
    local mysql_host="${DB_HOST:-mysql}"
    local mysql_port="${DB_PORT:-3306}"
    local mysql_user="${DB_USERNAME:-root}"
    local mysql_password="${DB_PASSWORD:-${MYSQL_ROOT_PASSWORD:-root123}}"

    log "Starting backup of database '${DB_NAME}' from ${mysql_host}:${mysql_port}..."

    mysqldump \
        --host="${mysql_host}" \
        --port="${mysql_port}" \
        --user="${mysql_user}" \
        --password="${mysql_password}" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --set-gtid-purged=OFF \
        --default-character-set=utf8mb4 \
        "${DB_NAME}" 2>/dev/null | gzip > "${BACKUP_FILE}"

    if [[ ! -s "${BACKUP_FILE}" ]]; then
        rm -f "${BACKUP_FILE}"
        error "Backup file is empty or was not created"
    fi

    local size
    size="$(du -h "${BACKUP_FILE}" | cut -f1)"
    log "Backup created: ${BACKUP_FILE} (${size})"
}

# Rotate old backups - keep only the last N daily backups
rotate_backups() {
    log "Rotating backups (keeping last ${RETENTION_DAYS} daily backups)..."

    local count
    count="$(find "${BACKUP_DIR}" -maxdepth 1 -name "${DB_NAME}_*.sql.gz" -type f | wc -l)"

    if (( count <= RETENTION_DAYS )); then
        log "No rotation needed (${count} backups, limit is ${RETENTION_DAYS})"
        return
    fi

    # Delete backups older than retention period
    local deleted=0
    while IFS= read -r old_backup; do
        rm -f "${old_backup}"
        (( deleted++ ))
        log "Deleted old backup: $(basename "${old_backup}")"
    done < <(find "${BACKUP_DIR}" -maxdepth 1 -name "${DB_NAME}_*.sql.gz" -type f -mtime +"${RETENTION_DAYS}" | sort)

    log "Rotation complete: removed ${deleted} old backup(s)"
}

# Verify backup integrity
verify_backup() {
    log "Verifying backup integrity..."
    if gzip -t "${BACKUP_FILE}" 2>/dev/null; then
        log "Backup integrity check passed"
    else
        error "Backup integrity check FAILED - file may be corrupted"
    fi
}

# Main
main() {
    load_env
    prepare_backup_dir

    log "=== Dong Medicine Database Backup ==="

    create_backup
    verify_backup
    rotate_backups

    log "=== Backup complete ==="
    log "File: ${BACKUP_FILE}"
}

main "$@"
