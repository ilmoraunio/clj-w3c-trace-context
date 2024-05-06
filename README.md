# clj-w3c-trace-context

A micro library for parsing & passing W3C Trace Contexts.

## Installation

```clojure
{:deps {ilmoraunio/clj-w3-trace-context {:git/url "https://github.com/ilmoraunio/clj-w3c-trace-context"
                                         :sha "48fd364f834abf65eb22f8bfd4dc6d5022d18b23"}}}
```

## Usage

Basic example:

```bash
user=> (require '[clj-w3c-trace-context.core :as w3c])
nil
user=> (w3c/traceparent)
"00-0e08516218404124aa5edef73a2f45a4-0000000000000000-00"
```

Parent ID becomes new upon each new traceparent (first traceparent in the
forest of spans is initialized with a null ID):

```bash
user=> (let [trace (w3c/traceparent)
             another-trace (w3c/traceparent (w3c/parse-traceparent (w3c/traceparent trace)))]
         [trace another-trace])
["00-b26e88803f5a46adae542f87a26023f5-0000000000000000-00" "00-844cea87d30a44e1b7a7329653fa731c-b0a078895360ef64-00"]
```

Overriding values (such as trace-flags):

```
user=> (w3c/traceparent {:trace-id "3cde554288b04a62bdd33fef575e43cf" :trace-flags "01"})
"00-3cde554288b04a62bdd33fef575e43cf-0000000000000000-01"
```

Every value is overridable for `traceparent`, see tests for more examples.
