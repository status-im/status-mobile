(ns status-im.commands.handlers.debug
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.commands.events.loading :as loading-events]
            [status-im.data-store.accounts :as accounts]
            [status-im.data-store.messages :as messages]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.platform :as platform]
            [status-im.utils.types :as types]
            [taoensso.timbre :as log]))

(def debug-server-port 5561)

(defn respond [data]
  (.respond react/http-bridge
            200
            "application/json"
            (types/clj->json data)))

(defn debug-server-start []
  (.start react/http-bridge
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
                (re-frame/dispatch [::process-request url postData]))
              (catch js/Error e
                (log/debug "Error: " e))))))


;;;; Specific debug methods
;; TODO: there are still a lot of dispatch calls here. we can remove or restructure most of them,
;; but to do this we need to also rewrite a lot of already existing functions

(defn add-contact
  [{:contacts/keys [contacts]} {:keys [name whisper-identity dapp-url bot-url] :as dapp-data}]
  (if (and name
           whisper-identity
           (or dapp-url bot-url))
    (if (or (not (get contacts whisper-identity))
            (get-in contacts [whisper-identity :debug?]))
      (let [dapp (merge dapp-data {:dapp?  true
                                   :debug? true})]
        (re-frame/dispatch [:upsert-chat! {:chat-id whisper-identity
                                           :name    name
                                           :debug?  true}])
        (if (get contacts whisper-identity)
          (do (re-frame/dispatch [:update-contact! dapp])
              (respond {:type :ok
                        :text "The DApp or bot has been updated."}))
          (do (re-frame/dispatch [:add-contacts [dapp]])
              (re-frame/dispatch [:open-chat-with-contact dapp])
              (respond {:type :ok
                        :text "The DApp or bot has been added."}))))
      (respond {:type :error
                :text "Your DApp or bot should be debuggable."}))
    (respond {:type :error
              :text (str "You can add either DApp or bot. The object should contain \"name\", "
                         "\"whisper-identity\", and \"dapp-url\" or \"bot-url\" fields.")})))

(defn remove-contact
  [{:keys [chats]} {:keys [whisper-identity]}]
  (if (get chats whisper-identity)
    (if (get-in chats [whisper-identity :debug?])
      (do (re-frame/dispatch [:remove-chat whisper-identity])
          (respond {:type :ok
                    :text "The DApp or bot has been removed."}))
      (respond {:type :error
                :text "Your DApp or bot should be debuggable."}))
    (respond {:type :error
              :text "There is no such DApp or bot."}))
  (re-frame/dispatch [:remove-contact whisper-identity #(and (:dapp? %) (:debug? %))]))

(defn contact-changed
  [{:keys          [webview-bridge current-chat-id]
    :contacts/keys [contacts]} {:keys [whisper-identity] :as dapp-data}]
  (when (get-in contacts [whisper-identity :debug?])
    (when (and (= current-chat-id whisper-identity)
               webview-bridge)
      (.reload webview-bridge))
    (when-let [bot-url (get-in contacts [whisper-identity :bot-url])]
      (re-frame/dispatch [::load-commands! {:whisper-identity whisper-identity
                                            :bot-url          bot-url}])))
  (respond {:type :ok
            :text "Command has been executed."}))

(defn switch-node
  [{:keys [url]}]
  (re-frame/dispatch [:initialize-protocol url])
  (respond {:type :ok
            :text "You've successfully switched the node."}))

(defn dapps-list
  [{:contacts/keys [contacts]}]
  (let [contacts (->> (vals contacts)
                      (filter :debug?)
                      (map #(select-keys % [:name :whisper-identity :dapp-url :bot-url])))]
    (if (seq contacts)
      (respond {:type :ok
                :data contacts})
      (respond {:type :error
                :text "No DApps or bots found."}))))

(defn log [db {:keys [identity]}]
  (let [log (messages/get-log-messages identity)]
    (if (seq log)
      (respond {:type :ok
                :data log})
      (respond {:type :error
                :text "No log messages found."}))))


;;;; FX

(re-frame/reg-fx
 ::initialize-debugging-fx
 (fn [[address force-start?]]
   (if force-start?
     (debug-server-start)
     (let [{:keys [debug?]} (accounts/get-by-address address)]
       (when debug?
         (debug-server-start))))))

(re-frame/reg-fx
 ::stop-debugging-fx
 (fn [_]
   (.stop react/http-bridge)))

(re-frame/reg-fx
  ::process-request-fx
  (fn [[{:keys [web3] :as db} url {:keys [encoded] :as post-data}]]
    (try
      (let [json (some->> encoded
                          (.toAscii web3)
                          (.parse js/JSON))
            obj  (when json
                   (js->clj json :keywordize-keys true))]
        (case url
          "/add-dapp" (add-contact db obj)
          "/remove-dapp" (remove-contact db obj)
          "/dapp-changed" (contact-changed db obj)
          "/switch-node" (switch-node obj)
          "/list" (dapps-list db)
          "/log" (log db post-data)
          :default))
      (catch js/Error e
        (respond {:type :error :text (str "Error: " e)})
        (log/debug "Error: " e)))))


;;;; Handlers

(handlers/register-handler-fx
 :initialize-debugging
 [re-frame/trim-v]
 (fn [_ [{:keys [address force-start?]}]]
   {::initialize-debugging-fx [address force-start?]}))

(handlers/register-handler-fx
 :stop-debugging
 (fn [_]
   {::stop-debugging-fx nil}))

(handlers/register-handler-fx
 ::process-request
 [re-frame/trim-v]
 (fn [{:keys [db]} [url post-data]]
   {::process-request-fx [db url post-data]}))

;; TODO(janherich) once `contact-changed` fn is refactored, get rid of this unnecessary event
(handlers/register-handler-fx
  ::load-commands
  [re-frame/trim-v (re-frame/inject-cofx :get-local-storage-data)]
  (fn [cofx [contact]]
    (loading-events/load-commands cofx {} contact)))
