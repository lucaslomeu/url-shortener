### TODO:
- [ ] Add project description
- [ ] Add excalidraw with architecture diagram
- [ ] Implement index postgres
- [ ] Unit tests
- [ ] How scale horizontally or vertically
- [ ] Add CI/CD
- [ ] Add monitoring and logging
- [ ] Add security best practices
- [ ] Add more examples of usage
- [ ] Endpoint to get url info (clicks, creation date, etc)

### To bring up the project
- Docker and Docker-compose
- Virtual Threads
- Queue
- Use my kafka service (notification)

## How to test v1
1. Start gradle
```
./gradlew run
```

2. Run curl command
```
curl -X POST http://localhost:8080/shorten -d "http://www.exemplo.com"
```

3. Get the original URL with the short URL
```
curl http://localhost:8080/1
```