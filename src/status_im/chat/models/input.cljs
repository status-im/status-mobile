(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [status-im.components.react :as rc]
            [status-im.chat.constants :as const]
            [status-im.utils.phone-number :as phone-number]
            [status-im.chat.views.input.validation-messages :refer [validation-message]]
            [status-im.i18n :as i18n]
            [taoensso.timbre :as log]))

(defn content-by-message-id [db chat-id message-id]
  (get-in db [:chats chat-id]))

(defn possible-chat-actions [db chat-id]
  (let [{:keys [commands requests]} (get-in db [:chats chat-id])
        commands  (mapv (fn [[_ command]]
                          (vector command :any))
                        commands)
        responses (mapv (fn [{:keys [message-id type]}]
                          (vector
                            (get-in db [:chats chat-id :responses type])
                            message-id))
                        requests)]
    (into commands responses)))

(defn split-command-args [command-text]
  (let [splitted (str/split command-text const/spacing-char)]
    (first
      (reduce (fn [[list command-started?] arg]
                (let [quotes-count       (count (filter #(= % const/arg-wrapping-char) arg))
                      has-quote?         (and (= quotes-count 1)
                                              (str/index-of arg const/arg-wrapping-char))
                      arg                (str/replace arg #"\"" "")
                      new-list           (if command-started?
                                           (let [index (dec (count list))]
                                             (update list index str const/spacing-char arg))
                                           (conj list arg))
                      command-continues? (or (and command-started? (not has-quote?))
                                             (and (not command-started?) has-quote?))]
                  [new-list command-continues?]))
              [[] false]
              splitted))))

(defn join-command-args [args]
  (->> args
       (map (fn [arg]
              (if (not (str/index-of arg const/spacing-char))
                arg
                (str const/arg-wrapping-char arg const/arg-wrapping-char))))
       (str/join const/spacing-char)))

(defn selected-chat-command [{:keys [current-chat-id] :as db} chat-id]
  (let [chat-id          (or chat-id current-chat-id)
        input-text       (get-in db [:chats chat-id :input-text])
        input-metadata   (get-in db [:chats chat-id :input-metadata])
        possible-actions (possible-chat-actions db chat-id)
        command-args     (split-command-args input-text)
        command-name     (first command-args)]
    (when (.startsWith (or command-name "") const/command-char)
      (when-let [command (-> (filter (fn [[{:keys [name]} message-id]]
                                       (and (= name (subs command-name 1))
                                            (= message-id (or (:to-message-id input-metadata)
                                                              :any))))
                                     possible-actions)
                             (ffirst))]
        {:command       command
         :metadata      input-metadata
         :args          (remove empty? (rest command-args))}))))

(defn current-chat-argument-position
  [{:keys [args] :as command} input-text]
  (if command
    (let [current (count args)]
      (if (= (last input-text) const/spacing-char)
        current
        (dec current)))
    -1))

(defn argument-position [{:keys [current-chat-id] :as db} chat-id]
  (let [chat-id          (or chat-id current-chat-id)
        input-text       (get-in db [:chats chat-id :input-text])
        chat-command     (selected-chat-command db chat-id)]
    (current-chat-argument-position chat-command input-text)))

(defn command-complete?
  ([{:keys [current-chat-id] :as db} chat-id]
   (let [chat-id             (or chat-id current-chat-id)
         input-text          (get-in db [:chats chat-id :input-text])
         chat-command        (selected-chat-command db chat-id)]
     (command-complete? chat-command)))
  ([{:keys [args] :as chat-command}]
   (let [params          (get-in chat-command [:command :params])
         required-params (remove :optional params)]
     (and chat-command
          (or (= (count args) (count params))
              (= (count args) (count required-params)))))))

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