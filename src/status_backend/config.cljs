(ns status-backend.config)

;; Before run:
;; adb reverse tcp:9050 tcp:9050

(defonce ws (js/WebSocket. "ws://localhost:9050/signals"))

(set! (.-onopen ws) (fn [] (js/alert "WS Connection opened!")))
(set! (.-onmessage ws) (fn [e] (js/alert "WS Message arrived") (def --m e)))
(set! (.-onerror ws) (fn [e] (js/alert "WS ERROR") (def --e e)))
(set! (.-onclose ws) (fn [e] (js/alert "WS Connection closed") (js/console.info (.-code e) (.-reason e))))

(defn fetch [url params callback]
  (let [js-params (cond-> params
                    (:body params) (update :body (comp js/JSON.stringify clj->js))
                    :always clj->js)]
    (-> (js/fetch url js-params)
        (.then callback))))

(def backend-port 9050)
(def backend-address (str "http://localhost:" backend-port "/statusgo/"))

(def ^:private backend-fetch-default-params
  {:method  :POST
   :headers #js{"Accept"       "application/json"
                "Content-Type" "application/json"}})

(defn backend-fetch
  ([endpoint body-params]
   (backend-fetch endpoint body-params (fn [])))
  ([endpoint body-params callback]
   (let [url    (str backend-address endpoint)
         params (assoc backend-fetch-default-params :body body-params)]
     (fetch url params callback))))

(def backend-obj
  (js/Proxy. #js{}
             #js{:get (fn [_target native-module-method]
                        (println "_________________NNM CALL:" native-module-method)
                        (partial backend-fetch native-module-method))}))

;; TODO: finish signals and check RPC calls

(comment
 ;; STEP 1
 (-> (fetch "http://localhost:9050/statusgo/InitializeApplication"
              {:method  :POST
               :headers {"Accept"       "application/json"
                         "Content-Type" "application/json"}
               :body    (-> {:dataDir       "/home/ulises/p/status-go/test-db"
                             :mixpanelAppId config/mixpanel-app-id
                             :mixpanelToken config/mixpanel-token}
                            (clj->js)
                            (js/JSON.stringify))})
       (.then (fn [response]
                (.json response)))
       (.then (fn [json]
                (js/alert (str "HTTP-RESPONSE:\n" (js->clj json)))))
       )
 ;; STEP 2
 (let [body (-> (status-im.contexts.profile.config/create)
                (assoc :displayName "Sonic"
                       :password (native-module/sha3 (utils.security.core/safe-unmask-data "Hola1234.,"))
                       :imagePath nil
                       :customizationColor :army)
                (clj->js)
                (js/JSON.stringify))]

   (-> (fetch "http://localhost:9050/statusgo/CreateAccountAndLogin"
              (clj->js
               {:method  :POST
                :headers {"Accept"       "application/json"
                          "Content-Type" "application/json"}
                :body    body}))
       (.then (fn [response]
                (.json response)))
       (.then (fn [json]
                (js/alert (str "HTTP-RESPONSE:\n" (js->clj json)))))
       (.catch (fn [json]
                 (js/alert (str "HTTP-RESPONSE-ERROR!:\n" (js->clj json)))))))

 ;;
 )
