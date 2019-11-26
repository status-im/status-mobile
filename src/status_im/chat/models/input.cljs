(ns status-im.chat.models.input
  (:require [clojure.string :as string]
            [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.models :as chat]
            [status-im.chat.models.message-content :as message-content]
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
  {:db (assoc-in db [:chats current-chat-id :input-text] (text->emoji new-input))})

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

(fx/defn reply-to-message
  "Sets reference to previous chat message and focuses on input"
  [{:keys [db] :as cofx} message-id]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message]
                             {:message-id     message-id})}
              (chat-input-focus :input-ref))))

(fx/defn cancel-message-reply
  "Cancels stage message reply"
  [{:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message] nil)}
              (chat-input-focus :input-ref))))

(defn plain-text-message-fx
  "when not empty, proceed by sending text message"
  [input-text current-chat-id {:keys [db] :as cofx}]
  (when-not (string/blank? input-text)
    (let [{:keys [message-id]}
          (get-in db [:chats current-chat-id :metadata :responding-to-message])
          show-name?     (get-in db [:multiaccount :show-name?])
          preferred-name (when show-name? (get-in db [:multiaccount :preferred-name]))
          emoji? (message-content/emoji-only-content? {:text input-text
                                                       :response-to message-id})]
      (fx/merge cofx
                {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message] nil)}
                (chat.message/send-message {:chat-id      current-chat-id
                                            :content-type (if emoji?
                                                            constants/content-type-emoji
                                                            constants/content-type-text)
                                            :text input-text
                                            :response-to message-id
                                            :ens-name preferred-name})
                (set-chat-input-text nil)
                (process-cooldown)))))

(fx/defn send-sticker-fx
  [{:keys [db] :as cofx} {:keys [hash pack]} current-chat-id]
  (when-not (string/blank? hash)
    (chat.message/send-message cofx {:chat-id      current-chat-id
                                     :content-type constants/content-type-sticker
                                     :sticker {:hash hash
                                               :pack pack}
                                     :text    "Update to latest version to see a nice sticker here!"})))

(fx/defn send-current-message
  "Sends message from current chat input"
  [{{:keys [current-chat-id] :as db} :db :as cofx}]
  (let [{:keys [input-text]} (get-in db [:chats current-chat-id])]
    (plain-text-message-fx input-text current-chat-id cofx)))

(fx/defn send-transaction-result
  {:events [:chat/send-transaction-result]}
  [cofx chat-id params result]
  ;;TODO: should be implemented on status-go side
  ;;see https://github.com/status-im/team-core/blob/6c3d67d8e8bd8500abe52dab06a59e976ec942d2/rfc-001.md#status-gostatus-react-interface
)

;; effects

(re-frame/reg-fx
 ::focus-rn-component
 (fn [ref]
   (try
     (.focus ref)
     (catch :default e
       (log/debug "Cannot focus the reference")))))
