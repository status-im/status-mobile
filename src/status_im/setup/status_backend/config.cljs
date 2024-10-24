(ns status-im.setup.status-backend.config
  (:require [clojure.string :as string]
            [oops.core :as oops]
            [re-frame.core :as rf]))

;; Before run:
;; adb reverse tcp:9050 tcp:9050

(defn fetch [url {:keys [body] :as params} callback]
  (let [js-params (cond-> params
                    (map? body)
                    (update :body (comp js/JSON.stringify clj->js))

                    :always
                    clj->js)]
    (def --fp js-params)
    (-> (js/fetch url js-params)
        (.then (fn [response]
                 (.text response)
                 ;(.json response)
                 ))
        (.then callback)
        (.catch (fn [error]
                  (prn "ERROR:" error)
                  ;(def --err error)
                  (js/alert "ERRROR:" (prn-str error))
                  ;(callback response)
                  )))))

(def backend-port 9050)
(def backend-address (str "http://localhost:" backend-port "/statusgo/"))

(def ^:private backend-fetch-default-params
  {:method  :POST
   :headers {"Accept"       "application/json"
             "Content-Type" "application/json"}})

#_(defn wait-for-end-point-response [check-fn interval]
  (js/Promise.
   (fn [resolve _]
     (let [interval-id (js/setInterval
                        (fn []
                          (let [val (check-fn)]
                            (when (some? val)
                              (js/clearInterval interval-id)
                              (resolve val))))
                        interval)]))))

(defn backend-fetch
  ([endpoint]
   (prn "Backend fetch 1 ------ " endpoint)
   (let [result-atom (atom nil)]
     (backend-fetch endpoint nil (fn [result]
                                   (reset! result-atom result)))
     ;; I need to wait for the execution HERE!
     ;; And then return:
     @result-atom
     ))
  ([endpoint body-params]
   (prn "Backend fetch 2 ------ " endpoint)
   (backend-fetch endpoint body-params (fn [])))
  ([endpoint body-params callback]
   (prn "Backend fetch!")
   (let [url    (str backend-address endpoint)
         params (assoc backend-fetch-default-params :body body-params)]
     (fetch url params callback))))

(def request-obj
  (js/Proxy. #js{}
             #js{:get (fn [_target native-module-method]
                        (let [method-name (str (string/capitalize (subs native-module-method 0 1))
                                               (subs native-module-method 1))]
                          (println "_________________NNM CALL:" method-name)
                          (partial backend-fetch method-name)))}))

(declare ws)
(def ws-address (str "http://localhost:" backend-port "/signals"))

(defn init-ws [on-message]
  (defonce ws (js/WebSocket. ws-address))
  (set! (.-onopen ws) (fn [] (js/alert "WS Connection opened!")))
  (set! (.-onmessage ws) (fn [js-event]
                           ;(js/pr "WS NEW MESSAGE!!")
                           (def --jse js-event)
                           (-> js-event (oops/oget "data") (on-message))))
  (set! (.-onerror ws) (fn [e] (js/alert "WS ERROR") (def --e e)))
  (set! (.-onclose ws) (fn [e] (js/alert "WS Connection closed") (js/console.info (.-code e) (.-reason e)))))

;; TODO: finish signals and check RPC calls

(defn call-private-rpc [payload callback]
  (println "RPC CALLED!!!!! ⚠")
  (backend-fetch "CallRPC" payload callback))

(comment
 ;; STEP 1
 (.initializeApplication ^js backend-obj
                         (utils.transforms/clj->json {:dataDir       "/home/ulises/p/status-go/test-db"
                                                      :mixpanelAppId status-im.config/mixpanel-app-id
                                                      :mixpanelToken status-im.config/mixpanel-token})
                         (fn [xxxx]
                           ;;(def --xxxx xxxx)
                           ((fn [response]
                              (def --rx response)
                              (js/alert (str "HTTP-RESPONSE:\n" (js/JSON.stringify response))))
                            (utils.transforms/json->clj --xxxx))))

 (backend-fetch "InitializeApplication"
                {:dataDir       "/home/ulises/p/status-go/test-db"
                 :mixpanelAppId status-im.config/mixpanel-app-id
                 :mixpanelToken status-im.config/mixpanel-token}
                (fn [response]
                  ;;(re-frame.core/dispatch [:profile/get-profiles-overview-success response])
                  (def --r response)
                  (js/alert (str "HTTP-RESPONSE:\n" (js/JSON.stringify response)))))
 #_(fetch "http://localhost:9050/statusgo/InitializeApplication"
        {:method  :POST
         :headers {"Accept"       "application/json"
                   "Content-Type" "application/json"}
         :body    {:dataDir       "/home/ulises/p/status-go/test-db"
                   :mixpanelAppId status-im.config/mixpanel-app-id
                   :mixpanelToken status-im.config/mixpanel-token}}
        (fn [response]
          (def --r response)
          (js/alert (str "HTTP-RESPONSE:\n" (prn-str response)))))
 ;; STEP 2
 (let [body (-> (status-im.contexts.profile.config/create)
                (assoc :displayName "Sonic"
                       :password (native-module.core/sha3 "Hola1234.," #(def --result %))
                       :imagePath nil
                       :customizationColor :army)
                (clj->js)
                (js/JSON.stringify))]

   (backend-fetch "CreateAccountAndLogin"
                  body
                  (fn [response]
                    (def --r response)
                    (js/alert (str "HTTP-RESPONSE:\n" (js/JSON.stringify response)))))
   #_(-> (fetch "http://localhost:9050/statusgo/CreateAccountAndLogin"
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
