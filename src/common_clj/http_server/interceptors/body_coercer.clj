(ns common-clj.http-server.interceptors.body-coercer)

#_(def body-coercer
  (interceptor
   {:name  ::body-coercer
    :enter (fn [{:keys [request route] :as context}]
             (let [{:keys [json-params]}    request
                   {:keys [route-name]}     route
                   {:keys [request-schema]} (route-name routes)
                   coerced-body             (when request-schema
                                              (coerce request-schema json-params (or override-coercers default-coercers)))]
               (if request-schema
                 (do
                   (s/validate request-schema coerced-body)
                   (assoc-in context [:request :body] coerced-body))
                 context)))
    :leave (fn [{:keys [response route] :as context}]
             (let [{:keys [body]}            response
                   {:keys [route-name]}      route
                   {:keys [response-schema]} (route-name routes)
                   serialized-body           (-> body misc/dash->underscore json->string)]
               (s/validate response-schema body)
               (assoc-in context [:response :body] serialized-body)))}))
