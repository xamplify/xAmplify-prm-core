#!/bin/bash
set -e
echo "Restoring database backup..."
pg_restore -U postgres -d xamplify-prm /docker-entrypoint-initdb.d/01-restore.backup || echo "Restore completed with warnings"

# Create test user
echo "Creating test user..."
psql -U postgres -d xamplify-prm << 'EOF'
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'users') THEN
        INSERT INTO users (
            username, email, password, enabled, account_non_expired,
            account_non_locked, credentials_non_expired, created_date
        )
        SELECT 'testuser', 'test@example.com',
               '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9P2.nRs.fSvXe66',
               true, true, true, true, NOW()
        WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'test@example.com');

        IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'user_roles')
           AND EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'roles') THEN
            INSERT INTO user_roles (user_id, role_id)
            SELECT u.id, r.id FROM users u, roles r
            WHERE u.email = 'test@example.com' AND r.name = 'ROLE_USER'
            AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
        END IF;
    END IF;
END
$$;
EOF

echo "Database setup completed"
