(ns status-backend.config)

(defonce ws (js/WebSocket. "ws://localhost:9050/signals"))
;; Before run:
;; adb reverse tcp:9050 tcp:9050

;;(doto )

(set! (.-onopen ws) (fn []
                      (js/alert "WS Connection opened!")))

(set! (.-onmessage ws) (fn [e]
                         (js/alert "WS Message arrived")
                         (def --m e)
                         ))

(set! (.-onerror ws) (fn [e]
                       (js/alert "WS ERROR")
                       (def --e e)
                       ))

(set! (.-onclose ws) (fn [e]
                       (js/alert "WS Connection closed")
                       (js/console.info (.-code e) (.-reason e))
                       ))


(comment
 ;; STEP 1
 #_(-> (fetch "http://localhost:9050/statusgo/InitializeApplication"
              (clj->js
               {:method  :POST
                :headers {"Accept"       "application/json"
                          "Content-Type" "application/json"}
                :body    (-> {:dataDir       "/home/ulises/p/status-go/test-db"
                              :mixpanelAppId config/mixpanel-app-id
                              :mixpanelToken config/mixpanel-token}
                             (clj->js)
                             (js/JSON.stringify))}))
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
