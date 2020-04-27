# Change Log
### Changed [1.5.5]
- [consumer] Add simple kafka consumer. 
- [state-flow] Add state-flow helpers for kafka

### Changed [1.5.4]
- [producer] Add basic kafka producer. Broker address should be configured through `:kafka/brokers` key on `app.edn`.

### Changed [1.5.3]
- [http-client] Extend mock calls to work with functions of the request as well

### Changed [1.5.2]
- [http-client] Support `:extra-interceptors` override

### Changed [1.5.0]
- [clojure-test-helpers] Add macro `deftest` for testing with schema validation

### Changed [1.3.0]
- [coercion] Improve enum coercer to handle snake-cased values

### Changed [1.1.1]
- [http-server] Add CORS [*] when in dev mode
- [http-server] Add service-map override

### Changed [1.1.0]
- Add components http-client, http-server and config
- Provide test utilities for `state-flow`

