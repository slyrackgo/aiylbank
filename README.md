## Aiyl Bank - Internal Transfer Service

REST-сервис для внутренних переводов денежных средств между счетами банка.

### Основные возможности

- **Перевод средств** между счетами банка: `POST /api/transfers`
- **Переводы за последний месяц**: `GET /api/transfers/last-month`
- **Переводы за период**: `GET /api/transfers?from=YYYY-MM-DD&to=YYYY-MM-DD`
- **Общая сумма переводов за период**: `GET /api/transfers/total?from=YYYY-MM-DD&to=YYYY-MM-DD`
- **Счета с отрицательным балансом**: `GET /api/accounts/negative-balance`
- **Выписка по счету с пагинацией**:
  - `GET /api/accounts/{accountNumber}/statement`
  - Параметры:
    - `from` (опционально, `YYYY-MM-DD`)
    - `to` (опционально, `YYYY-MM-DD`)
    - `page` (номер страницы, начиная с 0)
    - `size` (размер страницы)

Выписка содержит: дату операции, тип (`DEBIT`/`CREDIT`), сумму и баланс после операции.

### SQL

Файл `src/main/resources/queries.sql` содержит:

- **ТОП-5 счетов по количеству транзакций за последний месяц**
- **Общая сумма переводов за выбранный период**
- **Счета с отрицательным балансом**
- **Оптимизация**: индекс `idx_transfers_created_at` на таблице `transfers` по полю `created_at`, чтобы ускорить запросы с фильтрацией по дате (уменьшается объём сканируемых строк и ускоряется поиск по временным интервалам).

### Запуск через Docker.

1. Перейти в корень проекта (`aiylbank` там, где находится `pom.xml` и `docker-compose.yml`).
2. Выполнить команду:

```bash
docker-compose up --build
```

Приложение поднимется на порту `8080`, PostgreSQL — на порту `5432`.

Для остановки:

```bash
docker-compose down
```

