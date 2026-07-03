-- 小農送審時管理員尚未介入，因此待審核紀錄的 admin_id 必須允許 NULL。
ALTER TABLE farm_trip_audits
    MODIFY COLUMN admin_id INT NULL;

-- 為導入新流程前已存在、但沒有 PENDING 審核紀錄的待審活動補資料。
INSERT INTO farm_trip_audits (
    farm_trip_id,
    admin_id,
    status,
    reason,
    created_at,
    updated_at
)
SELECT
    trip.farm_trip_id,
    NULL,
    'PENDING',
    NULL,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM farm_trip trip
WHERE trip.status = 'PENDING'
  AND NOT EXISTS (
      SELECT 1
      FROM farm_trip_audits audit
      WHERE audit.farm_trip_id = trip.farm_trip_id
        AND audit.status = 'PENDING'
  );
