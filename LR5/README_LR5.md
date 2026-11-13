# ЛР5. JWT Access/Refresh для Recycling

Используются Java 17, Maven, Spring Boot 3.5.4, Spring Security, Spring Data JPA, MySQL и Postman.

## Что добавлено

- `UserSession` и `SessionStatus` (`ACTIVE`, `USED`, `REVOKED`).
- `UserSessionRepository` с блокировкой строки на время обновления refresh-токена.
- `JwtService`: создание и проверка JWT с HS256, `sub`, `jti`, `type`, `iat`, `exp`.
- `JwtAuthenticationFilter`: принимает только access-токен из `Authorization: Bearer ...`.
- `UserSessionService`: вход, регистрация с выдачей пары и атомарное обновление пары.
- `POST /auth/login`, `POST /auth/register`, `POST /auth/refresh`.
- Коллекция Postman: `postman/LR5_JWT.postman_collection.json`.

## Важные замечания

- Время жизни задаётся в `application.yaml`: access — 15 минут, refresh — 7 дней.
- Сроки в БД сохраняются с точностью до секунды, как `exp` JWT.
- Access и refresh имеют разные значения claim `type`.
- В `user_sessions` сами токены хранятся в `TEXT`. Для уникальности refresh используется дополнительный уникальный SHA-256 хеш (`refresh_token_hash`), потому что MySQL не поддерживает обычный полный UNIQUE-индекс на поле `TEXT`.
- При повторном использовании refresh со статусом `USED` запись переводится в `REVOKED`, а запрос получает `401`. Для этого отказ возвращается штатно из транзакции, чтобы изменение статуса не откатилось.
- CSRF отключён, поскольку это REST API с Bearer-токенами, а не авторизация через cookie/браузерную сессию. HTTP Basic больше не используется.
- Секрет в YAML предназначен только для учебного локального запуска. Для реального развёртывания задавай собственный `JWT_SECRET`.
