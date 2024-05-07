# clj-w3c-trace-context

A micro library for parsing & passing W3C Trace Contexts.

In scope:

- `traceparent` header
- `tracestate` header (contributions welcome)

## Installation

```clojure
{:deps {ilmoraunio/clj-w3-trace-context {:git/url "https://github.com/ilmoraunio/clj-w3c-trace-context"
                                         :sha "d7e2e49f047d00832016868d0eda0913f78556d0"}}}
```

## Usage

Basic example:

```clojure
(require '[clj-w3c-trace-context.core :as w3c])

(w3c/traceparent)
; => "00-0e08516218404124aa5edef73a2f45a4-0000000000000000-00"

(let [trace (w3c/traceparent)
      another-trace (w3c/traceparent (w3c/traceparent trace))]
  [trace another-trace])

; => ["00-b26e88803f5a46adae542f87a26023f5-0000000000000000-00" "00-844cea87d30a44e1b7a7329653fa731c-b0a078895360ef64-00"]
```

Overriding values (such as trace-flags):

```clojure
(w3c/traceparent* {:trace-id "3cde554288b04a62bdd33fef575e43cf" :trace-flags "01"})

; => "00-3cde554288b04a62bdd33fef575e43cf-0000000000000000-01"
```

Field values are overridable for `traceparent`, see tests for more examples.
