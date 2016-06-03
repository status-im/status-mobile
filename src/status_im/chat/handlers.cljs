(ns status-im.chat.handlers
  (:require [re-frame.core :refer [register-handler enrich after debug dispatch]]
            [status-im.models.commands :as commands]
            [clojure.string :as str]
            [status-im.components.drag-drop :as drag]
            [status-im.components.animation :as anim]
            [status-im.components.styles :refer [default-chat-color]]
            [status-im.chat.styles.response :refer [request-info-height response-height-normal]]
            [status-im.chat.styles.response-suggestions :as response-suggestions-styles]
            [status-im.chat.suggestions :as suggestions]
            [status-im.protocol.api :as api]
            [status-im.models.messages :as messages]
            [status-im.constants :refer [text-content-type
                                         content-type-command
                                         response-input-hiding-duration]]
            [status-im.utils.random :as random]
            [status-im.chat.sign-up :as sign-up-service]
            [status-im.models.chats :as chats]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :as u]
            [status-im.persistence.realm :as r]
            [status-im.handlers.server :as server]
            [status-im.handlers.content-suggestions :refer [get-content-suggestions]]
            [status-im.utils.phone-number :refer [format-phone-number]]
            [status-im.utils.datetime :as time]))

(def delta 1)

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
        (assoc-in [:animations :commands-input-is-switching?] false)
        (assoc-in [:chats current-chat-id :command-input] {})
        (update-in [:chats current-chat-id :input-text] safe-trim))))

(defn animate-cancel-command! [{{:keys [response-height-anim-value
                                        message-input-buttons-scale
                                        message-input-offset
                                        messages-offset-anim-value]} :animations}]
  (let [height-to-value 1]
    (anim/add-listener response-height-anim-value
                       (fn [val]
                         (when (<= (- height-to-value delta) (anim/value val) (+ height-to-value delta))
                           (anim/remove-all-listeners response-height-anim-value)
                           (dispatch [:cancel-command]))))
    (anim/start (anim/spring response-height-anim-value {:toValue  height-to-value
                                                         :velocity 1
                                                         :tension  1
                                                         :friction 5}))
    (anim/start (anim/timing message-input-buttons-scale {:toValue  1
                                                          :duration response-input-hiding-duration}))
    (anim/start (anim/timing message-input-offset {:toValue  0
                                                   :duration response-input-hiding-duration}))
    (anim/start (anim/spring messages-offset-anim-value {:toValue 0}))))

(register-handler :start-cancel-command
  (after animate-cancel-command!)
  (fn [db _]
    (let [hiding? (get-in db [:animations :commands-input-is-switching?])]
      (if-not hiding?
        (assoc-in db [:animations :commands-input-is-switching?] true)
        db))))

(register-handler :finish-animate-response-resize
  (fn [db _]
    (let [fixed (get-in db [:animations :response-height-fixed])]
      (-> db
          (assoc-in [:animations :response-height] fixed)
          (assoc-in [:animations :response-resize?] false)))))

(defn animate-response-resize! [{{height-anim-value :response-height-anim-value
                                      from              :response-height
                                      to                :response-height-fixed} :animations}]
  (let [delta 5]
    (anim/set-value height-anim-value from)
    (anim/add-listener height-anim-value
                       (fn [val]
                         (when (<= (- to delta) (anim/value val) (+ to delta))
                           (anim/remove-all-listeners height-anim-value)
                           (dispatch [:finish-animate-response-resize]))))
    (anim/start (anim/spring height-anim-value {:toValue to}))))

(register-handler :animate-response-resize
  (after animate-response-resize!)
  (fn [db _]
    (assoc-in db [:animations :response-resize?] true)))

(defn get-response-height [db]
  (when (commands/get-chat-command-to-msg-id db)
    (let [command (commands/get-chat-command db)
          text (commands/get-chat-command-content db)
          suggestions (get-content-suggestions command text)
          suggestions-height (reduce + 0 (map #(if (:header %)
                                                response-suggestions-styles/header-height
                                                response-suggestions-styles/suggestion-height)
                                              suggestions))]
      (min response-height-normal (+ suggestions-height request-info-height)))))

(defn update-response-height [db]
  (when (commands/get-chat-command-to-msg-id db)
    (assoc-in db [:animations :response-height-fixed] (get-response-height db))))

(register-handler :set-chat-command-content
  (fn [{:keys [current-chat-id] :as db} [_ content]]
    (dispatch [:animate-response-resize])
    (-> db
        (commands/set-chat-command-content content)
        (assoc-in [:chats current-chat-id :input-text] nil)
        (update-response-height))))

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

(register-handler :finish-show-response!
  (fn [db _]
    (assoc-in db [:animations :commands-input-is-switching?] false)))

(defn animate-show-response! [{{scale-anim-value           :message-input-buttons-scale
                                input-offset-anim-value    :message-input-offset
                                messages-offset-anim-value :messages-offset-anim-value} :animations}]
  (let [to-value 0.1
        delta 0.02]
    (anim/add-listener scale-anim-value
                       (fn [val]
                         (when (<= (- to-value delta) (anim/value val) (+ to-value delta))
                           (anim/remove-all-listeners scale-anim-value)
                           (dispatch [:finish-show-response!]))))
    (anim/start (anim/timing scale-anim-value {:toValue  to-value
                                               :duration response-input-hiding-duration}))
    (anim/start (anim/timing input-offset-anim-value {:toValue  -40
                                                      :duration response-input-hiding-duration}))
    (anim/start (anim/spring messages-offset-anim-value {:toValue request-info-height}))))

(defn set-response-chat-command [db [_ to-msg-id command-key]]
  (dispatch [:animate-response-resize])
  (-> db
      (commands/set-response-chat-command to-msg-id command-key)
      (assoc-in [:animations :commands-input-is-switching?] true)
      (assoc-in [:animations :response-height] 0)
      (update-response-height)))

(register-handler :set-response-chat-command
  (after animate-show-response!)
  set-response-chat-command)

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
    (update-in db messages conj message)))

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
  (fn [db [_ confirmation-code]]
    (server/sign-up-confirm confirmation-code sign-up-service/on-send-code-response)
    db))

(register-handler :set-signed-up
  (fn [db [_ signed-up]]
    (sign-up-service/set-signed-up db signed-up)))

(defn load-messages!
  ([db] (load-messages! db nil))
  ([db _]
   (->> (:current-chat-id db)
        messages/get-messages
        (assoc db :messages))))

(register-handler :set-response-max-height
  (fn [db [_ height]]
    (assoc-in db [:animations :response-height-max] height)))

(register-handler :on-drag-response
  (fn [db [_ dy]]
    (let [fixed (get-in db [:animations :response-height-fixed])]
      (assoc-in db [:animations :response-height] (- fixed dy)))))

(register-handler :fix-response-height
  (fn [db _]
    (let [current (get-in db [:animations :response-height])
          normal-height response-height-normal
          max-height (get-in db [:animations :response-height-max])
          delta (/ normal-height 2)
          new-fixed (cond
                      (<= current delta) request-info-height
                      (<= current (+ normal-height delta)) (get-response-height db)
                      :else max-height)]
      (dispatch [:animate-response-resize])
      (assoc-in db [:animations :response-height-fixed] new-fixed))))

(defn create-response-pan-responder []
  (drag/create-pan-responder
    {:on-move    (fn [e gesture]
                   (dispatch [:on-drag-response (.-dy gesture)]))
     :on-release (fn [e gesture]
                   (dispatch [:fix-response-height]))}))

(defn init-response-dragging [db]
  (assoc-in db [:animations :response-pan-responder] (create-response-pan-responder)))

(defn init-chat
  ([db] (init-chat db nil))
  ([{:keys [messages current-chat-id] :as db} _]
   (-> db
       (assoc-in [:chats current-chat-id :messages] messages)
       (init-response-dragging))))

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
