## Architecture Overview

This project implements a URL Shortener using Hexagonal Architecture (Ports & Adapters),
focusing on scalability, high availability, and fast redirection.

The system is designed to:
- Handle high read traffic with low latency
- Scale horizontally
- Be database and transport agnostic

## Architecture Diagram

The system follows Hexagonal Architecture principles.
// TODO: Add architecture diagram here

- HTTP is an inbound adapter
- JDBC/Postgres is an outbound adapter
- Business rules live inside the domain and application layers

## Key Design Decisions
1. Why Hexagonal Architecture?
   - Decouples business logic from infrastructure
   - Allows replacing HTTP server, database or cache without impacting the core
   - Improves testability
2. Why 302 instead of 301 for redirection?
   - 302 allows tracking click metrics
   - 301 would be cached by browsers/CDNs, preventing accurate click counting
   - Enables future cache layers without losing observability

## Data Model

Table: urls

Fields:
- id (BIGSERIAL, PK)
- short_code (VARCHAR, UNIQUE)
- original_url (TEXT)
- clicks (INT)
- creation_date (TIMESTAMP)
- updated_at (TIMESTAMP)

## Indexing Strategy

- Unique index on short_code for fast redirection
- Optional index on original_url for deduplication

## Caching Strategy (Planned)

- Hot URLs (most accessed) will be cached
- Cache key: short_code → original_url
- Cache TTL based on access frequency

## Scalability & Availability
1. Write 
   - Write-heavy operations are relatively low (1k/s)
   - Can be scaled horizontally using stateless application nodes
2. Read
   - Read-heavy workload (~11k/s)
   - Cache-first strategy ensures low latency
3. Database
   - Read replicas for scaling reads
   - Primary node for writes

## Observability (Planned)

- Structured logging
- Metrics:
    - Redirect latency
    - Top accessed URLs
- Prometheus + Grafana

## Security Considerations

- Input validation for URLs
- Rate limiting (future)
- Protection against abuse (spam URLs)

## Roadmap

The following improvements are planned to evolve the system while preserving architectural boundaries:

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