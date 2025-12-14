-- ===================================================================
-- UPDATE WEIGHT FOR user_id = 4
-- Để test progress bar tăng lên
-- ===================================================================

-- Option 1: Update current weight (today) lên 60kg
-- Progress sẽ = (60 - 58) / (65 - 58) * 100 = 2/7 * 100 = 28.5%
UPDATE body_measurements 
SET weight_kg = 60.0
WHERE user_id = 4 
  AND date_recorded = CURDATE();

-- Nếu không có measurement cho today, insert mới
INSERT INTO body_measurements (user_id, weight_kg, height_cm, date_recorded)
SELECT 4, 60.0, 170.0, CURDATE()
WHERE NOT EXISTS (
    SELECT 1 FROM body_measurements 
    WHERE user_id = 4 AND date_recorded = CURDATE()
);

-- Option 2: Update weight trong 30 days để có trend tăng dần
-- (Uncomment nếu muốn)
/*
UPDATE body_measurements 
SET weight_kg = CASE 
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 29 DAY) THEN 58.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 28 DAY) THEN 58.2
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 27 DAY) THEN 58.4
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 26 DAY) THEN 58.6
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 25 DAY) THEN 58.8
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 24 DAY) THEN 59.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 23 DAY) THEN 59.2
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 22 DAY) THEN 59.4
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 21 DAY) THEN 59.6
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 20 DAY) THEN 59.8
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 19 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 18 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 17 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 16 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 15 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 14 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 13 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 12 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 11 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 10 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 9 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 8 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 7 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 6 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 5 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 4 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 3 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 2 DAY) THEN 60.0
    WHEN date_recorded = DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN 60.0
    WHEN date_recorded = CURDATE() THEN 60.0
    ELSE weight_kg
END
WHERE user_id = 4;
*/

-- ===================================================================
-- VERIFY
-- ===================================================================

-- Check current weight
SELECT date_recorded, weight_kg, height_cm 
FROM body_measurements 
WHERE user_id = 4 
ORDER BY date_recorded DESC
LIMIT 5;

-- Expected: Latest weight should be 60.0 kg
-- Progress should be: (60 - 58) / (65 - 58) * 100 = 28.5%

-- ===================================================================
-- DONE! 
-- Restart app và check Profile screen
-- Progress bar should show: 28% hoàn thành • Còn tăng 5.0 kg
-- ===================================================================

