(ns status-im.chat.models.input
  (:require [clojure.string :as string]
            [goog.object :as object]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.models :as chat]
            [status-im.chat.models.message :as chat.message]
            [status-im.chat.models.message-content :as message-content]
            [status-im.constants :as constants]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]
            ["emojilib" :as emojis]
            [status-im.chat.models.mentions :as mentions]
            [status-im.utils.utils :as utils]
            [status-im.multiaccounts.update.core :as multiaccounts.update]))

(defn text->emoji
  "Replaces emojis in a specified `text`"
  [text]
  (utils/safe-replace text
                      #":([a-z_\-+0-9]*):"
                      (fn [[original emoji-id]]
                        (if-let [emoji-map (object/get (.-lib emojis) emoji-id)]
                          (.-char ^js emoji-map)
                          original))))

(fx/defn set-chat-input-text
  "Set input text for current-chat. Takes db and input text and cofx
  as arguments and returns new fx. Always clear all validation messages."
  {:events [:chat.ui/set-chat-input-text]}
  [{{:keys [current-chat-id] :as db} :db} new-input]
  {:db (assoc-in db [:chat/inputs current-chat-id :input-text] (text->emoji new-input))})

(fx/defn select-mention
  {:events [:chat.ui/select-mention]}
  [{:keys [db] :as cofx} text-input-ref {:keys [alias name searched-text match] :as user}]
  (let [chat-id     (:current-chat-id db)
        new-text    (mentions/new-input-text-with-mention cofx user)
        at-sign-idx (get-in db [:chats chat-id :mentions :at-sign-idx])
        cursor      (+ at-sign-idx (count name) 2)]
    (fx/merge
     cofx
     {:db (-> db
              (assoc-in [:chats/cursor chat-id] cursor)
              (assoc-in [:chats/mention-suggestions chat-id] nil))}
     (set-chat-input-text new-text)
     ;; NOTE(rasom): Some keyboards do not react on selection property passed to
     ;; text input (specifically Samsung keyboard with predictive text set on).
     ;; In this case, if the user continues typing after the programmatic change,
     ;; the new text is added to the last known cursor position before
     ;; programmatic change. By calling `reset-text-input-cursor` we force the
     ;; keyboard's cursor position to be changed before the next input.
     (mentions/reset-text-input-cursor text-input-ref cursor)
     ;; NOTE(roman): on-text-input event is not dispatched when we change input
     ;; programmatically, so we have to call `on-text-input` manually
     (mentions/on-text-input
      (let [match-len         (count match)
            searched-text-len (count searched-text)
            start             (inc at-sign-idx)
            end               (+ start match-len)]
        {:new-text      match
         :previous-text searched-text
         :start         start
         :end           end}))
     (mentions/recheck-at-idxs {alias user}))))

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

(fx/defn reply-to-message
  "Sets reference to previous chat message and focuses on input"
  {:events [:chat.ui/reply-to-message]}
  [{:keys [db] :as cofx} message]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message]
                                 message)
                       (update-in [:chat/inputs current-chat-id :metadata]
                                  dissoc :sending-image))})))

(fx/defn cancel-message-reply
  "Cancels stage message reply"
  {:events [:chat.ui/cancel-message-reply]}
  [{:keys [db]}]
  (let [current-chat-id (:current-chat-id db)]
    {:db (assoc-in db [:chat/inputs current-chat-id :metadata :responding-to-message] nil)}))

(defn build-text-message
  [{:keys [db]} input-text current-chat-id]
  (when-not (string/blank? input-text)
    (let [{:keys [message-id]}
          (get-in db [:chat/inputs current-chat-id :metadata :responding-to-message])
          preferred-name (get-in db [:multiaccount :preferred-name])
          emoji? (message-content/emoji-only-content? {:text input-text
                                                       :response-to message-id})]
      {:chat-id      current-chat-id
       :content-type (if emoji?
                       constants/content-type-emoji
                       constants/content-type-text)
       :text input-text
       :response-to message-id
       :ens-name preferred-name})))

(defn build-image-messages
  [{{:keys [current-chat-id] :as db} :db} chat-id]
  (let [images (get-in db [:chat/inputs current-chat-id :metadata :sending-image])]
    (mapv (fn [[_ {:keys [uri]}]]
            {:chat-id      chat-id
             :content-type constants/content-type-image
             :image-path   (utils/safe-replace uri #"file://" "")
             :text         (i18n/label :t/update-to-see-image)})
          images)))

(fx/defn clean-input [{:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:chat/inputs current-chat-id :metadata :sending-image] nil)
                       (assoc-in [:chat/inputs current-chat-id :metadata :responding-to-message] nil))}
              (set-chat-input-text nil))))

(fx/defn send-messages [{:keys [db] :as cofx} input-text current-chat-id]
  (let [image-messages (build-image-messages cofx current-chat-id)
        text-message (build-text-message cofx input-text current-chat-id)
        messages (keep identity (conj image-messages text-message))]
    (when (seq messages)
      (fx/merge cofx
                (clean-input cofx)
                (process-cooldown)
                (chat.message/send-messages messages)))))

(fx/defn send-my-status-message
  "when not empty, proceed by sending text message with public key topic"
  {:events [:profile.ui/send-my-status-message]}
  [{{:keys [current-chat-id] :as db} :db :as cofx}]
  (let [{:keys [input-text]} (get-in db [:chat/inputs current-chat-id])
        chat-id (chat/profile-chat-topic (get-in db [:multiaccount :public-key]))]
    (send-messages cofx input-text chat-id)))

(fx/defn send-audio-message
  [cofx audio-path duration current-chat-id]
  (when-not (string/blank? audio-path)
    (chat.message/send-message cofx {:chat-id           current-chat-id
                                     :content-type      constants/content-type-audio
                                     :audio-path        audio-path
                                     :audio-duration-ms duration
                                     :text              (i18n/label :t/update-to-listen-audio)})))

(fx/defn send-sticker-message
  [cofx {:keys [hash pack]} current-chat-id]
  (when-not (string/blank? hash)
    (chat.message/send-message cofx {:chat-id      current-chat-id
                                     :content-type constants/content-type-sticker
                                     :sticker {:hash hash
                                               :pack pack}
                                     :text    (i18n/label :t/update-to-see-sticker)})))

(fx/defn send-current-message
  "Sends message from current chat input"
  {:events [:chat.ui/send-current-message]}
  [{{:keys [current-chat-id] :as db} :db :as cofx}]
  (let [{:keys [input-text]} (get-in db [:chat/inputs current-chat-id])
        input-text-with-mentions (mentions/check-mentions cofx input-text)]
    (fx/merge cofx
              (send-messages input-text-with-mentions current-chat-id)
              (mentions/clear-mentions)
              (mentions/clear-cursor))))

(fx/defn chat-send-sticker
  {:events [:chat/send-sticker]}
  [{{:keys [current-chat-id multiaccount]} :db :as cofx} {:keys [hash] :as sticker}]
  (fx/merge
   cofx
   (multiaccounts.update/multiaccount-update
    :stickers/recent-stickers
    (conj (remove #(= hash %) (:stickers/recent-stickers multiaccount)) hash)
    {})
   (send-sticker-message sticker current-chat-id)))

(fx/defn chat-send-audio
  {:events [:chat/send-audio]}
  [{{:keys [current-chat-id]} :db :as cofx} audio-path duration]
  (send-audio-message cofx audio-path duration current-chat-id))
