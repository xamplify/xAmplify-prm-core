-- Fix OAuth client details if table exists
DO $$
BEGIN
    IF EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'oauth_client_details') THEN
        UPDATE oauth_client_details SET client_secret = 'xAmplify-prm-secret' WHERE client_id = 'xAmplify-prm-client';
        RAISE NOTICE 'OAuth client details updated successfully';
    ELSE
        RAISE NOTICE 'oauth_client_details table does not exist, skipping update';
    END IF;
END
$$;
