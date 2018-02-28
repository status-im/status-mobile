(ns status-im.chat.events
  (:require [clojure.set :as set] 
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.protocol.core :as protocol]
            [status-im.chat.models :as models]
            [status-im.chat.console :as console]
            [status-im.chat.constants :as chat.constants] 
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.screens.navigation :as navigation] 
            [status-im.utils.handlers :as handlers]
            status-im.chat.events.commands
            status-im.chat.events.requests
            status-im.chat.events.send-message
            status-im.chat.events.queue-message
            status-im.chat.events.receive-message
            status-im.chat.events.console
            status-im.chat.events.webview-bridge))

;;;; Effects

(re-frame/reg-fx
  :protocol-send-seen
  (fn [params]
    (protocol/send-seen! params)))

(re-frame/reg-fx
  :browse
  (fn [link]
    (list-selection/browse link)))

;;;; Handlers

(handlers/register-handler-db
  :set-chat-ui-props
  [re-frame/trim-v]
  (fn [db [kvs]]
    (models/set-chat-ui-props db kvs)))

(handlers/register-handler-db
  :toggle-chat-ui-props
  [re-frame/trim-v]
  (fn [db [ui-element]]
    (models/toggle-chat-ui-prop db ui-element)))

(handlers/register-handler-db
  :show-message-details
  [re-frame/trim-v]
  (fn [db [details]]
    (models/set-chat-ui-props db {:show-bottom-info? true
                                  :bottom-info details})))

(def index-messages (partial into {} (map (juxt :message-id identity))))

(handlers/register-handler-fx
  :load-more-messages
  [(re-frame/inject-cofx :get-stored-messages)]
  (fn [{{:keys [current-chat-id] :as db} :db get-stored-messages :get-stored-messages} _]
    (when-not (get-in db [:chats current-chat-id :all-loaded?])
      (let [loaded-count (count (get-in db [:chats current-chat-id :messages]))
            new-messages (index-messages (get-stored-messages current-chat-id loaded-count))]
        {:db (-> db
                 (update-in [:chats current-chat-id :messages] merge new-messages)
                 (update-in [:chats current-chat-id :not-loaded-message-ids] #(apply disj % (keys new-messages)))
                 (assoc-in [:chats current-chat-id :all-loaded?]
                           (> constants/default-number-of-messages (count new-messages))))}))))

(handlers/register-handler-db
  :message-appeared
  [re-frame/trim-v]
  (fn [db [{:keys [chat-id message-id]}]]
    (update-in db [:chats chat-id :messages message-id] assoc :appearing? false)))

(defn init-console-chat
  [{:keys [chats] :accounts/keys [current-account-id] :as db}]
  (if (chats constants/console-chat-id)
    {:db db}
    (cond-> {:db (-> db
                     (assoc :current-chat-id constants/console-chat-id)
                     (update :chats assoc constants/console-chat-id console/chat))
             :dispatch-n [[:add-contacts [console/contact]]]
             :save-chat console/chat
             :save-all-contacts [console/contact]}

      (not current-account-id)
      (update :dispatch-n concat [[:chat-received-message/add-when-commands-loaded console/intro-message1]]))))

(handlers/register-handler-fx
  :init-console-chat
  (fn [{:keys [db]} _]
    (init-console-chat db)))

(handlers/register-handler-fx
  :initialize-chats
  [(re-frame/inject-cofx :all-stored-chats)
   (re-frame/inject-cofx :inactive-chat-ids)
   (re-frame/inject-cofx :get-stored-messages)
   (re-frame/inject-cofx :stored-unviewed-messages)
   (re-frame/inject-cofx :stored-message-ids)
   (re-frame/inject-cofx :get-stored-unanswered-requests)]
  (fn [{:keys [db
               all-stored-chats
               inactive-chat-ids
               stored-unanswered-requests
               get-stored-messages
               stored-unviewed-messages
               stored-message-ids]} _]
    (let [{:accounts/keys [account-creation?]} db
          load-default-contacts-event [:load-default-contacts!]]
      (if account-creation?
        {:db db
         :dispatch load-default-contacts-event}
        (let [chat->message-id->request (reduce (fn [acc {:keys [chat-id message-id] :as request}]
                                                  (assoc-in acc [chat-id message-id] request))
                                                {}
                                                stored-unanswered-requests)
              chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                              (let [chat-messages (index-messages (get-stored-messages chat-id))]
                                (assoc acc chat-id
                                       (assoc chat
                                              :unviewed-messages      (get stored-unviewed-messages chat-id)
                                              :requests               (get chat->message-id->request chat-id)
                                              :messages               chat-messages
                                              :not-loaded-message-ids (set/difference (get stored-message-ids chat-id)
                                                                                      (-> chat-messages keys set))))))
                            {}
                            all-stored-chats)]
          (-> db
              (assoc :chats         chats
                     :deleted-chats inactive-chat-ids)
              init-console-chat
              (update :dispatch-n conj load-default-contacts-event)))))))

(handlers/register-handler-fx
  :send-seen!
  [re-frame/trim-v]
  (fn [{:keys [db]} [{:keys [chat-id from me message-id]}]]
    (let [{:keys [web3 chats] :contacts/keys [contacts]} db
          {:keys [group-chat public? messages]} (get chats chat-id)
          statuses (assoc (get-in messages [message-id :user-statuses]) me :seen)]
      (cond-> {:db (-> db
                       (update-in [:chats chat-id :unviewed-messages] disj message-id)
                       (assoc-in [:chats chat-id :messages message-id :user-statuses] statuses))
               :update-message {:message-id    message-id
                                :user-statuses statuses}}
        ;; for public chats and 1-1 bot/dapp chats, it makes no sense to signalise `:seen` msg
        (not (or public? (get-in contacts [chat-id :dapp?])))
        (assoc :protocol-send-seen {:web3    web3
                                    :message (cond-> {:from       me
                                                      :to         from
                                                      :message-id message-id}
                                               group-chat (assoc :group-id chat-id))})))))

(handlers/register-handler-fx
  :show-mnemonic
  [re-frame/trim-v]
  (fn [{:keys [db]} [mnemonic signing-phrase]]
    (let [crazy-math-message? (contains? (get-in db [:chats chat.constants/console-chat-id]) chat.constants/crazy-math-message-id)
          messages-events     (->> (console/passphrase-messages mnemonic signing-phrase crazy-math-message?)
                                   (mapv #(vector :chat-received-message/add %)))]
      {:dispatch-n messages-events})))

;; TODO(alwx): can be simplified
(handlers/register-handler-fx
  :account-generation-message
  (fn [{:keys [db]} _]
    (when-not (contains? (get-in db [:chats chat.constants/console-chat-id]) chat.constants/passphrase-message-id)
      {:dispatch [:chat-received-message/add console/account-generation-message]})))

(handlers/register-handler-fx
  :move-to-internal-failure-message
  (fn [{:keys [db]} _]
    (when-not (contains? (get-in db [:chats chat.constants/console-chat-id]) chat.constants/move-to-internal-failure-message-id)
      {:dispatch [:chat-received-message/add console/move-to-internal-failure-message]})))

(handlers/register-handler-fx
  :browse-link-from-message
  (fn [_ [_ link]]
    {:browse link}))

(defn preload-chat-data
  "Takes coeffects map and chat-id, returns effects necessary when navigating to chat"
  [{:keys [db]} chat-id]
  (let [chat-loaded-event (get-in db [:chats chat-id :chat-loaded-event])]
    (cond-> {:db (-> db
                     (assoc :current-chat-id chat-id)
                     (assoc-in [:chats chat-id :was-opened?] true)
                     (models/set-chat-ui-props {:validation-messages nil})
                     (update-in [:chats chat-id] dissoc :chat-loaded-event))}

      chat-loaded-event
      (assoc :dispatch chat-loaded-event))))

(handlers/register-handler-fx
  :add-chat-loaded-event
  [(re-frame/inject-cofx :get-stored-chat) re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [chat-id event]]
    (if (get (:chats db) chat-id)
      {:db (assoc-in db [:chats chat-id :chat-loaded-event] event)}
      (-> (models/add-chat chat-id cofx) ; chat not created yet, we have to create it
          (assoc-in [:db :chats chat-id :chat-loaded-event] event)))))

;; TODO(janherich): remove this unnecessary event in the future (only model function `add-chat` will stay)
(handlers/register-handler-fx
  :add-chat
  [(re-frame/inject-cofx :get-stored-chat) re-frame/trim-v]
  (fn [cofx [chat-id chat-props]]
    (models/add-chat chat-id chat-props cofx)))

(defn navigate-to-chat
  "Takes coeffects map and chat-id, returns effects necessary for navigation and preloading data"
  ([cofx chat-id]
   (navigate-to-chat cofx chat-id false))
  ([cofx chat-id navigation-replace?]
   (let [nav-fn (if navigation-replace?
                  #(navigation/replace-view % :chat)
                  #(navigation/navigate-to % :chat))]
     (-> (preload-chat-data cofx chat-id)
         (update :db nav-fn)))))

(handlers/register-handler-fx
  :navigate-to-chat
  [re-frame/trim-v]
  (fn [cofx [chat-id {:keys [navigation-replace?]}]]
    (navigate-to-chat cofx chat-id navigation-replace?)))

(handlers/register-handler-fx
  :start-chat
  [(re-frame/inject-cofx :get-stored-chat) re-frame/trim-v]
  (fn [{:keys [db] :as cofx} [contact-id {:keys [navigation-replace?]}]]
    (when (not= (:current-public-key db) contact-id) ; don't allow to open chat with yourself
      (if (get (:chats db) contact-id)
        (navigate-to-chat cofx contact-id navigation-replace?) ; existing chat, just preload and displey
        (let [add-chat-fx (models/add-chat contact-id cofx)] ; new chat, create before preload & display
          (merge add-chat-fx
                 (navigate-to-chat (assoc cofx :db (:db add-chat-fx))
                                   contact-id
                                   navigation-replace?)))))))

;; TODO(janherich): remove this unnecessary event in the future (only model function `update-chat` will stay)
(handlers/register-handler-fx
  :update-chat!
  [re-frame/trim-v]
  (fn [cofx [chat]]
    (models/update-chat chat cofx)))

(handlers/register-handler-fx
  :delete-chat
  [re-frame/trim-v]
  (fn [cofx [chat-id]]
    (-> (models/remove-chat chat-id cofx)
        (update :db navigation/replace-view :home))))

(handlers/register-handler-fx
  :delete-chat?
  [re-frame/trim-v]
  (fn [_ [chat-id group?]]
    {:show-confirmation {:title               (i18n/label :t/delete-confirmation)
                         :content             (i18n/label (if group? :t/delete-group-chat-confirmation :t/delete-chat-confirmation))
                         :confirm-button-text (i18n/label :t/delete)
                         :on-accept           #(re-frame/dispatch [:delete-chat chat-id])}}))
