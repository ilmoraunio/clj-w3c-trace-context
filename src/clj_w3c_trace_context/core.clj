(ns clj-w3c-trace-context.core
  (:require [clojure.string :as str])
  (:import (java.util UUID)
           (java.util.concurrent ThreadLocalRandom)))

(def null-parent-id
  "Used for first span of a distributed trace."
  "0000000000000000")

(def current-version
  "The current specification assumes the version is set to 00."
  "00")

(def not-sampled-trace-flag
  "00")

(defn gen-trace-id
  []
  (str/replace (str (UUID/randomUUID)) #"-" ""))

(defn gen-span-id
  []
  (format "%016x" (.nextLong (ThreadLocalRandom/current))))

(defn parse-traceparent
  "Parses a W3C Trace Context traceparent. Produces a hash-map containing the field values of a traceparent."
  [s]
  (when s
    (when-let [[_ version trace-id parent-id trace-flags :as _trace-context] (re-find #"^([0-9a-f]{2})-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})$" s)]
      {:version version
       :trace-id trace-id
       :parent-id parent-id
       :span-id (gen-span-id)
       :trace-flags trace-flags})))

(defn traceparent
  "Outputs a W3C Trace Context traceparent. Supports overriding specific field values:

  - `version`: represents an 8-bit unsigned integer as a string, \"00\" by default
  - `trace-id`: Single consistent ID logical operation. Used to correlate all actions in the overall distributed trace,
    random by default.
  - `span-id`: ID of the current span/operation, random by default.
  - `trace-flags`: 8-bit field to control tracing flags such as sampling, trace level, etc. \"00\" by default."
  ([]
   (traceparent nil))
  ([{:keys [version trace-id span-id trace-flags] :as _trace-context}]
   (when (some? version)
     (assert (= (count version) 2))
     (assert (not= version "ff")))
   (when (some? trace-id)
     (assert (= (count trace-id) 32))
     (assert (not= trace-id "00000000000000000000000000000000")))
   (when (some? span-id)
     (assert (= (count span-id) 16)))
   (when (some? trace-flags)
     (assert (= (count trace-flags) 2)))
   (format "%s-%s-%s-%s"
           (or version current-version)
           (or trace-id (gen-trace-id))
           (or span-id null-parent-id)
           (or trace-flags not-sampled-trace-flag))))
