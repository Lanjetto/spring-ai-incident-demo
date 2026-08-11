# Тестовые сообщения: кидает инциденты разного уровня критичности.
# Сценарий "сервер не отвечает" повторяется 35 раз, чтобы показать эскалацию A4->A1.
# Использование: ./send-test-messages.ps1

$base = "http://localhost:8080/api/messages"

function Send-Msg([string]$userId, [string]$message) {
    $body = @{ userId = $userId; message = $message; timestamp = (Get-Date).ToUniversalTime().ToString("o") } | ConvertTo-Json
    try {
        $r = Invoke-RestMethod -Uri $base -Method Post -ContentType "application/json" -Body $body
        Write-Host "OK [$userId] $message  -> $r"
    } catch {
        Write-Host "FAIL [$userId] $message -> $($_.Exception.Message)"
    }
}

Write-Host "=== Бакет A1: сервер не отвечает, 500 (35 сообщений для эскалации) ==="
1..35 | ForEach-Object {
    Send-Msg "user-$($_)" "Не работает сервер, на все запросы возвращается ошибка 500, пользователи не могут зайти"
    Start-Sleep -Milliseconds 300
}

Write-Host "=== Бакет A4: поиск долго грузит ==="
1..5 | ForEach-Object {
    Send-Msg "user-$($_)-search" "Поиск по каталогу стал очень долго грузиться, результаты приходят через минуту"
    Start-Sleep -Milliseconds 300
}

Write-Host "=== Бакет A2: не приходят уведомления ==="
Send-Msg "user-notif" "Перестали приходить уведомления на почту, письма не доходят вообще"
Start-Sleep -Milliseconds 300
Send-Msg "user-notif-2" "Не отправляются email уведомления, рассылка сломалось"

Write-Host "=== Бакет A3: медленный экспорт ==="
Send-Msg "user-export" "Экспорт отчёта в Excel падает с ошибкой на середине"
Start-Sleep -Milliseconds 300
Send-Msg "user-export-2" "Не формируется выгрузка данных в файл, отчёт зависает"

Write-Host "=== Бакет A1: база данных недоступна ==="
Send-Msg "user-db" "База данных не отвечает, все запросы падают с ошибкой подключения"
