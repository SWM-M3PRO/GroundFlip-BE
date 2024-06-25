use groundflip_develop;
DROP PROCEDURE IF EXISTS insertPixel;
DELIMITER $$
CREATE PROCEDURE insertPixel()
BEGIN
    DECLARE lat_per_pixel DOUBLE DEFAULT 0.0000724;
    DECLARE lon_per_pixel DOUBLE DEFAULT 0.000909;
    DECLARE upper_left_lat DOUBLE DEFAULT 38.240675;
    DECLARE upper_left_lon DOUBLE DEFAULT 125.905952;
    DECLARE current_lat DOUBLE;
    DECLARE current_lon DOUBLE;
    DECLARE i BIGINT DEFAULT 0;
    DECLARE j BIGINT DEFAULT 0;
    SET current_lat = upper_left_lat;
    SET current_lon = upper_left_lon;

    START TRANSACTION;
    WHILE i < 7000
        DO
            SET current_lat = upper_left_lat - (i * lat_per_pixel);
            SET j = 0;
            SET @bulk_query = 'INSERT INTO pixel (latitude, longitude, x, y, created_at, modified_at) VALUES ';
            WHILE j < 4156
                DO
                    SET @aaa = @aaa + 1;
                    SET current_lon = upper_left_lon + (j * lon_per_pixel);
                    SET @bulk_query = CONCAT(@bulk_query, '(', current_lat, ', ', current_lon, ', ', i, ', ', j,
                                             ', NOW(), NOW()) ,');
                    SET j = j + 1;
                END WHILE;
            SET @bulk_query = SUBSTRING(@bulk_query, 1, length(@bulk_query) - 1);

            PREPARE stmt FROM @bulk_query;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            SET current_lon = upper_left_lon; -- Reset current longitude to the initial value
            SET i = i + 1;
        END WHILE;
    COMMIT;

END $$
DELIMITER ;

CALL InsertPixel();