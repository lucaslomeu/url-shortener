## URL Shortener – Java 21 (Pure Java)

URL Shortener implemented using:
- Java 21 (Virtual Threads)
- Hexagonal Architecture (Ports & Adapters)
- JDBC + PostgreSQL
- No Spring / No Framework

### TODO ORDER LIST:
- [X] Create relational table
- [X] Create index in database for faster searches
- [ ] Generate Unique ID for short URL
- [ ] Hashing Base62 short URL without collisions using Unique ID
- [ ] Add params in properties/env
- [ ] Hashing algorithm para short url
- [ ] Endpoint to get url info (stats)

- [ ] Unit tests
- [ ] Add project description
- [ ] Add architecture diagram (Hexagonal)

- [ ] Implement cache (Caffeine → Redis)
- [ ] Virtual Threads (documentation)
- [ ] Queue (async click increment ou event)

- [ ] Structured Logging
- [ ] Monitoring (prometheus, grafana, etc)
- [ ] Security best practices

- [ ] How to scale horizontally or vertically
- [ ] Kafka service (notification)
- [ ] Add CI/CD