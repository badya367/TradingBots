SELECT
   name
FROM
    sqlite_schema
WHERE 1 = 1
    AND type ='table'
    AND name NOT LIKE 'sqlite_%';
