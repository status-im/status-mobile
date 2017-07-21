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
  (fn [{:keys [current-chat-id] :as db} [_ kvs]]
    (update-in db [:chat-ui-props current-chat-id] merge kvs)))

(register-handler :toggle-chat-ui-props
  (fn [{:keys [current-chat-id chat-ui-props] :as db} [_ ui-element chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (update-in db [:chat-ui-props chat-id ui-element] not))))

(register-handler :show-message-details
  (u/side-effect!
    (fn [_ [_ details]]
      (dispatch [:set-chat-ui-props {:show-bottom-info? true
                                     :show-emoji?       false
                                     :bottom-info       details}]))))

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

(register-handler :move-to-internal-failure-message
  (u/side-effect!
    (fn [_]
      (when-not (messages/get-by-id chat-consts/move-to-internal-failure-message-id)
        (sign-up-service/move-to-internal-failure-message)))))

(register-handler :show-mnemonic
  (u/side-effect!
    (fn [_ [_ mnemonic]]
      (let [crazy-math-message? (messages/get-by-id chat-consts/crazy-math-message-id)]
        (sign-up-service/passphrase-messages mnemonic crazy-math-message?)))))

(defn- handle-sms [{body :body}]
  (when-let [matches (re-matches #"(\d{4})" body)]
    (dispatch [:sign-up-confirm (second matches)])))

(register-handler :sign-up
  (after (fn [_ [_ phone-number]]
           (dispatch [:account-update {:phone phone-number}])))
  (fn [db [_ phone-number message-id]]
    (sign-up-service/start-listening-confirmation-code-sms)
    (let [formatted (format-phone-number phone-number)]
      (server/sign-up db
                      formatted
                      message-id
                      sign-up-service/on-sign-up-response))))

(register-handler :start-listening-confirmation-code-sms
  (fn [db [_ listener]]
    (if-not (:confirmation-code-sms-listener db)
      (assoc db :confirmation-code-sms-listener listener)
      db)))

(register-handler :stop-listening-confirmation-code-sms
  (fn [db]
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
         ;; todo rewrite it so that commands defined outside chat's context
         ;; (bots' commands in group chats and global commands in all chats)
         ;; could be rendered properly
         (dispatch [:request-command-data (assoc message :chat-id current-chat-id)])))
     (assoc db :messages messages))))

(defn init-chat
  ([db] (init-chat db nil))
  ([{:keys [messages current-chat-id] :as db} _]
   (-> db
       (assoc-in [:chats current-chat-id :messages] messages)
       (dissoc :messages))))

(defn load-commands!
  [{:keys [current-chat-id]} _]
  (dispatch [:load-commands! current-chat-id]))

(register-handler :init-chat
  (after #(dispatch [:load-requests!]))

  (u/handlers->
    load-messages!
    init-chat
    load-commands!))

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

(register-handler :initialize-chats
  [(after #(dispatch [:load-unviewed-messages!]))
   (after #(dispatch [:load-default-contacts!]))]
  (u/handlers->
    load-chats!
    initialize-chats))

(register-handler :reload-chats
  (fn [{:keys [chats] :as db} _]
    (let [chats' (->> (chats/get-all)
                      (map (fn [{:keys [chat-id] :as chat}]
                             (let [last-message (messages/get-last-message chat-id)
                                   prev-chat    (get chats chat-id)
                                   new-chat     (assoc chat :last-message last-message)]
                               [chat-id (merge prev-chat new-chat)])))
                      (into (priority-map-by compare-chats)))]
      (-> (assoc db :chats chats')
          (init-console-chat true)))))

(defmethod nav/preload-data! :chat
  [{:keys [current-chat-id current-account-id] :as db} [_ _ id]]
  (let [chat-id           (or id current-chat-id)
        messages          (get-in db [:chats chat-id :messages])
        db'               (-> db
                              (assoc :current-chat-id chat-id)
                              (assoc-in [:chats chat-id :was-opened?] true))
        commands-loaded?  (get-in db [:contacts/contacts chat-id :commands-loaded?])
        bot-url           (get-in db [:contacts/contacts chat-id :bot-url])
        was-opened?       (get-in db [:chats chat-id :was-opened?])
        call-init-command #(when (and (not was-opened?) bot-url)
                             (status/call-function!
                               {:chat-id  chat-id
                                :function :init
                                :context  {:from current-account-id}}))]
       ; Reset validation messages, if any
       (dispatch [:set-chat-ui-props {:validation-messages nil}])
       (dispatch [:load-requests! chat-id])
        ;; todo rewrite this. temporary fix for https://github.com/status-im/status-react/issues/607
       #_(dispatch [:load-commands! chat-id])
        (if-not commands-loaded?
          (dispatch [:load-commands! chat-id call-init-command])
          (do
            (call-init-command)
            (dispatch [:invoke-chat-loaded-callbacks chat-id])))
        (if (and (seq messages)
                 (not= (count messages) 1))
          db'
          (-> db'
              load-messages!
              init-chat))))

(register-handler :add-chat-loaded-callback
  (fn [db [_ chat-id callback]]
    (log/debug "Add chat loaded callback: " chat-id callback)
    (update-in db [:chat-loaded-callbacks chat-id] conj callback)))

(register-handler ::clear-chat-loaded-callbacks
  (fn [db [_ chat-id]]
    (log/debug "Clear chat loaded callback: " chat-id)
    (assoc-in db [:chat-loaded-callbacks chat-id] nil)))

(register-handler :invoke-chat-loaded-callbacks
  (u/side-effect!
    (fn [db [_ chat-id]]
      (log/debug "Invoking chat loaded callbacks: " chat-id)
      (let [callbacks (get-in db [:chat-loaded-callbacks chat-id])]
        (log/debug "Invoking chat loaded callbacks: " callbacks)
        (doseq [callback callbacks]
          (callback))
        (dispatch [::clear-chat-loaded-callbacks chat-id])))))

(defn prepare-chat [{:contacts/keys [contacts]} chat-id chat]
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
  (u/handlers->
    add-new-chat
    add-chat
    save-new-chat!
    open-chat!))

(register-handler :start-chat
  (u/side-effect!
    (fn [{:keys [chats current-public-key]}
         [_ contact-id options navigation-type]]
      (when-not (= current-public-key contact-id)
        (if (chats contact-id)
          (dispatch [(or navigation-type :navigate-to) :chat contact-id])
          (dispatch [::start-chat! contact-id options navigation-type]))))))

(register-handler :add-chat
  (u/handlers->
    add-new-chat
    add-chat
    save-new-chat!))

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
                 (-> (chats/get-by-id chat-id)
                     (assoc :timestamp (random/timestamp))
                     (merge opts))
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
  (let [{:keys [debug? group-chat]} (chats/get-by-id chat-id)]
    (if (and (not debug?) group-chat)
      (chats/set-inactive chat-id)
      (chats/delete chat-id))))

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
  (u/handlers->
    remove-chat
    delete-messages!
    remove-pending-messages!
    delete-chat!))

(defn send-seen!
  [{:keys [web3 current-public-key chats]
    :contacts/keys [contacts]}
   [_ {:keys [from chat-id message-id]}]]
  (when-not (get-in contacts [chat-id :dapp?])
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
  [{:keys [web3 current-public-key]
    :contacts/keys [contacts]} [_ {:keys [message-id from]}]]
  (when-not (get-in contacts [from :dapp?])
    (protocol/send-clock-value-request!
      {:web3    web3
       :message {:from       current-public-key
                 :to         from
                 :message-id message-id}})))

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

(register-handler :check-and-open-dapp!
  (u/side-effect!
    (fn [{:keys [current-chat-id global-commands]
          :contacts/keys [contacts]}]
      (let [dapp-url (get-in contacts [current-chat-id :dapp-url])]
        (when dapp-url
          (am/go
            (dispatch [:select-chat-input-command
                       (assoc (:browse global-commands) :prefill [dapp-url])
                       nil
                       true])
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
