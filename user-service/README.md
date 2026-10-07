# User service

Micronaut CRUD service for users, backed by an in-memory H2 database.

## Run

Requires Java 25.

```powershell
.\gradlew run
```

## API

| Method | Path | Result |
| --- | --- | --- |
| `GET` | `/users` | List users |
| `GET` | `/users/{id}` | Get a user, or `404` |
| `POST` | `/users` | Create a user, returning `201` |
| `PUT` | `/users/{id}` | Update a user, or `404` |
| `DELETE` | `/users/{id}` | Delete a user, returning `204` or `404` |

Create and update requests accept JSON with `name` and `email` fields:

```json
{
  "name": "Nguyen Van A",
  "email": "a@example.com"
}
```
