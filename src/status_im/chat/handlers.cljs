(ns status-im.chat.handlers
  (:require-macros [cljs.core.async.macros :as am])
  (:require [re-frame.core :refer [enrich after debug dispatch]]
            [status-im.models.commands :as commands]
            [clojure.string :as string]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.chat.models.suggestions :as suggestions]
            [status-im.chat.constants :as chat-consts]
            [status-im.protocol.core :as protocol]
            [status-im.data-store.chats :as chats]
            [status-im.data-store.contacts :as contacts]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.pending-messages :as pending-messages]
            [status-im.constants :refer [text-content-type
                                         content-type-command
                                         content-type-command-request
                                         default-number-of-messages
                                         console-chat-id
                                         wallet-chat-id]]
            [status-im.utils.random :as random]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.handlers.server :as server]
            [status-im.utils.phone-number :refer [format-phone-number
                                                  valid-mobile-number?]]
            [status-im.components.status :as status]
            [status-im.utils.types :refer [json->clj]]
            [status-im.chat.utils :refer [console? not-console? safe-trim]]
            [status-im.utils.gfycat.core :refer [generate-gfy]]
            status-im.chat.handlers.input
            status-im.chat.handlers.commands
            status-im.chat.handlers.animation
            status-im.chat.handlers.requests
            status-im.chat.handlers.unviewed-messages
            status-im.chat.handlers.send-message
            status-im.chat.handlers.receive-message
            status-im.chat.handlers.faucet
            [cljs.core.async :as a]
            status-im.chat.handlers.webview-bridge
            status-im.chat.handlers.console
            [taoensso.timbre :as log]
            [tailrecursion.priority-map :refer [priority-map-by]]))

(register-handler :set-layout-height
  (fn [db [_ height]]
    (assoc db :layout-height height)))

(register-handler :set-chat-ui-props
  (fn [{:keys [current-chat-id] :as db} [_ ui-element value chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (assoc-in db [:chat-ui-props chat-id ui-element] value))))

(register-handler :toggle-chat-ui-props
  (fn [{:keys [current-chat-id chat-ui-props] :as db} [_ ui-element chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (update-in db [:chat-ui-props chat-id ui-element] not))))

(register-handler :show-message-details
  (u/side-effect!
    (fn [_ [_ details]]
      (dispatch [:set-chat-ui-props :show-bottom-info? true])
      (dispatch [:set-chat-ui-props :show-emoji? false])
      (dispatch [:set-chat-ui-props :bottom-info details]))))

(register-handler :load-more-messages
  (fn [{:keys [current-chat-id loading-allowed] :as db} _]
    (let [all-loaded? (get-in db [:chats current-chat-id :all-loaded?])]
      (if loading-allowed
        (do (am/go
              (<! (a/timeout 400))
              (dispatch [:set :loading-allowed true]))
            (if all-loaded?
              db
              (let [messages-path [:chats current-chat-id :messages]
                    messages      (get-in db messages-path)
                    chat-messages (filter #(= current-chat-id (:chat-id %)) messages)
                    new-messages  (messages/get-by-chat-id current-chat-id (count chat-messages))
                    all-loaded?   (> default-number-of-messages (count new-messages))]
                (-> db
                    (assoc :loading-allowed false)
                    (update-in messages-path concat new-messages)
                    (assoc-in [:chats current-chat-id :all-loaded?] all-loaded?)))))
        db))))

(defn set-message-shown
  [db chat-id message-id]
  (update-in db
             [:chats chat-id :messages]
             (fn [messages]
               (map (fn [message]
                      (if (= message-id (:message-id message))
                        (assoc message :new? false)
                        message))
                    messages))))

(register-handler :set-message-shown
  (fn [db [_ {:keys [chat-id message-id]}]]
    (set-message-shown db chat-id message-id)))

(register-handler :cancel-command
  (fn [{:keys [current-chat-id] :as db} _]
    (-> db
        (dissoc :canceled-command)
        (assoc-in [:chats current-chat-id :command-input] {})
        (update-in [:chats current-chat-id :input-text] safe-trim))))

(defn init-console-chat
  ([{:keys [chats current-account-id] :as db} existing-account?]
   (let [new-chat sign-up-service/console-chat]
     (if (chats console-chat-id)
       db
       (do
         (dispatch [:add-contacts [sign-up-service/console-contact]])
         (chats/save new-chat)
         (contacts/save-all [sign-up-service/console-contact])
         (when-not current-account-id
           (sign-up-service/intro))
         (when existing-account?
           (sign-up-service/start-signup))
         (-> db
             (assoc :new-chat new-chat)
             (update :chats assoc console-chat-id new-chat)
             (assoc :current-chat-id console-chat-id)))))))

(register-handler :init-console-chat
  (fn [db _]
    (init-console-chat db false)))

(register-handler :account-generation-message
  (u/side-effect!
    (fn [_]
      (when-not (messages/get-by-id chat-consts/passphrase-message-id)
        (sign-up-service/account-generation-message)))))

(register-handler :show-mnemonic
  (u/side-effect!
    (fn [_ [_ mnemonic]]
      (let [crazy-math-message? (messages/get-by-id chat-consts/crazy-math-message-id)]
        (sign-up-service/passphrase-messages mnemonic crazy-math-message?)))))

(register-handler :sign-up
  (after (fn [_ [_ phone-number]]
           (dispatch [:account-update {:phone phone-number}])))
  (fn [db [_ phone-number message-id]]
    (let [formatted (format-phone-number phone-number)]
      (-> db
          (assoc :user-phone-number formatted)
          sign-up-service/start-listening-confirmation-code-sms
          (server/sign-up formatted
                          message-id
                          sign-up-service/on-sign-up-response)))))

(register-handler :stop-listening-confirmation-code-sms
  (fn [db [_]]
    (if (:confirmation-code-sms-listener db)
      (sign-up-service/stop-listening-confirmation-code-sms db)
      db)))

(register-handler :sign-up-confirm
  (u/side-effect!
    (fn [_ [_ confirmation-code message-id]]
      (server/sign-up-confirm
        confirmation-code
        message-id
        sign-up-service/on-send-code-response))))

(register-handler :set-signed-up
  (u/side-effect!
    (fn [_ [_ signed-up]]
      (dispatch [:account-update {:signed-up? signed-up}]))))

(defn load-messages!
  ([db] (load-messages! db nil))
  ([{:keys [current-chat-id] :as db} _]
   (let [messages (messages/get-by-chat-id current-chat-id)]
     (doseq [{:keys [content] :as message} messages]
       (when (and (:command content)
                  (not (:content content)))
         (dispatch [:request-command-data (assoc message :chat-id current-chat-id)])))
     (assoc db :messages messages))))

(defn init-chat
  ([db] (init-chat db nil))
  ([{:keys [messages current-chat-id] :as db} _]
   (-> db
       (assoc-in [:chats current-chat-id :messages] messages)
       (dissoc :messages))))

(defn load-commands!
  [{:keys [current-chat-id]}]
  (dispatch [:load-commands! current-chat-id]))

(register-handler :init-chat
  (after #(dispatch [:load-requests!]))
  (-> load-messages!
      ((enrich init-chat))
      ((after load-commands!))))

(defn compare-chats
  [{timesatmp1 :timestamp} {timestamp2 :timestamp}]
  (compare timestamp2 timesatmp1))

(defn initialize-chats
  [{:keys [loaded-chats account-creation? chats] :as db} _]
  (let [chats' (if account-creation?
                 chats
                 (->> loaded-chats
                      (map (fn [{:keys [chat-id] :as chat}]
                             (let [last-message (messages/get-last-message chat-id)]
                               [chat-id (assoc chat :last-message last-message)])))
                      (into (priority-map-by compare-chats))))]

    (-> db
        (assoc :chats chats')
        (dissoc :loaded-chats)
        (init-console-chat true))))

(defn load-chats!
  [{:keys [account-creation?] :as db} _]
  (if account-creation?
    db
    (assoc db :loaded-chats (chats/get-all))))

;TODO: check if its new account / signup status / create console chat
(register-handler :initialize-chats
  [(after #(dispatch [:load-unviewed-messages!]))
   (after #(dispatch [:load-default-contacts!]))]
  ((enrich initialize-chats) load-chats!))

(defmethod nav/preload-data! :chat
  [{:keys [current-chat-id] :as db} [_ _ id]]
  (let [chat-id          (or id current-chat-id)
        messages         (get-in db [:chats chat-id :messages])
        command?         (= :command (get-in db [:edit-mode chat-id]))
        db'              (assoc db :current-chat-id chat-id)
        commands-loaded? (if js/goog.DEBUG
                           false
                           (get-in db [:chats chat-id :commands-loaded]))]
    (dispatch [:load-requests! chat-id])
    ;; todo rewrite this. temporary fix for https://github.com/status-im/status-react/issues/607
    #_(dispatch [:load-commands! chat-id])
    (if-not commands-loaded?
      (dispatch [:load-commands! chat-id])
      (dispatch [:invoke-chat-loaded-callbacks chat-id]))
    (if (and (seq messages)
             (not= (count messages) 1))
      db'
      (-> db'
          load-messages!
          init-chat))))

(register-handler :add-chat-loaded-callback
  (fn [db [_ chat-id callback]]
    (log/debug "Add chat loaded callback: " chat-id callback)
    (update-in db [::chat-loaded-callbacks chat-id] conj callback)))

(register-handler ::clear-chat-loaded-callbacks
  (fn [db [_ chat-id]]
    (log/debug "Clear chat loaded callback: " chat-id)
    (assoc-in db [::chat-loaded-callbacks chat-id] nil)))

(register-handler :invoke-chat-loaded-callbacks
  (u/side-effect!
    (fn [db [_ chat-id]]
      (log/debug "Invoking chat loaded callbacks: " chat-id)
      (let [callbacks (get-in db [::chat-loaded-callbacks chat-id])]
        (log/debug "Invoking chat loaded callbacks: " callbacks)
        (doseq [callback callbacks]
          (callback))
        (dispatch [::clear-chat-loaded-callbacks chat-id])))))

(defn prepare-chat [{:keys [contacts]} chat-id chat]
  (let [name (get-in contacts [chat-id :name])]
    (merge {:chat-id    chat-id
            :name       (or name (generate-gfy))
            :color      default-chat-color
            :group-chat false
            :is-active  true
            :timestamp  (.getTime (js/Date.))
            :contacts   [{:identity chat-id}]}
           chat)))

(defn add-new-chat
  [db [_ chat-id chat]]
  (assoc db :new-chat (prepare-chat db chat-id chat)))

(defn add-chat [{:keys [new-chat chats] :as db} [_ chat-id]]
  (if-not (get chats chat-id)
    (update db :chats assoc chat-id new-chat)
    db))

(defn save-new-chat!
  [{{:keys [chat-id] :as new-chat} :new-chat} _]
  (when-not (chats/exists? chat-id)
    (chats/save new-chat)))

(defn open-chat!
  [_ [_ chat-id _ navigation-type]]
  (dispatch [(or navigation-type :navigate-to) :chat chat-id]))

(register-handler ::start-chat!
  (-> add-new-chat
      ((enrich add-chat))
      ((after save-new-chat!))
      ((after open-chat!))))

(register-handler :start-chat
  (u/side-effect!
    (fn [{:keys [chats current-public-key]}
         [_ contact-id options navigation-type]]
      (when-not (= current-public-key contact-id)
        (if (chats contact-id)
          (dispatch [(or navigation-type :navigate-to) :chat contact-id])
          (dispatch [::start-chat! contact-id options navigation-type]))))))

(register-handler :add-chat
  (-> add-new-chat
      ((enrich add-chat))
      ((after save-new-chat!))))

(defn update-chat!
  [_ [_ {:keys [name] :as chat}]]
  (let [chat' (if name chat (dissoc chat :name))]
    (chats/save chat')))

(register-handler :update-chat!
  (after update-chat!)
  (fn [db [_ {:keys [chat-id name] :as chat}]]
    (let [chat' (if name chat (dissoc chat :name))]
      (if (get-in db [:chats chat-id])
        (update-in db [:chats chat-id] merge chat')
        db))))

(register-handler :upsert-chat!
  (fn [db [_ {:keys [chat-id] :as opts}]]
    (let [chat (if (chats/exists? chat-id)
                 (let [chat (chats/get-by-id chat-id)]
                   (assoc chat :timestamp (random/timestamp)))
                 (prepare-chat db chat-id opts))]
      (chats/save chat)
      (update-in db [:chats chat-id] merge chat))))

(defn remove-chat
  [db [_ chat-id]]
  (update db :chats dissoc chat-id))

(defn delete-messages!
  [{:keys [current-chat-id]} [_ chat-id]]
  (let [id (or chat-id current-chat-id)]
    (messages/delete-by-chat-id id)))

(defn delete-chat!
  [_ [_ chat-id]]
  (let [{:keys [debug?]} (chats/get-by-id chat-id)]
    (if debug?
      (chats/delete chat-id)
      (chats/set-inactive chat-id))))

(defn remove-pending-messages!
  [_ [_ chat-id]]
  (pending-messages/delete-all-by-chat-id chat-id))

(register-handler :leave-group-chat
  ;; todo oreder of operations tbd
  (after (fn [_ _] (dispatch [:navigation-replace :chat-list])))
  (u/side-effect!
    (fn [{:keys [web3 current-chat-id chats current-public-key]} _]
      (let [{:keys [public-key private-key public?]} (chats current-chat-id)]
        (protocol/stop-watching-group!
          {:web3     web3
           :group-id current-chat-id})
        (when-not public?
          (protocol/leave-group-chat!
            {:web3     web3
             :group-id current-chat-id
             :keypair  {:public  public-key
                        :private private-key}
             :message  {:from       current-public-key
                        :message-id (random/id)}})))
      (dispatch [:remove-chat current-chat-id]))))

(register-handler :remove-chat
  (-> remove-chat
      ((after delete-messages!))
      ((after remove-pending-messages!))
      ((after delete-chat!))))

(defn send-seen!
  [{:keys [web3 current-public-key chats]}
   [_ {:keys [from chat-id message-id]}]]
  (when-not (console? chat-id)
    (let [{:keys [group-chat public?]} (chats chat-id)]
      (when-not public?
        (protocol/send-seen! {:web3    web3
                              :message {:from       current-public-key
                                        :to         from
                                        :group-id   (when group-chat chat-id)
                                        :message-id message-id}})))))

(register-handler :send-seen!
  [(after (fn [_ [_ {:keys [message-id]}]]
            (messages/update {:message-id     message-id
                              :message-status :seen})))
   (after (fn [_ [_ {:keys [chat-id]}]]
            (dispatch [:remove-unviewed-messages chat-id])))]
  (u/side-effect! send-seen!))

(defn send-clock-value-request!
  [{:keys [web3 current-public-key]} [_ {:keys [message-id from]}]]
  (protocol/send-clock-value-request! {:web3    web3
                                       :message {:from       current-public-key
                                                 :to         from
                                                 :message-id message-id}}))

(register-handler :send-clock-value-request! (u/side-effect! send-clock-value-request!))

(defn send-clock-value!
  [{:keys [web3 current-public-key]} to message-id clock-value]
  (when current-public-key
    (protocol/send-clock-value! {:web3    web3
                                 :message {:from        current-public-key
                                           :to          to
                                           :message-id  message-id
                                           :clock-value clock-value}})))

(register-handler :update-clock-value!
  (after (fn [db [_ to i {:keys [message-id] :as message} last-clock-value]]
           (let [clock-value (+ last-clock-value i 1)]
             (messages/update (assoc message :clock-value clock-value))
             (send-clock-value! db to message-id clock-value))))
  (fn [db [_ _ i {:keys [message-id]} last-clock-value]]
    (assoc-in db [:message-extras message-id :clock-value] (+ last-clock-value i 1))))

(register-handler :send-clock-value!
  (u/side-effect!
    (fn [db [_ to message-id]]
      (let [{:keys [clock-value]} (messages/get-by-id message-id)]
        (send-clock-value! db to message-id clock-value)))))

(register-handler :check-autorun
  (u/side-effect!
    (fn [{:keys [current-chat-id contacts] :as db}]
      (let [autorun (get-in db [:chats current-chat-id :autorun])]
        (when autorun
          (am/go
            (dispatch [:select-chat-input-command {:name autorun}])
            (a/<! (a/timeout 100))
            (dispatch [:send-current-message])))))))

(register-handler :update-group-message
  (u/side-effect!
    (fn [{:keys [current-public-key web3 chats]}
         [_ {:keys                                [from]
             {:keys [group-id keypair timestamp]} :payload}]]
      (let [{:keys [private public]} keypair]
        (let [is-active (chats/is-active? group-id)
              chat      {:chat-id     group-id
                         :public-key  public
                         :private-key private
                         :updated-at  timestamp}]
          (when (and (= from (get-in chats [group-id :group-admin]))
                     (or (not (chats/exists? group-id))
                         (chats/new-update? timestamp group-id)))
            (dispatch [:update-chat! chat])
            (when is-active
              (protocol/start-watching-group!
                {:web3     web3
                 :group-id group-id
                 :identity current-public-key
                 :keypair  keypair
                 :callback #(dispatch [:incoming-message %1 %2])}))))))))

(register-handler :update-message-overhead!
  (u/side-effect!
    (fn [_ [_ chat-id network-status]]
      (if (= network-status :offline)
        (chats/inc-message-overhead chat-id)
        (chats/reset-message-overhead chat-id)))))
