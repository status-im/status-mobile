## Gateway layer

### Description
This folder contains "gateway" layer.
Here belongs all the code responsible for communication with external data sources - APIs.
It abstracts the particular data source from the data itself.

### Responsibilities
- parsing network responses
- transforming backend data to internal formats

### Rules
- never processes business logic. Just passes it further to **Domain** layer
