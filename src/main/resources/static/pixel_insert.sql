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
            WHILE j < 4156
                DO
                    SET current_lon = upper_left_lon + (j * lon_per_pixel);
                    INSERT INTO pixel (coordinate, x, y, created_at, modified_at)
                    VALUES (ST_GeomFromText(CONCAT('POINT(', current_lat, ' ', current_lon, ')'), 4326), i, j, NOW(),
                            NOW());
                    SET j = j + 1;
                END WHILE;
            SET current_lon = upper_left_lon; -- Reset current longitude to the initial value
            SET i = i + 1;
        END WHILE;
    COMMIT;
END $$
DELIMITER ;
-- Call the stored procedure to insert the coordinates
CALL InsertPixel();