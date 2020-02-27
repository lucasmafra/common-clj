(ns common-clj.http-client.interceptors.path-params-replacer
  (:require [io.pedestal.interceptor :as interceptor]
            [selmer.parser :as sp]
            [clojure.string :as str]
            [schema.core :as s]))

(defn- keywords->placeholders [^String url, replace-map]
  ;; Convert conventional :id style placeholders to handlebars style for Selmer
  (str/replace url #":[^/\.]*" (fn [^String match]
                                 (let [key (.substring match 1 (.length match))]
                                   (if (find replace-map (keyword key))
                                     (str "{{" key "}}")
                                     (throw (IllegalArgumentException.
                                             (str "Missing path-param \"" key
                                                  "\" on url \"" url "\""))))))))

(def path-params-replacer
  (interceptor/interceptor
   {:name ::path-params-replacer
    :enter (fn [{:keys [endpoints endpoint] {:keys [path-params]} :options :as context}]
             (let [{:keys [path path-params-schema]} (endpoints endpoint)
                   path-template (keywords->placeholders path path-params)
                   path-replaced (sp/render path-template path-params)]
               (when path-params-schema
                 (s/validate path-params-schema path-params))
               (when (and (not path-params-schema) path-params)
                 (throw (AssertionError. ":path-params is present on request but there's no path-params-schema for endpoint " endpoint)))
               (assoc-in context [:path-replaced] path-replaced)))}))
