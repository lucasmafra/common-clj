(ns dev
  (:require [com.stuartsierra.component :as component]
            [com.stuartsierra.component.repl :refer [set-init]]))

(def system
  (component/system-map))

(set-init (constantly system))
