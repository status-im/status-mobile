(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [status-im.components.react :as rc]
            [status-im.components.status :as status]
            [status-im.chat.constants :as const]
            [status-im.chat.views.input.validation-messages :refer [validation-message]]
            [status-im.i18n :as i18n]
            [status-im.utils.phone-number :as phone-number]
            [status-im.chat.utils :as chat-utils]
            [status-im.bots.constants :as bots-constants]
            [status-im.js-dependencies :as dependencies]
            [taoensso.timbre :as log]))

(defn text->emoji
  "Replaces emojis in a specified `text`"
  [text]
  (when text
    (str/replace text
                 #":([a-z_\-+0-9]*):"
                 (fn [[original emoji-id]]
                   (if-let [emoji-map (aget dependencies/emojis "lib" emoji-id)]
                     (aget emoji-map "char")
                     original)))))

(defn text-ends-with-space? [text]
  (and (not (nil? text))
       (str/ends-with? text const/spacing-char)))

(defn starts-as-command?
  "Returns true if `text` may be treated as a command.
  To make sure that text is command we need to use `possible-chat-actions` function."
  [text]
  (and (not (nil? text))
       (or (str/starts-with? text const/bot-char)
           (str/starts-with? text const/command-char))))

(defn possible-chat-actions
  "Returns a map of possible chat actions (commands and response) for a specified `chat-id`.
  Every map's key is a command's name, value is a pair of [`command` `message-id`]. In the case
  of commands `message-id` is `:any`, for responses value contains the actual id.

  Example of output:
  {:browse  [{:description \"Launch the browser\" :name \"browse\" ...} :any]
   :request [{:description \"Request a payment\" :name \"request\" ...} \"message-id\"]}"
  [{:keys [global-commands current-chat-id] :as db} chat-id]
  (let [chat-id (or chat-id current-chat-id)
        {:keys [contacts requests]} (get-in db [:chats chat-id])]
    (->> contacts
         (map (fn [{:keys [identity]}]
                (let [{:keys [commands responses]} (get-in db [:contacts/contacts identity])]
                  (let [commands'  (mapv (fn [[k v]] [k [v :any]]) (merge global-commands commands))
                        responses' (mapv (fn [{:keys [message-id type]}]
                                           (when-let [response (get responses type)]
                                             [type [response message-id]]))
                                         requests)]
                    (into commands' responses')))))
         (reduce (fn [m cur] (into (or m {}) cur)))
         (into {}))))

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

(defn selected-chat-command
  "Returns a map containing `:command`, `:metadata` and `:args` keys.
  Can also return `nil` if there is no selected command.

  * `:command` key contains a map with all information about command.
  * `:metadata` is also a map which contains some additional information, usually not visible by user.
  For instance, we can add a `:to-message-id` key to this map, and this key will allow us to identity
  the request we're responding to.
  * `:args` contains all arguments provided by user."
  ([{:keys [current-chat-id] :as db} chat-id input-text]
   (let [chat-id          (or chat-id current-chat-id)
         input-metadata   (get-in db [:chats chat-id :input-metadata])
         seq-arguments    (get-in db [:chats chat-id :seq-arguments])
         possible-actions (possible-chat-actions db chat-id)
         command-args     (split-command-args input-text)
         command-name     (first command-args)]
     (when (starts-as-command? (or command-name ""))
       (when-let [[command to-message-id]
                  (-> (filter (fn [[{:keys [name bot]} message-id]]
                                (= (or (when-not (bots-constants/mailman-bot? bot) bot) name)
                                   (subs command-name 1)))
                              (vals possible-actions))
                      (first))]
         {:command  command
          :metadata (if (and (nil? (:to-message-id input-metadata)) (not= :any to-message-id))
                      (assoc input-metadata :to-message-id to-message-id)
                      input-metadata)
          :args     (if (empty? seq-arguments)
                      (rest command-args)
                      seq-arguments)}))))
  ([{:keys [current-chat-id] :as db} chat-id]
   (selected-chat-command db chat-id (get-in db [:chats chat-id :input-text]))))

(def *no-argument-error* -1)

(defn current-chat-argument-position
  "Returns the position of current argument. It's just an integer number from -1 to infinity.
  -1 (`*no-argument-error*`) means error. It can happen if there is no selected command or selection."
  [command input-text selection seq-arguments]
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

(defn argument-position
  "Returns the position of current argument. It's just an integer from -1 to infinity.
  -1 (`*no-argument-error*`) means error. It can happen if there is no command or selection.

  This method is basically just another way of calling `current-chat-argument-position`."
  [{:keys [current-chat-id] :as db} chat-id]
  (let [chat-id       (or chat-id current-chat-id)
        input-text    (get-in db [:chats chat-id :input-text])
        seq-arguments (get-in db [:chats chat-id :seq-arguments])
        selection     (get-in db [:chat-ui-props chat-id :selection])
        chat-command  (selected-chat-command db chat-id)]
    (current-chat-argument-position chat-command input-text selection seq-arguments)))

(defn command-completion
  "Returns one of the following values indicating a command's completion status:
   * `:complete` means that the command is complete and can be sent;
   * `:less-than-needed` means that the command is not complete and additional arguments should be provided;
   * `:more-than-needed` means that the command is more than complete and contains redundant arguments;
   * `:no-command` means that there is no selected command."
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

(defn args->params
  "Uses `args` (which is a list or vector like ['Jarrad' '1.0']) and command's `params`
  and returns a map that looks the following way:
  {:recipient \"Jarrad\" :amount \"1.0\"}"
  [{:keys [command args]}]
  (let [params (:params command)]
    (->> args
         (map-indexed (fn [i value]
                        [(keyword (get-in params [i :name])) value]))
         (remove #(nil? (first %)))
         (into {}))))

(defn command-dependent-context-params
  "Returns additional `:context` data that will be added to specific commands.
  The following data shouldn't be hardcoded here."
  [chat-id {:keys [name] :as command}]
  (case chat-id
    "console" (case name
                "phone" {:suggestions (phone-number/get-examples)}
                {})
    {}))

(defn modified-db-after-change
  "Returns the new db object that should be used after any input change."
  [{:keys [current-chat-id] :as db}]
  (let [input-text   (get-in db [:chats current-chat-id :input-text])
        command      (selected-chat-command db current-chat-id input-text)
        prev-command (get-in db [:chat-ui-props current-chat-id :prev-command])]
    (if command
      (cond-> db
        ;; clear the bot db
        (not= prev-command (-> command :command :name))
        (assoc-in [:bot-db (or (:bot command) current-chat-id)] nil)
        ;; clear the chat's validation messages
        true
        (assoc-in [:chat-ui-props current-chat-id :validation-messages] nil))
      (-> db
          ;; clear input metadata
          (assoc-in [:chats current-chat-id :input-metadata] nil)
          ;; clear
          (update-in [:chat-ui-props current-chat-id]
                     merge
                     {:result-box          nil
                      :validation-messages nil
                      :prev-command        (-> command :command :name)})))))

(defmulti validation-handler (fn [name] (keyword name)))

(defmethod validation-handler :phone
  [_]
  (fn [[number] error-events-creator]
    (when-not (phone-number/valid-mobile-number? number)
      (error-events-creator [validation-message
                             {:title       (i18n/label :t/phone-number)
                              :description (i18n/label :t/invalid-phone)}]))))
