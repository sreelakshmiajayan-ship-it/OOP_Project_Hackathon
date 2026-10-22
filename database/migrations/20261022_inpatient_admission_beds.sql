-- Run once against hospital_management after backing up the database.
-- Existing room/admission rows are retained. Legacy rooms are assigned to
-- the General ward and receive a stable bed number based on room_id.

ALTER TABLE rooms
    ADD COLUMN ward_name VARCHAR(100) NOT NULL DEFAULT 'General'
        AFTER room_type,
    ADD COLUMN bed_no INT NULL AFTER ward_name;

UPDATE rooms
SET bed_no = room_id
WHERE bed_no IS NULL;

ALTER TABLE rooms
    ADD UNIQUE KEY uq_rooms_ward_bed (ward_name, bed_no);

ALTER TABLE admissions
    ADD COLUMN discharge_date DATETIME NULL
        AFTER admission_date,
    ADD COLUMN active_room_number VARCHAR(20)
        GENERATED ALWAYS AS (
            CASE WHEN status = 'Active' THEN room_number ELSE NULL END
        ) STORED,
    ADD UNIQUE KEY uq_admissions_active_room (active_room_number);

-- Keep the room availability flag consistent with existing active admissions.
UPDATE rooms r
SET r.available = FALSE
WHERE EXISTS (
    SELECT 1
    FROM admissions a
    WHERE a.room_number = r.room_number
      AND a.status = 'Active'
);
