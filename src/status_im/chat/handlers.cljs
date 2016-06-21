(ns status-im.chat.handlers
  (:require [re-frame.core :refer [register-handler enrich after debug dispatch]]
            [status-im.models.commands :as commands]
            [clojure.string :as str]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.chat.styles.response :refer [request-info-height response-height-normal]]
            [status-im.chat.suggestions :as suggestions]
            [status-im.protocol.api :as api]
            [status-im.models.messages :as messages]
            [status-im.constants :refer [text-content-type
                                         content-type-command]]
            [status-im.utils.random :as random]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.models.chats :as chats]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :as u]
            [status-im.persistence.realm :as r]
            [status-im.handlers.server :as server]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.utils.datetime :as time]
            [status-im.chat.handlers.animation :refer [update-response-height
                                                       get-response-height]]))

(register-handler :set-show-actions
  (fn [db [_ show-actions]]
    (assoc db :show-actions show-actions)))

(register-handler :load-more-messages
  (fn [db _]
    db
    ;; TODO implement
    #_(let [chat-id      (get-in db [:chat :current-chat-id])
            messages     [:chats chat-id :messages]
            new-messages (gen-messages 10)]
        (update-in db messages concat new-messages))))

(defn safe-trim [s]
  (when (string? s)
    (str/trim s)))

(register-handler :cancel-command
  (fn [{:keys [current-chat-id] :as db} _]
    (-> db
        (assoc-in [:chats current-chat-id :command-input] {})
        (update-in [:chats current-chat-id :input-text] safe-trim))))

(register-handler :start-cancel-command
  (u/side-effect!
    (fn [db _]
      (dispatch [:animate-cancel-command]))))

(defn animate-set-chat-command-content [db _]
  (when (commands/get-chat-command-to-msg-id db)
    (dispatch [:animate-response-resize])))

(register-handler :set-chat-command-content
  (after animate-set-chat-command-content)
  (fn [{:keys [current-chat-id] :as db} [_ content]]
    (as-> db db
          (commands/set-chat-command-content db content)
          (assoc-in db [:chats current-chat-id :input-text] nil)
          (if (commands/get-chat-command-to-msg-id db)
            (update-response-height db)
            db))))

(defn update-input-text
  [{:keys [current-chat-id] :as db} text]
  (assoc-in db [:chats current-chat-id :input-text] text))

(register-handler :stage-command
  (fn [{:keys [current-chat-id] :as db} _]
    (let [db (update-input-text db nil)
          {:keys [command content]}
          (get-in db [:chats current-chat-id :command-input])
          command-info {:command command
                        :content content
                        :handler (:handler command)}]
      (-> db
          (assoc-in [:chats current-chat-id :command-input :command] nil)
          (commands/stage-command command-info)))))

(register-handler :set-message-input []
  (fn [db [_ input]]
    (assoc db :message-input input)))

(register-handler :blur-message-input
  (u/side-effect!
    (fn [db _]
      (when-let [message-input (:message-input db)]
        (.blur message-input)))))

(register-handler :set-response-chat-command
  (after #(dispatch [:animate-show-response]))
  (fn [db [_ to-msg-id command-key]]
    (commands/set-response-chat-command db to-msg-id command-key)))

(defn update-text
  [db [_ text]]
  (update-input-text db text))

(defn update-command [db [_ text]]
  (if-not (commands/get-chat-command db)
    (let [{:keys [command]} (suggestions/check-suggestion db text)]
      (if command
        (commands/set-chat-command db command)
        db))
    db))

(register-handler :set-chat-input-text
  ((enrich update-command) update-text))

(defn console? [s]
  (= "console" s))

(def not-console?
  (complement console?))

(defn check-author-direction
  [db chat-id {:keys [from outgoing] :as message}]
  (let [previous-message (first (get-in db [:chats chat-id :messages]))]
    (merge message
           {:same-author    (if previous-message
                              (= (:from previous-message) from)
                              true)
            :same-direction (if previous-message
                              (= (:outgoing previous-message) outgoing)
                              true)})))

(defn add-message-to-db
  [db chat-id message]
  (let [messages [:chats chat-id :messages]]
    (update-in db messages conj (assoc message :chat-id chat-id
                                               :new? true))))

(defn set-message-shown
  [db chat-id msg-id]
  (update-in db [:chats chat-id :messages] (fn [messages]
                                             (map (fn [msg]
                                                    (if (= msg-id (:msg-id msg))
                                                      (assoc msg :new? false)
                                                      msg))
                                                  messages))))

(register-handler :set-message-shown
  (fn [db [_ {:keys [chat-id msg-id]}]]
    (set-message-shown db chat-id msg-id)))

(defn prepare-message
  [{:keys [identity current-chat-id] :as db} _]
  (let [text    (get-in db [:chats current-chat-id :input-text])
        {:keys [command]} (suggestions/check-suggestion db (str text " "))
        message (check-author-direction
                  db current-chat-id
                  {:msg-id          (random/id)
                   :chat-id         current-chat-id
                   :content         text
                   :to              current-chat-id
                   :from            identity
                   :content-type    text-content-type
                   :outgoing        true
                   :timestamp       (time/now-ms)})]
    (if command
      (commands/set-chat-command db command)
      (assoc db :new-message (when-not (str/blank? text) message)))))

(defn prepare-command [identity chat-id staged-command]
  (let [command-key (get-in staged-command [:command :command])
        content     {:command (name command-key)
                     :content (:content staged-command)}]
    {:msg-id       (random/id)
     :from         identity
     :to           chat-id
     :content      content
     :content-type content-type-command
     :outgoing     true
     :handler      (:handler staged-command)}))

(defn prepare-staged-commans
  [{:keys [current-chat-id identity] :as db} _]
  (let [staged-commands (get-in db [:chats current-chat-id :staged-commands])]
    (->> staged-commands
         (map #(prepare-command identity current-chat-id %))
         ;todo this is wrong :(
         (map #(check-author-direction db current-chat-id %))
         (assoc db :new-commands))))

(defn add-message
  [{:keys [new-message current-chat-id] :as db}]
  (if new-message
    (add-message-to-db db current-chat-id new-message)
    db))

(defn add-commands
  [{:keys [new-commands current-chat-id] :as db}]
  (reduce
    #(add-message-to-db %1 current-chat-id %2)
    db
    new-commands))

(defn clear-input
  [{:keys [current-chat-id new-message] :as db} _]
  (if new-message
    (assoc-in db [:chats current-chat-id :input-text] nil)
    db))

(defn clear-staged-commands
  [{:keys [current-chat-id] :as db} _]
  (assoc-in db [:chats current-chat-id :staged-commands] []))

(defn send-message!
  [{:keys [new-message current-chat-id] :as db} _]
  (when (and new-message (not-console? current-chat-id))
    (let [{:keys [group-chat]} (get-in db [:chats current-chat-id])
          content (:content new-message)]
      (if group-chat
        (api/send-group-user-msg {:group-id current-chat-id
                                  :content  content})
        (api/send-user-msg {:to      current-chat-id
                            :content content})))))

(defn save-message-to-realm!
  [{:keys [new-message current-chat-id]} _]
  (when new-message
    (messages/save-message current-chat-id new-message)))

(defn save-commands-to-realm!
  [{:keys [new-commands current-chat-id]} _]
  (doseq [new-command new-commands]
    (messages/save-message current-chat-id (dissoc new-command :handler))))

(defn handle-commands
  [{:keys [new-commands]}]
  (doseq [{{content :content} :content
           handler            :handler} new-commands]
    (when handler
      (handler content))))

(register-handler :send-chat-msg
  (-> prepare-message
      ((enrich prepare-staged-commans))
      ((enrich add-message))
      ((enrich add-commands))
      ((enrich clear-input))
      ((enrich clear-staged-commands))
      ((after send-message!))
      ((after save-message-to-realm!))
      ((after save-commands-to-realm!))
      ((after handle-commands))))

(register-handler :unstage-command
  (fn [db [_ staged-command]]
    (commands/unstage-command db staged-command)))

(register-handler :set-chat-command
  (after #(dispatch [:animate-show-response]))
  (fn [db [_ command-key]]
    (-> db
        (commands/set-chat-command command-key)
        (assoc-in [:animations :command?] true))))

(register-handler :init-console-chat
  (fn [db [_]]
    (sign-up-service/init db)))

(register-handler :save-password
  (fn [db [_ password]]
    (sign-up-service/save-password password)
    (assoc db :password-saved true)))

(register-handler :sign-up
  (fn [db [_ phone-number]]
    ;; todo save phone number to db
    (let [formatted (format-phone-number phone-number)]
      (-> db
          (assoc :user-phone-number formatted)
          sign-up-service/start-listening-confirmation-code-sms
          (server/sign-up formatted sign-up-service/on-sign-up-response)))))

(register-handler :stop-listening-confirmation-code-sms
  (fn [db [_]]
    (sign-up-service/stop-listening-confirmation-code-sms db)))

(register-handler :sign-up-confirm
  (u/side-effect!
    (fn [_ [_ confirmation-code]]
      (server/sign-up-confirm confirmation-code sign-up-service/on-send-code-response))))

(register-handler :set-signed-up
  (fn [db [_ signed-up]]
    (sign-up-service/set-signed-up db signed-up)))

(defn load-messages!
  ([db] (load-messages! db nil))
  ([db _]
   (->> (:current-chat-id db)
        messages/get-messages
        (assoc db :messages))))

(defn init-chat
  ([db] (init-chat db nil))
  ([{:keys [messages current-chat-id] :as db} _]
   (assoc-in db [:chats current-chat-id :messages] messages)))

(register-handler :init-chat
  (-> load-messages!
      ((enrich init-chat))
      debug))

(defn initialize-chats
  [{:keys [loaded-chats] :as db} _]
  (let [chats (->> loaded-chats
                   (map (fn [{:keys [chat-id] :as chat}]
                          [chat-id chat]))
                   (into {}))
        ids   (set (keys chats))]
    (-> db
        (assoc :chats chats)
        (assoc :chats-ids ids)
        (dissoc :loaded-chats))))

(defn load-chats!
  [db _]
  (assoc db :loaded-chats (chats/chats-list)))

(register-handler :initialize-chats
  ((enrich initialize-chats) load-chats!))

(defn store-message!
  [{:keys [new-message]} [_ {chat-id :from}]]
  (messages/save-message chat-id new-message))

(defn receive-message
  [db [_ {chat-id :from :as message}]]
  (let [message' (check-author-direction db chat-id message)]
    (-> db
        (add-message-to-db chat-id message')
        (assoc :new-message message'))))

(register-handler :received-msg
  (-> receive-message
      ((after store-message!))))

(register-handler :group-received-msg
  (u/side-effect!
    (fn [_ [_ {chat-id :group-id :as msg}]]
      (messages/save-message chat-id msg))))

(defmethod nav/preload-data! :chat
  [{:keys [current-chat-id] :as db} [_ _ id]]
  (let [chat-id (or id current-chat-id)
        messages (get-in db [:chats chat-id :messages])
        db' (assoc db :current-chat-id chat-id)]
    (if (seq messages)
      db'
      (-> db'
          load-messages!
          init-chat))))

(defn prepare-chat
  [{:keys [contacts] :as db} [_ contcat-id]]
  (let [name (get-in contacts [contcat-id :name])
        chat {:chat-id    contcat-id
              :name       name
              :color      default-chat-color
              :group-chat false
              :is-active  true
              :timestamp  (.getTime (js/Date.))
              :contacts   [{:identity contcat-id}]}]
    (assoc db :new-chat chat)))

(defn add-chat [{:keys [new-chat] :as db} [_ chat-id]]
  (-> db
      (update :chats assoc chat-id new-chat)
      (update :chats-ids conj chat-id)))

(defn save-chat!
  [{:keys [new-chat]} _]
  (chats/create-chat new-chat))

(defn open-chat!
  [_ [_ chat-id]]
  (dispatch [:navigate-to :chat chat-id]))

(register-handler :start-chat
  (-> prepare-chat
      ((enrich add-chat))
      ((after save-chat!))
      ((after open-chat!))))

(register-handler :switch-command-suggestions
  (fn [db [_]]
    (suggestions/switch-command-suggestions db)))

(defn remove-chat
  [{:keys [current-chat-id] :as db} _]
  (update db :chats dissoc current-chat-id))

(defn notify-about-leaving!
  [{:keys [current-chat-id]} _]
  (api/leave-group-chat current-chat-id))

; todo do we really need this message?
(defn leaving-message!
  [{:keys [current-chat-id]} _]
  (messages/save-message
    current-chat-id
    {:from         "system"
     :msg-id       (random/id)
     :content      "You left this chat"
     :content-type text-content-type}))

(defn delete-messages!
  [{:keys [current-chat-id]} _]
  (r/write
    (fn []
      (r/delete (r/get-by-field :msgs :chat-id current-chat-id)))))

(defn delete-chat!
  [{:keys [current-chat-id]} _]
  (r/write
    (fn []
      (-> (r/get-by-field :chats :chat-id current-chat-id)
          (r/single)
          (r/delete)))))

(register-handler :leave-group-chat
  ;; todo oreder of operations tbd
  (after (fn [_ _] (dispatch [:navigation-replace :chat-list])))
  (-> remove-chat
      ;; todo uncomment
      ;((after notify-about-leaving!))
      ;((after leaving-message!))
      ((after delete-messages!))
      ((after delete-chat!))))
