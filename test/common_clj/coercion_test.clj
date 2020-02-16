(ns common-clj.coercion-test
  (:require [common-clj.coercion :as coercion]
            [common-clj.test-helpers :refer [coercion-error?]]
            [midje.sweet :refer :all]
            [schema.core :as s])
  (:import clojure.lang.ExceptionInfo))

#_(s/with-fn-validation
  (facts "coerce"
    (fact "BigDecimal"
      (coercion/coerce java.math.BigDecimal 20) => 20M)

    (fact "LocalDate"
      (coercion/coerce java.time.LocalDate "2019-08-22") => #local-date "2019-08-22")

    (fact "LocalDateTime"
      (coercion/coerce java.time.LocalDateTime "2019-08-22T12:52:37")
      => #local-date-time "2019-08-22T12:52:37")

    (fact "when it can't coerce it throws schema error"
      (coercion/coerce java.math.BigDecimal "twenty")
      => (throws ExceptionInfo coercion-error?))))
