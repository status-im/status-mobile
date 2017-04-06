(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [status-im.components.react :as rc]
            [status-im.chat.constants :as const]
            [status-im.chat.models.password-input :as password-input]
            [status-im.chat.views.input.validation-messages :refer [validation-message]]
            [status-im.i18n :as i18n]
            [status-im.utils.phone-number :as phone-number]
            [taoensso.timbre :as log]))

(defn text-ends-with-space? [text]
  (= (str/last-index-of text const/spacing-char)
     (dec (count text))))

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

(defn selected-chat-command
  ([{:keys [current-chat-id] :as db} chat-id input-text]
   (let [chat-id          (or chat-id current-chat-id)
         input-metadata   (get-in db [:chats chat-id :input-metadata])
         possible-actions (possible-chat-actions db chat-id)
         command-args     (split-command-args input-text)
         command-name     (first command-args)]
     (when (.startsWith (or command-name "") const/command-char)
       (when-let [[command to-message-id] (-> (filter (fn [[{:keys [name]} message-id]]
                                                        (= name (subs command-name 1)))
                                                      possible-actions)
                                              (first))]
         {:command  command
          :metadata (if (not= :any to-message-id)
                      (assoc input-metadata :to-message-id to-message-id)
                      input-metadata)
          :args     (remove empty? (rest command-args))}))))
  ([{:keys [current-chat-id] :as db} chat-id]
    (selected-chat-command db chat-id (get-in db [:chats chat-id :input-text]))))

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

(defn command-completion
  ([{:keys [current-chat-id] :as db} chat-id]
   (let [chat-id             (or chat-id current-chat-id)
         input-text          (get-in db [:chats chat-id :input-text])
         chat-command        (selected-chat-command db chat-id)]
     (command-completion chat-command)))
  ([{:keys [args] :as chat-command}]
   (let [params          (get-in chat-command [:command :params])
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

(def text-modifiers
  [password-input/modifier])

(defn add-modifiers [params new-args]
  (->> new-args
       (map-indexed (fn [i arg]
                      {:position i
                       :value    arg
                       :modifier (-> (filter
                                       (fn [mod]
                                         ((:execute-when mod) (get params i)))
                                       text-modifiers)
                                     (first))}))
       (into [])))

(defn apply-modifiers [text-splitted args]
  (if-let [{:keys [position modifier]} (first args)]
    (if modifier
      (let [{:keys [get-modified-text]} modifier
            modified-text (get-modified-text text-splitted position)]
        (apply-modifiers modified-text (rest args)))
      (apply-modifiers text-splitted (rest args)))
    (str/join const/spacing-char text-splitted)))

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

(defn make-input-text [modifiers [command & args] old-args selection]
  (let [arg-pos  (changed-arg-position args old-args)
        modifier (get-in modifiers [arg-pos :modifier])
        new-arg  (if (and arg-pos modifier)
                   (let [{:keys [make-change]} modifier]
                     (make-change {:command-name (subs command 1)
                                   :old-args     old-args
                                   :new-args     args
                                   :arg-pos      arg-pos
                                   :selection    selection}))
                   (get (into [] args) arg-pos))
        new-args (if arg-pos
                   (assoc (into [] old-args) arg-pos (when new-arg (str/trim new-arg)))
                   old-args)]
    (str
      command
      const/spacing-char
      (str/join const/spacing-char new-args))))