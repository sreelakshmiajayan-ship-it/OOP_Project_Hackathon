# OOP_Project_Hackathon

## Inpatient admission and bed allocation

Back up the `hospital_management` database, confirm there are no duplicate
active room allocations, then apply
[`database/migrations/20261022_inpatient_admission_beds.sql`](database/migrations/20261022_inpatient_admission_beds.sql)
once using MySQL Workbench or the MySQL command-line client:

```powershell
mysql -u root -p hospital_management < database\migrations\20261022_inpatient_admission_beds.sql
```

Check for duplicate active allocations first with
`SELECT room_number, COUNT(*) FROM admissions WHERE status = 'Active' GROUP BY room_number HAVING COUNT(*) > 1;`
and resolve any returned rows before migrating. The migration adds ward and
bed details to existing rooms, a discharge timestamp, and a generated-column
unique key that prevents two active admissions from sharing a room. It
preserves existing rows and maps legacy rooms to the `General` ward. Do not
re-import the schema dump to apply this migration.

From PowerShell, compile and launch the application from the project root:

```powershell
$env:DB_PASSWORD = "your-local-mysql-password"
javac -encoding UTF-8 -cp "lib\*" -d out (Get-ChildItem -Path "src\main\java" -Recurse -Filter "*.java" | ForEach-Object { $_.FullName })
java -cp "out;lib\*" app.Main
```

The application connects to `jdbc:mysql://localhost:3306/hospital_management`
as `root`; set `DB_PASSWORD` in the process environment before launching.
Use Java 11 or later.