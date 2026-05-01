# System Design Interview Docs

This folder is the main study hub for the project. Root-level files stay small;
deep interview preparation lives here.

## Recommended Path

1. Read `INTERVIEW_PREP.md` for the project-wide system design framework.
2. Study one component at a time:
   - `ratelimiter/HLD.md`, `ratelimiter/LLD.md`, and `ratelimiter/INTERVIEW_PREP.md`
   - `consistenthashing/HLD.md`, `consistenthashing/LLD.md`, and `consistenthashing/INTERVIEW_PREP.md`
   - `keyvaluestore/HLD.md`, `keyvaluestore/LLD.md`, and `keyvaluestore/INTERVIEW_PREP.md`
3. Read the matching Java implementation under `src/main/java/com/systemdesign`.
4. Run the tests and demos for the component.

## Component Map

| Component | HLD | LLD | Interview Prep | Demo Command |
|-----------|-----|-----|----------------|--------------|
| Rate Limiter | `ratelimiter/HLD.md` | `ratelimiter/LLD.md` | `ratelimiter/INTERVIEW_PREP.md` | `gradle demoRateLimiter` |
| Consistent Hashing | `consistenthashing/HLD.md` | `consistenthashing/LLD.md` | `consistenthashing/INTERVIEW_PREP.md` | `gradle demoConsistentHashing` |
| Key-Value Store | `keyvaluestore/HLD.md` | `keyvaluestore/LLD.md` | `keyvaluestore/INTERVIEW_PREP.md` | `gradle demoKeyValueStore` |

## Documentation Style

Each component should answer the interview in this order:

1. Problem and scope
2. Functional requirements
3. Non-functional requirements
4. Scale estimates
5. API and data model
6. High-level architecture
7. Core read/write flows
8. Deep dives and trade-offs
9. Failure handling
10. Interview talking points

This mirrors the common HLD interview structure used in popular prep material,
while keeping the wording and examples original to this repository.
