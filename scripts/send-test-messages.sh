#!/usr/bin/env bash
# Тестовые сообщения (для bash/WSL). Кидает инциденты разного уровня критичности.
# Использование: ./send-test-messages.sh

BASE="http://localhost:8080/api/messages"

send_msg() {
  local user_id="$1"
  local message="$2"
  curl -s -o /dev/null -w "OK [$user_id] %{http_code}\n" \
    -X POST "$BASE" \
    -H "Content-Type: application/json" \
    -d "{\"userId\":\"$user_id\",\"message\":\"$message\",\"timestamp\":\"$(date -u +%Y-%m-%dT%H:%M:%SZ)\"}"
}

echo "=== Бакет A1: сервер не отвечает, 500 (35 сообщений для эскалации) ==="
for i in $(seq 1 35); do
  send_msg "user-$i" "Не работает сервер, на все запросы возвращается ошибка 500, пользователи не могут зайти"
  sleep 0.3
done

echo "=== Бакет A4: поиск долго грузит ==="
for i in $(seq 1 5); do
  send_msg "user-${i}-search" "Поиск по каталогу стал очень долго грузиться, результаты приходят через минуту"
  sleep 0.3
done

echo "=== Бакет A2: не приходят уведомления ==="
send_msg "user-notif" "Перестали приходить уведомления на почту, письма не доходят вообще"
sleep 0.3
send_msg "user-notif-2" "Не отправляются email уведомления, рассылка сломалась"

echo "=== Бакет A3: медленный экспорт ==="
send_msg "user-export" "Экспорт отчёта в Excel падает с ошибкой на середине"
sleep 0.3
send_msg "user-export-2" "Не формируется выгрузка данных в файл, отчёт зависает"

echo "=== Бакет A1: база данных недоступна ==="
send_msg "user-db" "База данных не отвечает, все запросы падают с ошибкой подключения"
