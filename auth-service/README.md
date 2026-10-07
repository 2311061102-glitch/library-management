# Auth service

Authentication and API-key service for the library microservices system.

## Configuration

Set these environment variables before starting the service:

```text
DB_PASSWORD=<MySQL password>
JWT_SECRET=<shared secret used by all services>
```

`DB_USERNAME` is optional and defaults to `root`.

## Run and test

```bash
mvn test
mvn spring-boot:run
```
