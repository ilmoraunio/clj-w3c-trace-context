(ns clj-w3c-trace-context.core
  (:require [clojure.test :refer :all]
            [clj-w3c-trace-context.core :refer :all]))

(deftest test-parse-traceparent
  (testing "example good traceparent parses correctly"
    (let [trace (parse-traceparent "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00")]
     (is (some? (:span-id trace)))
     (is (= {:version "00"
             :trace-id "4bf92f3577b34da6a3ce929d0e0e4736"
             :parent-id "00f067aa0ba902b7"
             :trace-flags "00"}
            (dissoc trace :span-id)))))
  (testing "tolerates nil"
    (is (nil? (parse-traceparent nil))))
  (testing "tolerates invalid traceparent"
    (is (nil? (parse-traceparent "hölynpöly")))))

(deftest test-traceparent
  (testing "tolerates no arguments"
    (let [trace (parse-traceparent (traceparent))]
      (is (some? trace))
      (is (= (:parent-id trace) null-parent-id))))
  (testing "example good traceparent outputs valid traceparent"
    (let [trace (parse-traceparent "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00")
          a-traceparent (traceparent trace)]
      (is (not= trace a-traceparent))
      (let [another-trace (parse-traceparent a-traceparent)]
        (is (= (:trace-id trace) (:trace-id another-trace)))
        (is (some? (:span-id trace)))
        (is (some? (:span-id another-trace)))
        (is (not= (:span-id trace) (:span-id another-trace))))))
  (testing "second trace span receives a non-null span ID"
    (let [trace (parse-traceparent "00-091435af82ba422f91d0feeb0eb19670-0000000000000000-00")
          another-trace (parse-traceparent (traceparent trace))]
      (is (= (:parent-id trace) null-parent-id))
      (is (not= (:parent-id another-trace) null-parent-id))))
  (testing "tolerates nil"
    (let [trace (parse-traceparent (traceparent nil))]
      (is (some? trace))
      (is (= (:parent-id trace) null-parent-id))))
  (testing "supports overriding"
    (is (str/starts-with? (traceparent {:version "01"}) "01-"))
    (is (str/starts-with? (traceparent {:trace-id "10f6fc664b4a4581b1031ba7cd1dd5df"}) "00-10f6fc664b4a4581b1031ba7cd1dd5df-"))
    (is (str/ends-with? (traceparent {:span-id "00f067aa0ba902b7"}) "-00f067aa0ba902b7-00"))
    (is (str/ends-with? (traceparent {:trace-flags "01"}) "-0000000000000000-01"))
    (is (= (traceparent {:version "01"
                         :trace-id "10f6fc664b4a4581b1031ba7cd1dd5df"
                         :span-id "00f067aa0ba902b7"
                         :trace-flags "01"})
           "01-10f6fc664b4a4581b1031ba7cd1dd5df-00f067aa0ba902b7-01"))
    (is (= (traceparent {:version "01"
                         :trace-id "10f6fc664b4a4581b1031ba7cd1dd5df"
                         :trace-flags "01"})
           (format "01-10f6fc664b4a4581b1031ba7cd1dd5df-%s-01" null-parent-id)))))
