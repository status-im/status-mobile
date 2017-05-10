(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [status-im.components.react :as rc]
            [status-im.chat.constants :as const]
            [status-im.chat.views.input.validation-messages :refer [validation-message]]
            [status-im.i18n :as i18n]
            [status-im.utils.phone-number :as phone-number]
            [taoensso.timbre :as log]
            [status-im.chat.utils :as chat-utils]
            [status-im.bots.constants :as bots-constants]))

(def emojis (js/require "emojilib"))

(defn text->emoji [text]
  (when text
    (str/replace text
                 #":([a-z_\-+0-9]*):"
                 (fn [[original emoji-id]]
                   (if-let [emoji-map (aget emojis "lib" emoji-id)]
                     (aget emoji-map "char")
                     original)))))

(defn text-ends-with-space? [text]
  (when text
    (= (str/last-index-of text const/spacing-char)
       (dec (count text)))))

(defn possible-chat-actions [{:keys [global-commands] :as db} chat-id]
  (let [{:keys [contacts requests]} (get-in db [:chats chat-id])]
    (->> contacts
         (map (fn [{:keys [identity]}]
                (let [{:keys [commands responses]} (get-in db [:contacts identity])]
                  (let [commands'  (mapv (fn [[k v]] [k [v :any]]) (merge global-commands commands))
                        responses' (mapv (fn [{:keys [message-id type]}]
                                           [type [(get responses type) message-id]])
                                         requests)]
                    (into commands' responses')))))
         (reduce (fn [m cur] (into (or m {}) cur)))
         (into {})
         (vals))))

(defn split-command-args [command-text]
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
         (first))))

(defn join-command-args [args]
  (->> args
       (map (fn [arg]
              (if (not (str/index-of arg const/spacing-char))
                arg
                (str const/arg-wrapping-char arg const/arg-wrapping-char))))
       (str/join const/spacing-char)))

(defn selected-chat-command
  ([{:keys [current-chat-id] :as db} chat-id input-text]
   (let [chat-id          (or chat-id current-chat-id)
         input-metadata   (get-in db [:chats chat-id :input-metadata])
         seq-arguments    (get-in db [:chats chat-id :seq-arguments])
         possible-actions (possible-chat-actions db chat-id)
         command-args     (split-command-args input-text)
         command-name     (first command-args)]
     (when (chat-utils/starts-as-command? (or command-name ""))
       (when-let [[command to-message-id]
                  (-> (filter (fn [[{:keys [name bot]} message-id]]
                                (= (or (when-not (bots-constants/mailman-bot? bot) bot) name)
                                   (subs command-name 1)))
                              possible-actions)
                      (first))]
         {:command  command
          :metadata (if (not= :any to-message-id)
                      (assoc input-metadata :to-message-id to-message-id)
                      input-metadata)
          :args     (if (empty? seq-arguments)
                      (rest command-args)
                      seq-arguments)}))))
  ([{:keys [current-chat-id] :as db} chat-id]
   (selected-chat-command db chat-id (get-in db [:chats chat-id :input-text]))))

(def *no-argument-error* -1)

(defn current-chat-argument-position
  [{:keys [args] :as command} input-text selection seq-arguments]
  (if command
    (if (get-in command [:command :sequential-params])
      (count seq-arguments)
      (let [subs-input-text (subs input-text 0 selection)]
        (if subs-input-text
          (let [args               (split-command-args subs-input-text)
                argument-index     (dec (count args))
                ends-with-space?   (text-ends-with-space? subs-input-text)
                arg-wrapping-count (-> (frequencies subs-input-text)
                                       (get const/arg-wrapping-char)
                                       (or 0))]
            (if (and ends-with-space?
                     (even? arg-wrapping-count))
              argument-index
              (dec argument-index)))
          *no-argument-error*)))
    *no-argument-error*))

(defn argument-position [{:keys [current-chat-id] :as db} chat-id]
  (let [chat-id       (or chat-id current-chat-id)
        input-text    (get-in db [:chats chat-id :input-text])
        seq-arguments (get-in db [:chats chat-id :seq-arguments])
        selection     (get-in db [:chat-ui-props chat-id :selection])
        chat-command  (selected-chat-command db chat-id)]
    (current-chat-argument-position chat-command input-text selection seq-arguments)))

(defn command-completion
  ([{:keys [current-chat-id] :as db} chat-id]
   (let [chat-id      (or chat-id current-chat-id)
         input-text   (get-in db [:chats chat-id :input-text])
         chat-command (selected-chat-command db chat-id)]
     (command-completion chat-command)))
  ([{:keys [args] :as chat-command}]
   (let [args            (remove str/blank? args)
         params          (get-in chat-command [:command :params])
         required-params (remove :optional params)]
     (if chat-command
       (cond
         (or (= (count args) (count params))
             (= (count args) (count required-params)))
         :complete

         (< (count args) (count required-params))
         :less-than-needed

         (> (count args) (count params))
         :more-than-needed

         :default
         :no-command)
       :no-command))))

(defn args->params [{:keys [command args]}]
  (let [params (:params command)]
    (->> args
         (map-indexed (fn [i value]
                        (vector (get-in params [i :name]) value)))
         (into {}))))

(defn command-dependent-context-params
  [{:keys [name] :as command}]
  (case name
    "phone" {:suggestions (phone-number/get-examples)}
    {}))

(defmulti validation-handler (fn [name] (keyword name)))

(defmethod validation-handler :phone
  [_]
  (fn [[number] set-errors proceed]
    (if (phone-number/valid-mobile-number? number)
      (proceed)
      (set-errors [validation-message
                   {:title       (i18n/label :t/phone-number)
                    :description (i18n/label :t/invalid-phone)}]))))

(defn- changed-arg-position [xs ys]
  (let [longest  (into [] (max-key count xs ys))
        shortest (into [] (if (= longest xs) ys xs))]
    (->> longest
         (map-indexed (fn [index x]
                        (if (and (> (count shortest) index)
                                 (= (count x) (count (get shortest index))))
                          nil
                          index)))
         (remove nil?)
         (first))))

(defn make-input-text [[command & args] old-args]
  (let [args     (into [] args)
        old-args (into [] old-args)

        arg-pos  (changed-arg-position args old-args)
        new-arg  (get args arg-pos)
        new-args (if arg-pos
                   (assoc old-args arg-pos (when new-arg
                                             (str/trim new-arg)))
                   old-args)]
    (str
      command
      const/spacing-char
      (join-command-args new-args))))