(ns common-clj.schema-helpers
  (:require [clojure.walk :refer [postwalk prewalk prewalk-demo postwalk-demo]]
            [clojure.core.match :refer [match]]
            [schema.utils :refer [named-error-explain validation-error-explain]]
            [schema.core :as s])
  (:import (schema.utils NamedError ValidationError)))

(defn loose-schema [schema]
  (assoc schema s/Keyword s/Any))
