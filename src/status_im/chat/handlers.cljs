(ns status-im.chat.handlers
  (:require [re-frame.core :refer [enrich after debug dispatch]]
            [status-im.models.commands :as commands]
            [clojure.string :as str]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.chat.suggestions :as suggestions]
            [status-im.protocol.api :as api]
            [status-im.models.messages :as messages]
            [status-im.constants :refer [text-content-type
                                         content-type-command
                                         content-type-command-request
                                         default-number-of-messages]]
            [status-im.utils.random :as random]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.models.chats :as chats]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.persistence.realm.core :as r]
            [status-im.handlers.server :as server]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.utils.datetime :as time]
            [status-im.components.react :refer [geth]]
            [status-im.utils.logging :as log]
            [status-im.components.jail :as j]
            [status-im.utils.types :refer [json->clj]]
            [status-im.commands.utils :refer [generate-hiccup]]))

(register-handler :set-show-actions
  (fn [db [_ show-actions]]
    (assoc db :show-actions show-actions)))

(register-handler :load-more-messages
  (fn [{:keys [is-logged-in current-chat-id] :as db} _]
    (let [all-loaded? (get-in db [:chats current-chat-id :all-loaded?])
          account-realm-exists? (or (not= current-chat-id "console") is-logged-in)]
      (if (or all-loaded? (not account-realm-exists?))
        db
        (let [messages-path [:chats current-chat-id :messages]
              messages (get-in db messages-path)
              new-messages (messages/get-messages current-chat-id (count messages))
              all-loaded? (> default-number-of-messages (count new-messages))]
          (-> db
              (update-in messages-path concat new-messages)
              (assoc-in [:chats current-chat-id :all-loaded?] all-loaded?)))))))

(defn safe-trim [s]
  (when (string? s)
    (str/trim s)))

(register-handler :cancel-command
  (fn [{:keys [current-chat-id] :as db} _]
    (-> db
        (assoc-in [:chats current-chat-id :command-input] {})
        (update-in [:chats current-chat-id :input-text] safe-trim))))

(defn invoke-suggestions-handler!
  [{:keys [current-chat-id canceled-command] :as db} _]
  (when-not canceled-command
    (let [{:keys [command content]} (get-in db [:chats current-chat-id :command-input])
          {:keys [name type]} command
          path [(if (= :command type) :commands :responses)
                name
                :params
                0
                :suggestions]
          params {:value content}]
      (j/call current-chat-id
              path
              params
              #(dispatch [:suggestions-handler {:command command
                                                :content content
                                                :chat-id current-chat-id} %])))))

(register-handler :start-cancel-command
  (u/side-effect!
    (fn [db _]
      (dispatch [:animate-cancel-command]))))

(def command-prefix "c ")

(defn cancel-command!
  [{:keys [canceled-command]}]
  (when canceled-command
    (dispatch [:start-cancel-command])))

(register-handler :set-chat-command-content
  [(after invoke-suggestions-handler!)
   (after cancel-command!)]
  (fn [{:keys [current-chat-id] :as db} [_ content]]
    (let [starts-as-command? (str/starts-with? content command-prefix)
          path [:chats current-chat-id :command-input :command :type]
          command? (= :command (get-in db path))]
      (as-> db db
            (commands/set-chat-command-content db content)
            (assoc-in db [:chats current-chat-id :input-text] nil)
            (assoc db :canceled-command (and command? (not starts-as-command?)))))))

(defn update-input-text
  [{:keys [current-chat-id] :as db} text]
  (assoc-in db [:chats current-chat-id :input-text] text))

(defn invoke-command-preview!
  [{:keys [current-chat-id staged-command]} _]
  (let [{:keys [command content]} staged-command
        {:keys [name type]} command
        path [(if (= :command type) :commands :responses)
              name
              :preview]
        params {:value content}]
    (j/call current-chat-id
            path
            params
            #(dispatch [:command-preview current-chat-id %]))))

(register-handler :stage-command
  (after invoke-command-preview!)
  (fn [{:keys [current-chat-id] :as db} _]
    (let [db (update-input-text db nil)
          {:keys [command content to-msg-id]}
          (get-in db [:chats current-chat-id :command-input])
          content' (if (= :command (:type command))
                     (subs content 2)
                     content)
          command-info {:command    command
                        :content    content'
                        :to-message to-msg-id}]
      (-> db
          (commands/stage-command command-info)
          (assoc :staged-command command-info)))))

(register-handler :set-message-input []
  (fn [db [_ input]]
    (assoc db :message-input input)))

(register-handler :blur-message-input
  (u/side-effect!
    (fn [db _]
      (when-let [message-input (:message-input db)]
        (.blur message-input)))))

(register-handler :set-response-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:command-edit-mode]))
   (after #(dispatch [:set-chat-input-text ""]))]
  (fn [db [_ to-msg-id command-key]]
    (-> db
        (commands/set-response-chat-command to-msg-id command-key)
        (assoc :canceled-command false))))

(defn update-text
  [{:keys [current-chat-id] :as db} [_ text]]
  (let [suggestions (get-in db [:command-suggestions current-chat-id])]
    (if-not (= 1 (count suggestions))
      (update-input-text db text)
      (assoc db :disable-input true))))

(defn update-command [db [_ text]]
  (if-not (commands/get-chat-command db)
    (let [{:keys [command]} (suggestions/check-suggestion db text)]
      (if command
        (commands/set-chat-command db command)
        db))
    db))

(defn check-suggestions
  [{:keys [current-chat-id] :as db} [_ text]]
  (let [suggestions (suggestions/get-suggestions db text)]
    (assoc-in db [:command-suggestions current-chat-id] suggestions)))

(defn select-suggestion!
  [{:keys [current-chat-id] :as db} [_ text]]
  (let [suggestions (get-in db [:command-suggestions current-chat-id])]
    (when (= 1 (count suggestions))
      (dispatch [:set-chat-command (ffirst suggestions)]))))

(register-handler :set-chat-input-text
  [(enrich update-command)
   (after select-suggestion!)
   (after #(dispatch [:animate-command-suggestions]))]
  ((enrich update-text) check-suggestions))

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
  (let [text (get-in db [:chats current-chat-id :input-text])
        [command] (suggestions/check-suggestion db (str text " "))
        message (check-author-direction
                  db current-chat-id
                  {:msg-id       (random/id)
                   :chat-id      current-chat-id
                   :content      text
                   :to           current-chat-id
                   :from         identity
                   :content-type text-content-type
                   :outgoing     true
                   :timestamp    (time/now-ms)})]
    (if command
      (commands/set-chat-command db command)
      (assoc db :new-message (when-not (str/blank? text) message)))))

(defn prepare-command
  [identity chat-id {:keys [preview preview-string content command to-message]}]
  (let [content {:command (command :name)
                 :content content}]
    {:msg-id           (random/id)
     :from             identity
     :to               chat-id
     :content          content
     :content-type     content-type-command
     :outgoing         true
     :preview          preview-string
     :rendered-preview preview
     :to-message       to-message}))

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
  [{:keys [is-logged-in new-message current-chat-id]} _]
  (when (and new-message is-logged-in)
    (messages/save-message current-chat-id new-message)))

(defn save-commands-to-realm!
  [{:keys [is-logged-in new-commands current-chat-id]} _]
  (when is-logged-in
    (doseq [new-command new-commands]
      (messages/save-message
        current-chat-id
        (dissoc new-command :rendered-preview :to-message)))))

(defn dispatch-responded-requests!
  [{:keys [new-commands current-chat-id]} _]
  (doseq [{:keys [is-logged-in to-message]} new-commands]
    (when (and to-message is-logged-in)
      (dispatch [:request-answered! current-chat-id to-message]))))

(defn invoke-commands-handlers!
  [{:keys [new-commands current-chat-id]}]
  (doseq [{:keys [content] :as com} new-commands]
    (let [{:keys [command content]} content
          type (:type command)
          path [(if (= :command type) :commands :responses)
                command
                :handler]
          params {:value content}]
      (j/call current-chat-id
              path
              params
              #(dispatch [:command-handler! com %])))))

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
      ((after dispatch-responded-requests!))
      ;; todo maybe it is better to track if it was handled or not
      ((after invoke-commands-handlers!))
      ((after handle-commands))))

(register-handler :unstage-command
  (fn [db [_ staged-command]]
    (commands/unstage-command db staged-command)))

(register-handler :set-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:command-edit-mode]))]
  (fn [{:keys [current-chat-id] :as db} [_ command-key]]
    (-> db
        (commands/set-chat-command command-key)
        (assoc-in [:chats current-chat-id :command-input :content] "c ")
        (assoc :disable-input true))))

(register-handler :init-console-chat
  (fn [db [_]]
    (sign-up-service/init db)))

(register-handler :save-password
  (fn [db [_ password]]
    (dispatch [:create-account password])
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
  ([{:keys [current-chat-id] :as db} _]
   (assoc db :messages (messages/get-messages current-chat-id))))

(defn init-chat
  ([db] (init-chat db nil))
  ([{:keys [messages current-chat-id] :as db} _]
   (assoc-in db [:chats current-chat-id :messages] messages)))

(defn load-commands!
  [{:keys [current-chat-id]}]
  (dispatch [:load-commands! current-chat-id]))

(register-handler :init-chat
  (after #(dispatch [:load-requests!]))
  (-> load-messages!
      ((enrich init-chat))
      ((after load-commands!))))


(register-handler :save-console
  (fn [db _]
    (when-not (chats/chat-exists? "console")
      (let [console-chat (get-in db [:chats "console"])]
        (when console-chat
          (do
            (chats/create-chat console-chat)
            (doseq [message (reverse (:messages console-chat))]
              (messages/save-message "console" message))))))
    db))

(defn initialize-chats
  [{:keys [loaded-chats] :as db} _]
  (let [chats (->> loaded-chats
                   (map (fn [{:keys [chat-id] :as chat}]
                          [chat-id chat]))
                   (into {}))
        ids (set (keys chats))]
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
  [{:keys [is-logged-in new-message]} [_ {chat-id :from}]]
  (if (or (not= chat-id "console") is-logged-in)
    (messages/save-message chat-id new-message)))

(defn dispatch-request!
  [{:keys [new-message]} [_ {chat-id :from}]]
  (log/debug "Dispatching request: " new-message)
  (when (= (:content-type new-message) content-type-command-request)
    (dispatch [:add-request chat-id new-message])))

(defn receive-message
  [db [_ {chat-id :from :as message}]]
  (let [message' (check-author-direction db chat-id message)]
    (-> db
        (add-message-to-db chat-id message')
        (assoc :new-message message'))))

(register-handler :received-msg
  [(after store-message!)
   (after dispatch-request!)]
  receive-message)

(register-handler :group-received-msg
  (u/side-effect!
    (fn [_ [_ {chat-id :group-id :as msg}]]
      (messages/save-message chat-id msg))))

(defmethod nav/preload-data! :chat
  [{:keys [is-logged-in current-chat-id] :as db} [_ _ id]]
  (let [chat-id (or id current-chat-id)
        messages (get-in db [:chats chat-id :messages])
        db' (assoc db :current-chat-id chat-id)
        account-realm-exists? (or (not= chat-id "console") is-logged-in)]
    (when account-realm-exists?
      (dispatch [:load-requests! chat-id]))
    (if (seq messages)
      db'
      (-> db'
          ((fn [db] 
            (if account-realm-exists?
              (load-messages! db)
              db)))
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
              :contacts   [{:identity contcat-id}]
              :dapp-url   nil
              :dapp-hash  nil}]
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

(register-handler :switch-command-suggestions!
  (u/side-effect!
    (fn [db]
      (let [text (if (suggestions/typing-command? db) "" "!")]
        (dispatch [:set-chat-input-text text])))))

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
  (r/write :account
    (fn []
      (r/delete :account (r/get-by-field :account :msgs :chat-id current-chat-id)))))

(defn delete-chat!
  [{:keys [current-chat-id]} _]
  (r/write :account
    (fn [] :account
      (->> (r/get-by-field :account :chats :chat-id current-chat-id)
           (r/single)
           (r/delete :account)))))

(register-handler :leave-group-chat
  ;; todo oreder of operations tbd
  (after (fn [_ _] (dispatch [:navigation-replace :chat-list])))
  (-> remove-chat
      ;; todo uncomment
      ;((after notify-about-leaving!))
      ;((after leaving-message!))
      ((after delete-messages!))
      ((after delete-chat!))))

(defn edit-mode-handler [mode]
  (fn [{:keys [current-chat-id] :as db} _]
    (assoc-in db [:edit-mode current-chat-id] mode)))

(register-handler :command-edit-mode
  (edit-mode-handler :command))

(register-handler :text-edit-mode
  (after #(dispatch [:set-chat-input-text ""]))
  (edit-mode-handler :text))

(register-handler :set-layout-height
  [(after
     (fn [{:keys [current-chat-id] :as db}]
       (let [suggestions (get-in db [:suggestions current-chat-id])
             mode (get-in db [:edit-mode current-chat-id])]
         (when (and (= :command mode) (seq suggestions))
           (dispatch [:fix-response-height])))))
   (after
     (fn [{:keys [current-chat-id] :as db}]
       (let [suggestions (get-in db [:command-suggestions current-chat-id])
             mode (get-in db [:edit-mode current-chat-id])]
         (when (and (= :text mode)) (seq suggestions)
           (dispatch [:fix-commands-suggestions-height])))))]
  (fn [db [_ h]]
    (assoc db :layout-height h)))
