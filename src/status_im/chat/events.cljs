(ns status-im.chat.events
  (:require [clojure.set :as set]
            [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.protocol.core :as protocol]
            [status-im.chat.models :as models]
            [status-im.chat.console :as console]
            [status-im.data-store.chats :as chats]
            [status-im.data-store.messages :as messages]
            [status-im.data-store.pending-messages :as pending-messages]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.async :as utils.async]
            [status-im.utils.handlers :as handlers]
            status-im.chat.events.commands
            status-im.chat.events.requests
            status-im.chat.events.send-message
            status-im.chat.events.queue-message
            status-im.chat.events.receive-message
            status-im.chat.events.console
            status-im.chat.events.webview-bridge))

;;;; Coeffects

(re-frame/reg-cofx
  :stored-unviewed-messages
  (fn [cofx _]
    (assoc cofx :stored-unviewed-messages
                (messages/get-unviewed (-> cofx :db :current-public-key)))))

(re-frame/reg-cofx
  :get-stored-message
  (fn [cofx _]
    (assoc cofx :get-stored-message messages/get-by-id)))

(re-frame/reg-cofx
  :get-stored-messages
  (fn [cofx _]
    (assoc cofx :get-stored-messages messages/get-by-chat-id)))

(re-frame/reg-cofx
  :stored-message-ids
  (fn [cofx _]
    (assoc cofx :stored-message-ids (messages/get-stored-message-ids))))

(re-frame/reg-cofx
  :all-stored-chats
  (fn [cofx _]
    (assoc cofx :all-stored-chats (chats/get-all))))

(re-frame/reg-cofx
  :get-stored-chat
  (fn [cofx _]
    (assoc cofx :get-stored-chat chats/get-by-id)))

(re-frame/reg-cofx
  :inactive-chat-ids
  (fn [cofx _]
    (assoc cofx :inactive-chat-ids (chats/get-inactive-ids))))

;;;; Effects

(def ^:private realm-queue (utils.async/task-queue 2000))

(re-frame/reg-fx
  :update-message
  (fn [message]
    (async/go (async/>! realm-queue #(messages/update-message message)))))

(re-frame/reg-fx
  :save-message
  (fn [message]
    (async/go (async/>! realm-queue #(messages/save message)))))

(re-frame/reg-fx
  :delete-messages
  (fn [chat-id]
    (async/go (async/>! realm-queue #(messages/delete-by-chat-id chat-id)))))

(re-frame/reg-fx
  :delete-pending-messages
  (fn [chat-id]
    (async/go (async/>! realm-queue #(pending-messages/delete-all-by-chat-id chat-id)))))

(re-frame/reg-fx
  :save-chat
  (fn [chat]
    (async/go (async/>! realm-queue #(chats/save chat)))))

(re-frame/reg-fx
  :deactivate-chat
  (fn [chat-id]
    (async/go (async/>! realm-queue #(chats/set-inactive chat-id)))))

(re-frame/reg-fx
  :delete-chat
  (fn [chat-id]
    (async/go (async/>! realm-queue #(chats/delete chat-id)))))

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
                                  :bottom-info       details})))

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
  [{:keys [chats] :as db}]
  (if (chats constants/console-chat-id)
    {:db db}
    {:db                (-> db
                            (assoc :current-chat-id constants/console-chat-id)
                            (update :chats assoc constants/console-chat-id console/chat))
     :dispatch          [:add-contacts [console/contact]]
     :save-chat         console/chat
     :save-all-contacts [console/contact]}))

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
    (let [chat->message-id->request (reduce (fn [acc {:keys [chat-id message-id] :as request}]
                                              (assoc-in acc [chat-id message-id] request))
                                            {}
                                            stored-unanswered-requests)
          chats (reduce (fn [acc {:keys [chat-id] :as chat}]
                          (let [chat-messages (index-messages (get-stored-messages chat-id))]
                            (assoc acc chat-id
                                       (assoc chat
                                         :unviewed-messages (get stored-unviewed-messages chat-id)
                                         :requests (get chat->message-id->request chat-id)
                                         :messages chat-messages
                                         :not-loaded-message-ids (set/difference (get stored-message-ids chat-id)
                                                                                 (-> chat-messages keys set))))))
                        {}
                        all-stored-chats)]
      (-> db
          (assoc :chats chats
                 :deleted-chats inactive-chat-ids)
          init-console-chat
          (update :dispatch-n conj [:load-default-contacts!])))))

(handlers/register-handler-fx
  :send-seen!
  [re-frame/trim-v]
  (fn [{:keys [db]} [{:keys [chat-id from me message-id]}]]
    (let [{:keys [web3 chats] :contacts/keys [contacts]} db
          {:keys [group-chat public? messages]} (get chats chat-id)
          statuses (assoc (get-in messages [message-id :user-statuses]) me :seen)]
      (cond-> {:db             (-> db
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
      (-> (models/add-chat cofx chat-id)                    ; chat not created yet, we have to create it
          (assoc-in [:db :chats chat-id :chat-loaded-event] event)))))

;; TODO(janherich): remove this unnecessary event in the future (only model function `add-chat` will stay)
(handlers/register-handler-fx
  :add-chat
  [(re-frame/inject-cofx :get-stored-chat) re-frame/trim-v]
  (fn [cofx [chat-id chat-props]]
    (models/add-chat cofx chat-id chat-props)))

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
    (when (not= (:current-public-key db) contact-id)        ; don't allow to open chat with yourself
      (if (get (:chats db) contact-id)
        (navigate-to-chat cofx contact-id navigation-replace?) ; existing chat, just preload and displey
        (let [add-chat-fx (models/add-chat cofx contact-id)] ; new chat, create before preload & display
          (merge add-chat-fx
                 (navigate-to-chat (assoc cofx :db (:db add-chat-fx))
                                   contact-id
                                   navigation-replace?)))))))

(handlers/register-handler-fx
  :create-new-public-chat
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db now]} [_ topic]]
    (let [exists? (boolean (get-in db [:chats topic]))
          chat    {:chat-id               topic
                   :name                  topic
                   :color                 components.styles/default-chat-color
                   :group-chat            true
                   :public?               true
                   :is-active             true
                   :timestamp             now
                   :last-to-clock-value   0
                   :last-from-clock-value 0}]
      (merge
        (when-not exists?
          {:db                    (assoc-in db [:chats (:chat-id chat)] chat)
           :save-chat             chat
           :start-watching-group! {:group-id topic
                                   :web3     (:web3 db)
                                   :identity (:current-public-key db)}})
        {:dispatch-n [[:navigate-to-clean :home]
                      [:navigate-to-chat topic]]}))))

(handlers/register-handler-fx
  :create-new-group-chat-and-open
  [re-frame/trim-v
   (re-frame/inject-cofx :now)
   (re-frame/inject-cofx :random-id)
   (re-frame/inject-cofx :get-new-keypair!)]
  (fn [{:keys [db random-id] :as cofx} [group-name]]
    (let [{:keys [web3 current-public-key]} db
          new-chat   (models/prepare-group-chat cofx group-name)
          identities (mapv :identity (:contacts new-chat))
          {:keys [chat-id public-key private-key contacts name]} new-chat]
      {:db                    (-> db
                                  (assoc-in [:chats chat-id] new-chat)
                                  (assoc :group/selected-contacts #{}))
       :save-chat             new-chat
       :start-watching-group! {:web3     web3
                               :group-id chat-id
                               :identity current-public-key
                               :keypair  {:public  public-key
                                          :private private-key}
                               :callback #(re-frame/dispatch [:incoming-message %1 %2])}
       :invite-to-group!      {:web3       web3
                               :group      {:id       chat-id
                                            :name     name
                                            :contacts (conj identities current-public-key)
                                            :admin    current-public-key
                                            :keypair  {:public  public-key
                                                       :private private-key}}
                               :identities identities
                               :message    {:from       current-public-key
                                            :message-id random-id}}
       :dispatch-n            [[:navigate-to-clean :home]
                               [:navigate-to-chat chat-id]]})))

(handlers/register-handler-fx
  :leave-group-chat
  [(re-frame/inject-cofx :random-id)]
  (fn [{{:keys [web3 current-chat-id chats current-public-key]} :db random-id :random-id :as cofx}]
    (let [{:keys [public-key private-key public?]} (get chats current-chat-id)]
      (merge (models/remove-chat cofx current-chat-id)
             {:stop-watching-group! {:web3     web3
                                     :group-id current-chat-id}
              :dispatch             [:navigation-replace :home]}
             (when-not public?
               {:leave-group-chat! {:web3     web3
                                    :group-id current-chat-id
                                    :keypair  {:public  public-key
                                               :private private-key}
                                    :message  {:from       current-public-key
                                               :message-id random-id}}})))))

;; TODO(janherich): remove this unnecessary event in the future (only model function `update-chat` will stay)
(handlers/register-handler-fx
  :update-chat!
  [re-frame/trim-v]
  (fn [cofx [chat]]
    (models/update-chat cofx chat)))

(handlers/register-handler-fx
  :remove-chat
  [re-frame/trim-v]
  (fn [cofx [chat-id]]
    (models/remove-chat cofx chat-id)))

(handlers/register-handler-fx
  :delete-chat
  [re-frame/trim-v]
  (fn [cofx [chat-id]]
    (-> (models/remove-chat cofx chat-id)
        (update :db navigation/replace-view :home))))

(handlers/register-handler-fx
  :delete-chat?
  [re-frame/trim-v]
  (fn [_ [chat-id group?]]
    {:show-confirmation {:title               (i18n/label :t/delete-confirmation)
                         :content             (i18n/label (if group? :t/delete-group-chat-confirmation :t/delete-chat-confirmation))
                         :confirm-button-text (i18n/label :t/delete)
                         :on-accept           #(re-frame/dispatch [:delete-chat chat-id])}}))
