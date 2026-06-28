# TinyURL HLD Practice

This folder contains the High-Level Design notes and Excalidraw diagram for a TinyURL-style URL shortener based on the TechPrep discussion.

## Requirements captured

### Functional
- POST /create-url
- GET /{short-url}

### Non-functional
- POST: low latency
- GET: high availability

### Scale
- 1000 writes/sec
- 10:1 read:write ratio
- Approximately 31.5 billion URLs per year
- Approximately 300 billion reads per year

## API contract
- POST /create-url
  - Request: long-url
  - Response: 201 Created
  - Returns short-url
- GET /{short-url}
  - Response: 301 Permanent Redirect
  - Redirects to long-url

## Data model
- long-url: string
- short-url: string
- created-at: timestamp
- Optional: created-by / expiry

## Design notes
- Short key generation using Base62
- Alphabet: a-z, A-Z, 0-9 (62 chars)
- 6 chars gives ~56B combinations; 7 chars gives ~3.5T
- Use distributed ID/counter for uniqueness
- Cache hot mappings in Redis
- Primary DB for persistence
- GET uses cache first, DB fallback
