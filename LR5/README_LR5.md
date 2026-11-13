# ЛР5. JWT Access/Refresh для Recycling

Проект основан на ЛР4. Используются Java 17, Maven, Spring Boot 3.5.4, Spring Security, Spring Data JPA, MySQL и Postman.

## Что добавлено

- `UserSession` и `SessionStatus` (`ACTIVE`, `USED`, `REVOKED`).
- `UserSessionRepository` с блокировкой строки на время обновления refresh-токена.
- `JwtService`: создание и проверка JWT с HS256, `sub`, `jti`, `type`, `iat`, `exp`.
- `JwtAuthenticationFilter`: принимает только access-токен из `Authorization: Bearer ...`.
- `UserSessionService`: вход, регистрация с выдачей пары и атомарное обновление пары.
- `POST /auth/login`, `POST /auth/register`, `POST /auth/refresh`.
- Коллекция Postman: `postman/LR5_JWT.postman_collection.json`.

## Настройка

1. Открой папку `LR4` в IntelliJ IDEA как Maven-проект.
2. Проверь Java 17 в Project Structure и Maven.
3. В `src/main/resources/application.yaml` оставлены переменные `DB_USERNAME` и `DB_PASSWORD`, как в твоей ЛР4. В IntelliJ задай их в Run Configuration → Environment variables. При необходимости создай сервисную роль и пользователя, выполнив `database/LR5_permissions.sql` в MySQL Workbench. Замени учебный пароль в SQL перед использованием.
4. Убедись, что база `recycling` уже существует. Таблицу `user_sessions` создаст Hibernate благодаря `ddl-auto: update`.
5. Нажми Maven Reload, затем запусти `RecyclingApplication`.
6. При первом запуске проверь в MySQL Workbench, что появилась таблица `user_sessions`.

## Запросы Postman

Импортируй `postman/LR5_JWT.postman_collection.json`. Запускай запросы по порядку:

1. Регистрация: `POST http://localhost:8080/auth/register` — ожидается `201`. Создаёт пользователя с ролью USER и сразу выдаёт пару токенов.
2. Вход: `POST http://localhost:8080/auth/login` — ожидается `200`.
3. Защищённый запрос: `GET http://localhost:8080/api/recycling-batches` с `Authorization: Bearer {{accessToken}}` — ожидается `200`.
4. Обновление: `POST http://localhost:8080/auth/refresh`, JSON содержит только `refreshToken` — ожидается `200`; коллекция сохраняет старый refresh и заменяет текущую пару.
5. Повтор старого refresh — ожидается `401`; в БД старая запись должна стать `REVOKED`, новая остаться `ACTIVE`.

Для регистрации используется `name`, `username`, `password`. Логин и пароль должны соответствовать проверкам `UserService`. Заголовок `X-Device-Id` необязателен.

## Проверка MySQL

Для приложения достаточно прав `SELECT`, `INSERT`, `UPDATE` и `DELETE` на базе `recycling`. SQL для создания отдельной роли и пользователя находится в `database/LR5_permissions.sql`. Таблицу сессий и её ограничения создаёт JPA по аннотациям `UserSession`; вручную создавать её не нужно.

```sql
SELECT id, username, device_id, status,
       access_token_expiry, refresh_token_expiry,
       refresh_token_hash
FROM user_sessions
ORDER BY access_token_expiry;
```

Ожидаемый сценарий после входа, обновления и повторной отправки старого refresh:

- старая запись: `REVOKED`;
- новая запись: `ACTIVE`;
- третьей записи после повторного старого refresh не появляется.

## Важные замечания

- Время жизни задаётся в `application.yaml`: access — 15 минут, refresh — 7 дней.
- Сроки в БД сохраняются с точностью до секунды, как `exp` JWT.
- Access и refresh имеют разные значения claim `type`.
- В `user_sessions` сами токены хранятся в `TEXT`. Для уникальности refresh используется дополнительный уникальный SHA-256 хеш (`refresh_token_hash`), потому что MySQL не поддерживает обычный полный UNIQUE-индекс на поле `TEXT`.
- При повторном использовании refresh со статусом `USED` запись переводится в `REVOKED`, а запрос получает `401`. Для этого отказ возвращается штатно из транзакции, чтобы изменение статуса не откатилось.
- CSRF отключён, поскольку это REST API с Bearer-токенами, а не авторизация через cookie/браузерную сессию. HTTP Basic больше не используется.
- Секрет в YAML предназначен только для учебного локального запуска. Для реального развёртывания задавай собственный `JWT_SECRET`.
