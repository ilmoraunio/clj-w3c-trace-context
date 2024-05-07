(ns clj-w3c-trace-context.core
  (:require [clojure.string :as str])
  (:import (java.util UUID)
           (java.util.concurrent ThreadLocalRandom)))

(defn -assert
  [assertion message]
  (when-not assertion (throw (ex-info (str "assert failed: " message) {}))))

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

(def -re-version #"([0-9a-f]{2})")
(def -re-trace-id #"([0-9a-f]{32})")
(def -re-parent-id #"([0-9a-f]{16})")
(def -re-trace-flags #"([0-9a-f]{2})")
(def -re-traceparent (re-pattern (format "^%s-%s-%s-%s$"
                                         (str -re-version)
                                         (str -re-trace-id)
                                         (str -re-parent-id)
                                         (str -re-trace-flags))))

(defn parse-traceparent
  "Parses a W3C Trace Context traceparent. Produces a hash-map containing the field values of a traceparent.

   Generates a new random span ID whenever function is invoked."
  [s]
  (when s
    (when-let [[_ version trace-id parent-id trace-flags :as _trace-context] (re-find -re-traceparent s)]
      {:version version
       :trace-id trace-id
       :parent-id parent-id
       :span-id (gen-span-id)
       :trace-flags trace-flags})))

(defn traceparent
  "Returns a W3C Trace Context traceparent. Either provides a new traceparent containing the same trace-id if provided
  with `current-traceparent`, upon nil value will provide a new traceparent with new trace-id.

  Supports overriding specific field values:

  - `version`: represents an 8-bit unsigned integer as a string, \"00\" by default
  - `trace-id`: Single consistent ID logical operation. Used to correlate all actions in the overall distributed trace,
    random by default.
  - `span-id`: ID of the current span/operation, random by default. Acts as the update value for parent ID.
  - `trace-flags`: 8-bit field to control tracing flags such as sampling, trace level, etc. \"00\" by default."
  ([]
   (traceparent nil))
  ([current-traceparent]
   (traceparent current-traceparent nil))
  ([current-traceparent overridable-field-values]
   (let [{:keys [version trace-id span-id trace-flags] :as _trace-context} (parse-traceparent current-traceparent)]
     (when-let [version (:version overridable-field-values)]
       (-assert (re-matches -re-version version) "invalid format"))
     (when-let [trace-id (:trace-id overridable-field-values)]
       (-assert (re-matches -re-trace-id trace-id) "invalid format"))
     (when-let [span-id (:span-id overridable-field-values)]
       (-assert (re-matches -re-parent-id span-id) "invalid format"))
     (when-let [trace-flags (:trace-flags overridable-field-values)]
       (-assert (re-matches -re-trace-flags trace-flags) "invalid format"))
     (format "%s-%s-%s-%s"
             (or (:version overridable-field-values) version current-version)
             (or (:trace-id overridable-field-values) trace-id (gen-trace-id))
             (or (:span-id overridable-field-values) span-id null-parent-id)
             (or (:trace-flags overridable-field-values) trace-flags not-sampled-trace-flag)))))

(defn traceparent*
  [overridable-field-values]
  (traceparent nil overridable-field-values))