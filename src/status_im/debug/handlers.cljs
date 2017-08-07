(ns status-im.debug.handlers
  (:require [re-frame.core :refer [after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.components.react :refer [http-bridge]]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.accounts :as accounts]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]))

(def debug-server-port 5561)

(defn respond [data]
  (.respond http-bridge
            200
            "application/json"
            (types/clj->json data)))

(register-handler :init-debug-mode
  (u/side-effect!
    (fn [_ [_ address]]
      (let [{:keys [debug?]} (accounts/get-by-address address)]
        (when debug?
          (dispatch [:debug-server-start]))))))

(register-handler :debug-server-start
  (u/side-effect!
    (fn [_]
      (.start http-bridge
              debug-server-port
              (if platform/ios?
                "Status iOS"
                "Status Android")
              (fn [req]
                (try
                  (let [{:keys [postData url]} (js->clj req :keywordize-keys true)
                        postData (if (string? postData)
                                   (-> (.parse js/JSON postData)
                                       (js->clj :keywordize-keys true))
                                   postData)]
                    (dispatch [:debug-request {:url url :postData postData}]))
                  (catch js/Error e
                    (log/debug "Error: " e))))))))

(register-handler :debug-server-stop
  (u/side-effect!
    (fn [_]
      (.stop http-bridge))))

(register-handler :debug-request
  (u/side-effect!
    (fn [{:keys [web3]} [_ {url               :url
                            {:keys [encoded]
                             :as   post-data} :postData}]]
      (try
        (let [json (some->> encoded
                            (.toAscii web3)
                            (.parse js/JSON))
              obj  (when json
                     (js->clj json :keywordize-keys true))]
          (case url
            "/add-dapp" (dispatch [:debug-add-contact obj])
            "/remove-dapp" (dispatch [:debug-remove-contact obj])
            "/dapp-changed" (dispatch [:debug-contact-changed obj])
            "/switch-node" (dispatch [:debug-switch-node obj])
            "/list" (dispatch [:debug-dapps-list])
            "/log" (dispatch [:debug-log post-data])
            :default))
        (catch js/Error e
          (respond {:type :error :text (str "Error: " e)})
          (log/debug "Error: " e))))))

(register-handler :debug-add-contact
  (u/side-effect!
    (fn [{:contacts/keys [contacts]} [_ {:keys [name whisper-identity dapp-url bot-url] :as dapp-data}]]
      (if (and name
               whisper-identity
               (or dapp-url bot-url))
        (if (or (not (get contacts whisper-identity))
                (get-in contacts [whisper-identity :debug?]))
          (let [dapp (merge dapp-data {:dapp?  true
                                       :debug? true})]
            (dispatch [:upsert-chat! {:chat-id whisper-identity
                                      :name    name
                                      :debug?  true}])
            (if (get contacts whisper-identity)
              (do (dispatch [:update-contact! dapp])
                  (respond {:type :ok
                            :text "The DApp or bot has been updated."}))
              (do (dispatch [:add-contacts [dapp]])
                  (dispatch [:open-chat-with-contact dapp])
                  (respond {:type :ok
                            :text "The DApp or bot has been added."}))))
          (respond {:type :error
                    :text "Your DApp or bot should be debuggable."}))
        (respond {:type :error
                  :text (str "You can add either DApp or bot. The object should contain \"name\", "
                             "\"whisper-identity\", and \"dapp-url\" or \"bot-url\" fields.")})))))

(register-handler :debug-remove-contact
  (u/side-effect!
    (fn [{:keys [chats]} [_ {:keys [whisper-identity]}]]
      (if (get chats whisper-identity)
        (if (get-in chats [whisper-identity :debug?])
          (do (dispatch [:remove-chat whisper-identity])
              (respond {:type :ok
                        :text "The DApp or bot has been removed."}))
          (respond {:type :error
                    :text "Your DApp or bot should be debuggable."}))
        (respond {:type :error
                  :text "There is no such DApp or bot."}))
      (dispatch [:remove-contact whisper-identity #(and (:dapp? %) (:debug? %))]))))

(register-handler :debug-contact-changed
  (u/side-effect!
    (fn [{:keys [webview-bridge current-chat-id]
          :contacts/keys [contacts]} [_ {:keys [whisper-identity] :as dapp-data}]]
      (when (get-in contacts [whisper-identity :debug?])
        (when (and (= current-chat-id whisper-identity)
                   webview-bridge)
          (.reload webview-bridge))
        (when (get-in contacts [whisper-identity :bot-url])
          (dispatch [:load-commands! whisper-identity])))
      (respond {:type :ok
                :text "Command has been executed."}))))

(register-handler :debug-switch-node
  (u/side-effect!
    (fn [{:accounts/keys [current-account-id]} [_ {:keys [url]}]]
      (dispatch [:initialize-protocol current-account-id url])
      (respond {:type :ok
                :text "You've successfully switched the node."}))))

(register-handler :debug-dapps-list
  (u/side-effect!
    (fn [{:contacts/keys [contacts]}]
      (let [contacts (->> (vals contacts)
                          (filter :debug?)
                          (map #(select-keys % [:name :whisper-identity :dapp-url :bot-url])))]
        (if (seq contacts)
          (respond {:type :ok
                    :data contacts})
          (respond {:type :error
                    :text "No DApps or bots found."}))))))

(register-handler :debug-log
  (u/side-effect!
    (fn [db [_ {:keys [identity]}]]
      (let [log (messages/get-log-messages identity)]
        (if (seq log)
          (respond {:type :ok
                    :data log})
          (respond {:type :error
                    :text "No log messages found."}))))))


