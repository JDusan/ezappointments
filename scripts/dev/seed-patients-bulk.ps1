param(
    [int]$Count = 1000000,
    [string]$Output = "build/dev-data/patients.csv",
    [string]$Container = "ezappointments-postgres",
    [string]$Database = "ezappointments",
    [string]$Username = "ezappointments"
)

$ErrorActionPreference = "Stop"

$firstNames = @(
    "Ada", "Grace", "Katherine", "Alan", "Barbara", "Mark", "Margaret", "Edsger",
    "Mary", "John", "Patricia", "Sammy", "Jennifer", "Michael", "Linda", "William",
    "Elizabeth", "David", "Susan", "Richard", "Jessica", "Joseph", "Sarah", "Thomas"
)
$middleNames = @("", "", "", "Ann", "Lee", "Marie", "James", "Rose", "Paul", "Jane")
$lastNames = @(
    "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
    "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson",
    "Thomas", "Taylor", "Moore", "Jackson", "Martin", "Lee", "Perez", "Thompson",
    "White", "Harris", "Sanchez", "Clark", "Ramirez", "Lewis", "Robinson", "Walker",
    "Young", "Allen", "King", "Wright", "Scott", "Torres", "Nguyen", "Hill", "Hopper"
)

$outputPath = Resolve-Path -Path "." | ForEach-Object { Join-Path $_ $Output }
$outputDir = Split-Path -Parent $outputPath
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

Write-Host "Generating $Count patients into $outputPath"

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$writer = [System.IO.StreamWriter]::new($outputPath, $false, $utf8NoBom)
$random = [System.Random]::new(42)

try {
    $writer.WriteLine("id,first_name,middle_name,last_name,date_of_birth,created_at")

    for ($i = 1; $i -le $Count; $i++) {
        $id = [guid]::NewGuid().ToString()
        $firstName = $firstNames[$random.Next($firstNames.Count)]
        $middleName = $middleNames[$random.Next($middleNames.Count)]
        $lastName = $lastNames[$random.Next($lastNames.Count)]
        $dateOfBirth = (Get-Date "1940-01-01").AddDays($random.Next(0, 25567)).ToString("yyyy-MM-dd")
        $createdAt = "2026-01-01T00:00:00Z"

        $writer.WriteLine("$id,$firstName,$middleName,$lastName,$dateOfBirth,$createdAt")

        if ($i % 100000 -eq 0) {
            Write-Host "Generated $i rows"
        }
    }
}
finally {
    $writer.Dispose()
}

Write-Host "Copying CSV into container $Container"
docker cp $outputPath "${Container}:/tmp/patients.csv"

Write-Host "Importing patients with PostgreSQL COPY"
docker exec $Container psql -U $Username -d $Database -c "\copy patient(id, first_name, middle_name, last_name, date_of_birth, created_at) FROM '/tmp/patients.csv' CSV HEADER"

Write-Host "Refreshing PostgreSQL planner statistics"
docker exec $Container psql -U $Username -d $Database -c "ANALYZE patient;"

Write-Host "Done. Imported $Count patients."
