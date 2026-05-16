# Assumptions

- The spec was updated conservatively: no paths, methods, or core endpoint semantics were changed.
- Delete endpoints now return 204 instead of 200 and an empty schema.
- Pagination was added only where the operation returns a collection:
  `POST /v1/patient/search`
  `GET /v1/appointment/doctor/{id}`
- Pagination is modeled with `limit` plus an opaque `cursor`/`nextCursor` pair
- For doctor appointment listing, pagination was added as optional query parameters rather than redesigning the endpoint or changing its response to a radically different contract.
- Added `409 Conflict` response to the delete patient and delete doctor endpoints to avoid hanging/invalid appointments
