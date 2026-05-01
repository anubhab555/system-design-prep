# System Design Questions

Java implementations and interview notes for common system design topics. The
repo is organized so each component has source code, tests, and documentation
that can be extended as your prep list grows.

## Components

| Component | Source | Docs | Main Ideas |
|-----------|--------|------|------------|
| Rate Limiter | `src/main/java/com/systemdesign/ratelimiter` | `docs/ratelimiter` | fixed window, sliding window, token bucket, leaky bucket |
| Consistent Hashing | `src/main/java/com/systemdesign/consistenthashing` | `docs/consistenthashing` | hash ring, virtual nodes, minimal redistribution |
| Key-Value Store | `src/main/java/com/systemdesign/keyvaluestore` | `docs/keyvaluestore` | GET/PUT/DELETE, TTL, versioning, replication, partitioning |

## Project Layout

```text
.
|-- README.md
|-- build.gradle
|-- settings.gradle
|-- docs/
|   |-- README.md
|   |-- INTERVIEW_PREP.md
|   |-- ratelimiter/
|   |-- consistenthashing/
|   `-- keyvaluestore/
`-- src/
    |-- main/java/com/systemdesign/
    |   |-- ratelimiter/
    |   |-- consistenthashing/
    |   `-- keyvaluestore/
    `-- test/java/com/systemdesign/
```

## Commands

```bash
gradle clean build
gradle test
gradle demoRateLimiter
gradle demoConsistentHashing
gradle demoKeyValueStore
```

## Documentation Entry Points

- `docs/README.md` - documentation index
- `docs/INTERVIEW_PREP.md` - one-stop project-wide prep guide
- `docs/ratelimiter/INTERVIEW_PREP.md` - focused rate limiter interview script
- `docs/ratelimiter/HLD.md` and `docs/ratelimiter/LLD.md` - rate limiter design notes
- `docs/consistenthashing/INTERVIEW_PREP.md` - focused consistent hashing interview script
- `docs/consistenthashing/HLD.md` and `docs/consistenthashing/LLD.md` - consistent hashing design notes
- `docs/keyvaluestore/INTERVIEW_PREP.md` - focused key-value store interview script
- `docs/keyvaluestore/HLD.md` - distributed key-value store HLD
- `docs/keyvaluestore/LLD.md` - in-memory key-value store LLD and code mapping

## Adding New Components

Use the same shape for each new topic:

```text
src/main/java/com/systemdesign/<component>/
src/test/java/com/systemdesign/<component>/
docs/<component>/HLD.md
docs/<component>/LLD.md
docs/<component>/INTERVIEW_PREP.md
```

Keep the root small, put learning material under `docs/`, and add a Gradle demo
task only when the component has an executable example.
