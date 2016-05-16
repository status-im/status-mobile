(ns syng-im.chat.handlers
  (:require [re-frame.core :refer [register-handler enrich after debug dispatch]]
            [syng-im.models.commands :as commands]
            [clojure.string :as str]
            [syng-im.chat.suggestions :as suggestions]
            [syng-im.protocol.api :as api]
            [syng-im.models.messages :as messages]
            [syng-im.constants :refer [text-content-type
                                       content-type-command]]
            [syng-im.utils.random :as random]
            [syng-im.components.react :as r]
            [syng-im.handlers.sign-up :as sign-up-service]
            [syng-im.models.chats :as chats]
            [syng-im.navigation.handlers :as nav]
            [syng-im.models.chats :as c]))

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

(register-handler :set-chat-command-content
  (fn [db [_ content]]
    (commands/set-chat-command-content db content)))

(defn update-input-text
  [{:keys [current-chat-id] :as db} text]
  (assoc-in db [:chats current-chat-id :input-text] text))

(register-handler :stage-command
  (fn [{:keys [current-chat-id] :as db} _]
    (let [db           (update-input-text db nil)
          {:keys [command content]}
          (get-in db [:chats current-chat-id :command-input])
          command-info {:command command
                        :content content
                        :handler (:handler command)}]
      (commands/stage-command db command-info))))

(register-handler :set-response-chat-command
  (fn [db [_ to-msg-id command-key]]
    (commands/set-response-chat-command db to-msg-id command-key)))

(defn update-text
  [db [_ text]]
  (update-input-text db text))

(defn update-command [db [_ text]]
  (let [{:keys [command]} (suggestions/check-suggestion db text)]
    (commands/set-chat-command db command)))

(register-handler :set-chat-input-text
  ((enrich update-command) update-text))

(register-handler :send-group-chat-msg
  (fn [db [_ chat-id text]]
    (let [{msg-id       :msg-id
           {from :from} :msg} (api/send-group-user-msg {:group-id chat-id
                                                        :content  text})
          msg {:msg-id       msg-id
               :from         from
               :to           nil
               :content      text
               :content-type text-content-type
               :outgoing     true}]
      (messages/save-message chat-id msg))))

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
    (update-in db messages conj message)))

(defn prepare-message
  [{:keys [identity current-chat-id] :as db} _]
  (let [text    (get-in db [:chats current-chat-id :input-text])
        {:keys [command]} (suggestions/check-suggestion db (str text " "))
        message (check-author-direction
                  db current-chat-id
                  {:msg-id       (random/id)
                   :chat-id      current-chat-id
                   :content      text
                   :to           current-chat-id
                   :from         identity
                   :content-type text-content-type
                   :outgoing     true})]
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
  [{:keys [new-message current-chat-id]} _]
  (when (and new-message (not-console? current-chat-id))
    (api/send-user-msg {:to      current-chat-id
                        :content (:content new-message)})))

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
      ((after (fn [_ _] (r/dismiss-keyboard!))))
      ((after send-message!))
      ((after save-message-to-realm!))
      ((after save-commands-to-realm!))
      ((after handle-commands))))

(register-handler :unstage-command
  (fn [db [_ staged-command]]
    (let []
      (commands/unstage-command db staged-command))))

(register-handler :set-chat-command
  (fn [db [_ command-key]]
    ;; todo what is going on there?!
    (commands/set-chat-command db command-key)))

(register-handler :init-console-chat
  (fn [db [_]]
    (sign-up-service/init db)))

(register-handler :save-password
  (fn [db [_ password]]
    (sign-up-service/save-password password)
    (assoc db :password-saved true)))

(register-handler :sign-up
  (-> (fn [db [_ phone-number]]
        ;; todo save phone number to db
        (assoc db :user-phone-number phone-number))
      ((after (fn [& _] (sign-up-service/on-sign-up-response))))))

(register-handler :sign-up-confirm
  (fn [db [_ confirmation-code]]
    (sign-up-service/on-send-code-response confirmation-code)
    (sign-up-service/set-signed-up db true)))

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
  (fn [db [_ {chat-id :group-id :as msg}]]
    (messages/save-message chat-id msg)
    db))

(defn load-chat!
  [{:keys [chats current-chat-id] :as db}]
  (when-not (chats current-chat-id)
    (c/create-chat {}))
  db)

(defmethod nav/preload-data! :chat
  [{:keys [current-chat-id] :as db} [_ _ id]]
  (-> db
      (assoc :current-chat-id (or id current-chat-id))
      load-messages!
      init-chat))

(defn prepare-chat
  [{:keys [contacts] :as db} [_ contcat-id]]
  (let [name (get-in contacts [contcat-id :name])
        chat {:chat-id    contcat-id
              :name       name
              :group-chat false
              :is-active  true
              :timestamp  (.getTime (js/Date.))
              ;; todo how to choose color?
              ;; todo do we need to have some color for not group chat?
              :contacts   [{:identity         contcat-id
                            :text-color       :#FFFFFF
                            :background-color :#AB7967}]}]
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
      ((after open-chat!))
      debug))
