(ns common-clj.http-server.interceptors.error
  (:require [common-clj.humanize :as humanize]
            [common-clj.json :as json]
            [io.pedestal.interceptor.error :as error-int])
  (:import (java.io StringWriter PrintWriter)))

(def error
  (error-int/error-dispatch
   [{:keys [env] :as ctx} ex]
   [{:type :schema-tools.coerce/error}]
   (let [error-map (->> ex
                        ex-data
                        :error
                        (#(humanize/explain % humanize/humanize)))
         body      (json/json->string {:error error-map})]
     (assoc ctx :response {:status 400 :body body}))

   :else
   (let [sw       (StringWriter.)
         pw       (PrintWriter. sw)
         e        (->> ex ex-data :exception)
         _        (when e
                    (.printStackTrace e pw))
         response (if (not= :prod env)
                    {:error       "Internal Server Error"
                     :stack-trace (str sw)}
                    {:error "Internal Server Error"})]
     (when (not= :prod env)
       (println ex))
     (assoc ctx :response {:status 500 :body (json/json->string response)}))))
