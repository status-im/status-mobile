(ns status-im.chat.models.input
  (:require [clojure.string :as string]
            [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands.input]
            [status-im.chat.commands.sending :as commands.sending]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.models :as chat]
            [status-im.chat.models.message :as chat.message]
            [status-im.constants :as constants]
            [status-im.js-dependencies :as dependencies]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]))

(defn text->emoji
  "Replaces emojis in a specified `text`"
  [text]
  (when text
    (string/replace text
                    #":([a-z_\-+0-9]*):"
                    (fn [[original emoji-id]]
                      (if-let [emoji-map (object/get (object/get dependencies/emojis "lib") emoji-id)]
                        (object/get emoji-map "char")
                        original)))))

(fx/defn set-chat-input-text
  "Set input text for current-chat. Takes db and input text and cofx
  as arguments and returns new fx. Always clear all validation messages."
  [{{:keys [current-chat-id] :as db} :db} new-input]
  {:db (-> (chat/set-chat-ui-props db {:validation-messages nil})
           (assoc-in [:chats.ui/input-text current-chat-id] (text->emoji new-input)))})

(defn- start-cooldown [{:keys [db]} cooldowns]
  {:dispatch-later        [{:dispatch [:chat/disable-cooldown]
                            :ms       (chat.constants/cooldown-periods-ms
                                       cooldowns
                                       chat.constants/default-cooldown-period-ms)}]
   :show-cooldown-warning nil
   :db                    (assoc db
                                 :chat/cooldowns (if (= chat.constants/cooldown-reset-threshold cooldowns)
                                                   0
                                                   cooldowns)
                                 :chat/spam-messages-frequency 0
                                 :chat/cooldown-enabled? true)})

(fx/defn process-cooldown
  "Process cooldown to protect against message spammers"
  [{{:keys [chat/last-outgoing-message-sent-at
            chat/cooldowns
            chat/spam-messages-frequency
            current-chat-id] :as db} :db :as cofx}]
  (when (chat/public-chat? cofx current-chat-id)
    (let [spamming-fast? (< (- (datetime/timestamp) last-outgoing-message-sent-at)
                            (+ chat.constants/spam-interval-ms (* 1000 cooldowns)))
          spamming-frequently? (= chat.constants/spam-message-frequency-threshold spam-messages-frequency)]
      (cond-> {:db (assoc db
                          :chat/last-outgoing-message-sent-at (datetime/timestamp)
                          :chat/spam-messages-frequency (if spamming-fast?
                                                          (inc spam-messages-frequency)
                                                          0))}

        (and spamming-fast? spamming-frequently?)
        (start-cooldown (inc cooldowns))))))

(fx/defn chat-input-focus
  "Returns fx for focusing on active chat input reference"
  [{{:keys [current-chat-id chat-ui-props]} :db} ref]
  (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
    {::focus-rn-component cmp-ref}))

(fx/defn select-chat-input-command
  "Sets chat command and focuses on input"
  [{:keys [db] :as cofx} command params previous-command-message]
  (fx/merge cofx
            (commands.input/set-command-reference previous-command-message)
            (commands.input/select-chat-input-command command params)
            (chat-input-focus :input-ref)))

(fx/defn set-command-prefix
  "Sets command prefix character and focuses on input"
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (set-chat-input-text chat.constants/command-char)
            (chat-input-focus :input-ref)))

(fx/defn reply-to-message
  "Sets reference to previous chat message and focuses on input"
  [{:keys [db] :as cofx} message-id]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message] message-id)}
              (chat-input-focus :input-ref))))

(fx/defn cancel-message-reply
  "Cancels stage message reply"
  [{:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message] nil)}
              (chat-input-focus :input-ref))))

(defn command-complete-fx
  "command is complete, proceed with command processing"
  [input-text command {:keys [db now] :as cofx}]
  (fx/merge cofx
            (commands.sending/validate-and-send input-text command)
            (set-chat-input-text nil)
            (process-cooldown)))

(defn command-not-complete-fx
  "command is not complete, just add space after command if necessary"
  [input-text current-chat-id {:keys [db]}]
  {:db (cond-> db
         (not (commands.input/command-ends-with-space? input-text))
         (assoc-in [:chats current-chat-id :input-text]
                   (str input-text chat.constants/spacing-char)))})

(defn plain-text-message-fx
  "no command detected, when not empty, proceed by sending text message without command processing"
  [input-text current-chat-id {:keys [db] :as cofx}]
  (when-not (string/blank? input-text)
    (let [reply-to-message (get-in db [:chats current-chat-id :metadata :responding-to-message])]
      (fx/merge cofx
                {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message] nil)}
                (chat.message/send-message {:chat-id      current-chat-id
                                            :content-type constants/content-type-text
                                            :content      (cond-> {:chat-id current-chat-id
                                                                   :text    input-text}
                                                            reply-to-message
                                                            (assoc :response-to reply-to-message))})
                (commands.input/set-command-reference nil)
                (set-chat-input-text nil)
                (process-cooldown)))))

(fx/defn send-current-message
  "Sends message from current chat input"
  [{{:keys [current-chat-id id->command access-scope->command-id] :as db} :db :as cofx}]
  (let [input-text   (get-in db [:chats.ui/input-text current-chat-id])
        command      (commands.input/selected-chat-command
                      input-text nil (commands/chat-commands id->command
                                                             access-scope->command-id
                                                             (get-in db [:chats current-chat-id])))]
    (if command
      ;; Returns true if current input contains command
      (if (= :complete (:command-completion command))
        (command-complete-fx input-text command cofx)
        (command-not-complete-fx input-text current-chat-id cofx))
      (plain-text-message-fx input-text current-chat-id cofx))))

;; effects

(re-frame/reg-fx
 ::focus-rn-component
 (fn [ref]
   (try
     (.focus ref)
     (catch :default e
       (log/debug "Cannot focus the reference")))))
