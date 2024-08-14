INSERT INTO permission (user_id, marketing_notifications_enabled, service_notifications_enabled, created_at,
                        modified_at)
SELECT u.user_id, FALSE, FALSE, NOW(), NOW()
FROM user u
         LEFT JOIN permission p ON u.user_id = p.user_id
WHERE p.user_id IS NULL;