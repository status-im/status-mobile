(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [goog.object :as object]
            [status-im.chat.constants :as const]
            [status-im.chat.models :as chat-model]
            [status-im.utils.config :as config]
            [status-im.utils.datetime :as datetime]
            [status-im.js-dependencies :as dependencies]))

(def space-char " ")

(defn text->emoji
  "Replaces emojis in a specified `text`"
  [text]
  (when text
    (str/replace text
                 #":([a-z_\-+0-9]*):"
                 (fn [[original emoji-id]]
                   (if-let [emoji-map (object/get (object/get dependencies/emojis "lib") emoji-id)]
                     (object/get emoji-map "char")
                     original)))))

(defn text-ends-with-space? [text]
  (and text (str/ends-with? text const/spacing-char)))

(defn starts-as-command?
  "Returns true if `text` may be treated as a command.
  To make sure that text is command we need to use `possible-chat-actions` function."
  [text]
  (and text (str/starts-with? text const/command-char)))

(defn split-command-args
  "Returns a list of command's arguments including the command's name.

  Examples:
  Input:  '/send Jarrad 1.0'
  Output: ['/send' 'Jarrad' '1.0']

  Input:  '/send \"Complex name with space in between\" 1.0'
  Output: ['/send' 'Complex name with space in between' '1.0']

  All the complex logic inside this function aims to support wrapped arguments."
  [command-text]
  (when command-text
    (let [space?                  (text-ends-with-space? command-text)
          command-text            (if space?
                                    (str command-text ".")
                                    command-text)
          command-text-normalized (if command-text
                                    (str/replace (str/trim command-text) #" +" " ")
                                    command-text)
          splitted                (cond-> (str/split command-text-normalized const/spacing-char)
                                    space? (drop-last))]
      (->> splitted
           (reduce (fn [[list command-started?] arg]
                     (let [quotes-count       (count (filter #(= % const/arg-wrapping-char) arg))
                           has-quote?         (and (= quotes-count 1)
                                                   (str/index-of arg const/arg-wrapping-char))
                           arg                (str/replace arg (re-pattern const/arg-wrapping-char) "")
                           new-list           (if command-started?
                                                (let [index (dec (count list))]
                                                  (update list index str const/spacing-char arg))
                                                (conj list arg))
                           command-continues? (or (and command-started? (not has-quote?))
                                                  (and (not command-started?) has-quote?))]
                       [new-list command-continues?]))
                   [[] false])
           (first)))))

(defn join-command-args
  "Transforms a list of args to a string. The opposite of `split-command-args`.

  Examples:
  Input:  ['/send' 'Jarrad' '1.0']
  Output: '/send Jarrad 1.0'

  Input:  ['/send' '\"Jarrad\"' '1.0']
  Output: '/send Jarrad 1.0'

  Input:  ['/send' 'Complex name with space in between' '1.0']
  Output: '/send \"Complex name with space in between\" 1.0'"
  [args]
  (when args
    (->> args
         (map (fn [arg]
                (let [arg (str/replace arg (re-pattern const/arg-wrapping-char) "")]
                  (if (not (str/index-of arg const/spacing-char))
                    arg
                    (str const/arg-wrapping-char arg const/arg-wrapping-char)))))
         (str/join const/spacing-char))))

(defn set-chat-input-text
  "Set input text for current-chat. Takes db and input text and cofx
  as arguments and returns new fx. Always clear all validation messages."
  [new-input {{:keys [current-chat-id] :as db} :db}]
  {:db (-> (chat-model/set-chat-ui-props db {:validation-messages nil})
           (assoc-in [:chats current-chat-id :input-text] (text->emoji new-input)))})

(defn set-chat-input-metadata
  "Sets user invisible chat input metadata for current-chat"
  [metadata {:keys [db] :as cofx}]
  (let [current-chat-id (:current-chat-id db)]
    {:db (assoc-in db [:chats current-chat-id :input-metadata] metadata)}))

(defn- start-cooldown [{:keys [db]} cooldowns]
  {:dispatch-later        [{:dispatch [:disable-cooldown]
                            :ms       (const/cooldown-periods-ms cooldowns
                                                                 const/default-cooldown-period-ms)}]
   :show-cooldown-warning nil
   :db                    (assoc db
                                 :chat/cooldowns (if (= const/cooldown-reset-threshold cooldowns)
                                                   0
                                                   cooldowns)
                                 :chat/spam-messages-frequency 0
                                 :chat/cooldown-enabled? true)})

(defn process-cooldown [{{:keys [chat/last-outgoing-message-sent-at
                                 chat/cooldowns
                                 chat/spam-messages-frequency
                                 current-chat-id] :as db} :db :as cofx}]
  (when (chat-model/public-chat? current-chat-id cofx)
    (let [spamming-fast? (< (- (datetime/timestamp) last-outgoing-message-sent-at)
                            (+ const/spam-interval-ms (* 1000 cooldowns)))
          spamming-frequently? (= const/spam-message-frequency-threshold spam-messages-frequency)]
      (cond-> {:db (assoc db
                          :chat/last-outgoing-message-sent-at (datetime/timestamp)
                          :chat/spam-messages-frequency (if spamming-fast?
                                                          (inc spam-messages-frequency)
                                                          0))}

        (and spamming-fast? spamming-frequently?)
        (start-cooldown (inc cooldowns))))))
