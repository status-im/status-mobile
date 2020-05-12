(ns status-im.chat.models.input
  (:require [clojure.string :as string]
            [goog.object :as object]
            [re-frame.core :as re-frame]
            [status-im.chat.constants :as chat.constants]
            [status-im.chat.models :as chat]
            [status-im.chat.models.message :as chat.message]
            [status-im.chat.models.message-content :as message-content]
            [status-im.constants :as constants]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            ["emojilib" :as emojis]))

(defn text->emoji
  "Replaces emojis in a specified `text`"
  [text]
  (when text
    (string/replace text
                    #":([a-z_\-+0-9]*):"
                    (fn [[original emoji-id]]
                      (if-let [emoji-map (object/get (.-lib emojis) emoji-id)]
                        (.-char ^js emoji-map)
                        original)))))

(defn get-suggestions
  "Returns a sorted list of users matching the input"
  [users input]
  (let [input (string/lower-case input)]
    (->> (filter (fn [[alias _]]
                   (when (string? alias)
                     (-> alias
                         string/lower-case
                         (string/starts-with? input))))
                 users)
         vals
         (sort-by (comp string/lower-case :alias))
         seq)))

(defn tag-text-parts
  "Takes a string, a map of users and a regexp of known mentions
  and splits the string into parts each parts being a vector
  with a part type:
  - `text` for regular text
  - `mention` for mentions, which will contain the user alias and public key
  This is used to quickly render the input field with the proper styling"
  [text users mention-regexp]
  (if mention-regexp
    (loop [[text mention & rest] (string/split text mention-regexp)
           previous-mention? false
           computed-text []]
      (if mention
        (let [mention (subs mention 1)]
          (recur rest true (conj computed-text
                                 [:text (if previous-mention?
                                          (subs text 1)
                                          text)]
                                 [:mention
                                  mention
                                  (get-in users [mention :public-key])])))
        (if text
          (conj computed-text
                [:text (if previous-mention?
                         (subs text 1)
                         text)])
          computed-text)))
    [[:text text]]))

(def end-mention-regexp
  #"(@(?:(?:[A-Z][a-z]*(?:\s[A-Z]?[a-z]*)?(?:\s[A-Z]?[a-z]*)?)|(?:[a-zA-Z0-9\.]*))$)")

(defn compute-all-text-parts
  "Takes the chat inputs and a cursor position and extracts the mentions
  from the text, returning new chat inputs which are important params for
  rendering (suggestions, tagged text parts, new cursor position)"
  [{:keys [mentioned users text]} cursor]
  (let [text (or text "")
        ;; split text at current cursor position
        text-before-cursor (subs text 0 cursor)
        text-after-cursor (subs text cursor)
        ;;check if user is completing a mention
        [completing? _] (re-find end-mention-regexp
                                 text-before-cursor)
        ;; update suggestions
        suggestions (if completing?
                      (get-suggestions users
                                       (subs completing? 1))
                      [])
        ;; check if a mention has been completed
        completed?
        (when (and completing?
                   (= 1 (count suggestions))
                   (= (string/lower-case completing?)
                      (-> suggestions
                          first
                          :alias
                          string/lower-case)))
          completing?)
        ;; remove the current mention from the text before the cursor
        text-before-cursor (if (or completed?
                                   (not completing?))
                             text-before-cursor
                             (subs text-before-cursor
                                   0
                                   (- (count text-before-cursor)
                                      (count completing?))))
        text-after-cursor (str (when completed? " ") text-after-cursor)
        mentioned (cond-> mentioned
                    completed? (conj mentioned completed?))
        ;; prepare regexp for currently mentioned users
        mention-regexp
        (when (seq mentioned)
          (re-pattern (str "(@" (string/replace
                                 (string/join "|@" mentioned)
                                 " " "\\s")
                           ")")))]
    (cond-> {:users users
             :text text
             :before-cursor (tag-text-parts text-before-cursor
                                            users
                                            mention-regexp)
             :after-cursor (tag-text-parts text-after-cursor
                                           users
                                           mention-regexp)}
      mentioned
      (assoc :mentioned mentioned)
      completed?
      (assoc :cursor (inc (count text-before-cursor)))
      (and completing?
           (not completed?))
      (assoc :completing? [(if (seq suggestions)
                             :current
                             :text)
                           completing?]
             :suggestions suggestions))))

(defn recompute-input-text
  "Reconstructs the input text from the tagged text parts"
  [{:keys [before-cursor after-cursor completing? replace-mentions?]}]
  (letfn [(add-s [acc t s k]
            (str acc (if (= :mention t)
                       (str "@" (if replace-mentions?
                                  k
                                  s)
                            " ")
                       s)))]
    (reduce (fn [acc [t s k]]
              (add-s acc t s k))
            (str (reduce (fn [acc [t s k]]
                           (add-s acc t s k))
                         ""
                         before-cursor)
                 completing?)
            after-cursor)))

(fx/defn fetch-mentionable-users
  "Gets map of users with props to extract mention suggestions from. It is
  made with the contacts and users whose messages are currently loaded in the
  chat"
  {:events [::mention-pressed]}
  [{{:keys [current-chat-id chats]
     :contacts/keys [contacts] :as db} :db}]
  (let [messages (get-in chats [current-chat-id :messages])
        mentionable-users
        (reduce (fn [acc [key {:keys [name identicon]}]]
                  (let [alias (string/replace name ".stateofus.eth" "")]
                    (assoc acc alias {:alias alias
                                      :identicon identicon
                                      :public-key key})))
                (reduce (fn [acc [_ {:keys [alias identicon from]}]]
                          (assoc acc alias {:alias alias
                                            :identicon identicon
                                            :public-key from}))
                        (get-in db [:chat/inputs current-chat-id :users])
                        messages)
                contacts)]
    {:db (-> db
             (assoc-in [:chat/inputs current-chat-id :users]
                       mentionable-users)
             (assoc-in [:chat/inputs current-chat-id :suggestions]
                       (get-suggestions mentionable-users "")))}))

(fx/defn recompute-input
  "On selection change we want to recompute the chat inputs because
  the user may have landed the cursor in the middle of a mention, which
  is now incomplete
  For simplicity we actually do it everytime the cursor moves
  NOTE: to avoid recomputing on every selection change
  which happens every time the cursor moves, we could use a state machine
  that starts recomputing on `@` key-press
  relevant further key-presses would be `Backspace` and ` `"
  {:events [::selection-change]}
  [{{:keys [current-chat-id chats] :as db} :db} cursor]
  (let [input (-> (get-in db [:chat/inputs current-chat-id])
                  (compute-all-text-parts cursor))
        input-text (recompute-input-text input)
        input-text-empty? (if (seq input-text)
                            (-> input-text string/trim string/blank?)
                            true)]
    {:db (assoc-in db
                   [:chat/inputs current-chat-id]
                   (assoc input
                          :input-text input-text
                          :input-text-empty? input-text-empty?))}))

(fx/defn compute-completion
  "When the user selects a suggestions we complete the current mention
  with it."
  {:events [::complete-mention]}
  [{{:keys [current-chat-id] :as db} :db} alias public-key]
  (let [{:keys [cursor users before-cursor completing?] :as input}
        (get-in db [:chat/inputs current-chat-id])
        before-cursor
        (conj before-cursor
              [:mention alias (get-in users [alias :public-key])])
        new-input (-> input
                      (dissoc :completing? :suggestions)
                      (update :mentioned conj alias)
                      (assoc :completed? true
                             :before-cursor before-cursor
                             :cursor (reduce (fn [acc [t s]]
                                               (+ acc (if (= :mention t)
                                                        (+ 2 (count s))
                                                        (count s))))
                                             0
                                             before-cursor)))]
    {:db (assoc-in db
                   [:chat/inputs current-chat-id]
                   (assoc new-input :text (recompute-input-text new-input)))}))

(fx/defn set-chat-input-text
  "Set input text for current-chat. Takes db and input text and cofx
  as arguments and returns new fx. Always clear all validation messages."
  {:events [::input-text-changed]}
  [{{:keys [current-chat-id] :as db} :db} new-input]
  {:db (assoc-in db [:chat/inputs current-chat-id :text] new-input)})

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

(fx/defn chat-input-clear
  "Returns fx for focusing on active chat input reference"
  [{{:keys [current-chat-id chat-ui-props]} :db} ref]
  (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
    {::clear-rn-component cmp-ref}))

(fx/defn reply-to-message
  "Sets reference to previous chat message and focuses on input"
  [{:keys [db] :as cofx} message]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message]
                             message)}
              (chat-input-focus :input-ref))))

(fx/defn cancel-message-reply
  "Cancels stage message reply"
  [{:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    (fx/merge cofx
              {:db (assoc-in db [:chats current-chat-id :metadata :responding-to-message] nil)}
              (chat-input-focus :input-ref))))

(fx/defn plain-text-message-fx
  "when not empty, proceed by sending text message"
  [{:keys [db] :as cofx} input-text current-chat-id]
  (when-not (string/blank? input-text)
    (let [{:keys [message-id]}
          (get-in db [:chats current-chat-id :metadata :responding-to-message])
          preferred-name (get-in db [:multiaccount :preferred-name])
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
                (chat-input-clear :input-ref)
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
  {:events [::send-current-message-pressed]}
  [{{:keys [current-chat-id] :as db} :db :as cofx}]
  (let [{:keys [users] :as input} (get-in db [:chat/inputs current-chat-id])
        input-text (-> (assoc input :replace-mentions? true)
                       recompute-input-text
                       text->emoji)]
    (fx/merge
     cofx
     {:db (assoc-in db
                    [:chat/inputs current-chat-id]
                    {:users users})}
     (plain-text-message-fx input-text current-chat-id))))

(fx/defn send-transaction-result
  {:events [:chat/send-transaction-result]}
  [cofx chat-id params result])
  ;;TODO: should be implemented on status-go side
  ;;see https://github.com/status-im/team-core/blob/6c3d67d8e8bd8500abe52dab06a59e976ec942d2/rfc-001.md#status-gostatus-react-interface

;; effects

(re-frame/reg-fx
 ::focus-rn-component
 (fn [^js ref]
   (try
     (.focus ref)
     (catch :default _
       (log/debug "Cannot focus the reference")))))

(re-frame/reg-fx
 ::clear-rn-component
 (fn [ref]
   (try
     (.clear ref)
     (catch :default _
       (log/debug "Cannot clear the reference")))))
