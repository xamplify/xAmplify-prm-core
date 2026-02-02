#!/bin/bash
set -e

echo "Starting database initialization..."

# Wait for PostgreSQL to be ready
until pg_isready -U postgres; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 2
done

echo "PostgreSQL is ready. Restoring database backup..."

# Restore the database backup
if [ -f /tmp/restore.backup ]; then
    echo "Restoring database from backup..."
    pg_restore -U postgres -d xamplify-prm -v /tmp/restore.backup || {
        echo "pg_restore failed, but continuing..."
    }
else
    echo "No backup file found at /tmp/restore.backup"
fi

echo "Database initialization completed."
