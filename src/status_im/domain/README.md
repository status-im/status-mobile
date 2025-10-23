## Domain layer

### Description
This folder contains "domain" layer.
Here belongs all the code responsible for storing "persistent" data which is data that remains useful after any user flow. 
Data as `accounts`, `transactions`, `tokens` managed in this layer and stored normalized in rf-db under this layer.

### Responsibilities
- storing data in a convenient structures and in a normalized form
- keeping functions to operate on domain data

### Rules
- data on domain level modified only by domain events, never from other layers
- domain level knows nothing about UI and user flows 
