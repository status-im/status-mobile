(ns status-im.chat.models.input
  (:require [clojure.string :as str]
            [status-im.ui.components.react :as rc]
            [status-im.native-module.core :as status]
            [status-im.chat.constants :as const]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.views.input.validation-messages :refer [validation-message]]
            [status-im.chat.utils :as chat-utils]
            [status-im.i18n :as i18n]
            [status-im.utils.phone-number :as phone-number]
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

(defn selected-chat-command
  "Takes whole db, or current chat and available commands/responses and returns a map
  containing `:command`, `:metadata` and `:args` keys. Can also return `nil` if there is no selected command.

  * `:command` key contains a map with all information about command.
  * `:metadata` is also a map which contains some additional information, usually not visible by user.
  For instance, we can add a `:to-message-id` key to this map, and this key will allow us to identity
  the request we're responding to.
  * `:args` contains all arguments provided by user."
  ([{:keys [input-text input-metadata seq-arguments] :as current-chat} commands responses]
   (let [[command-name :as command-args] (split-command-args input-text)]
     (when-let [{{:keys [message-id]} :request :as command} (and command-name
                                                                 (starts-as-command? command-name)
                                                                 (get (merge commands
                                                                             responses)
                                                                      (subs command-name 1)))]
       {:command  command
        :metadata (if (and message-id (not (:to-message-id input-metadata)))
                    (assoc input-metadata :to-message-id message-id)
                    input-metadata)
        :args     (into [] (if (empty? seq-arguments)
                             (rest command-args)
                             seq-arguments))})))
  ([{:keys [chats current-chat-id access-scope->commands-responses]
     :contacts/keys [contacts]
     :accounts/keys [accounts current-account-id] :as db}]
   (let [chat (get chats current-chat-id)
         account (get accounts current-account-id)]
     (selected-chat-command chat
                            (commands-model/commands-responses :command
                                                               access-scope->commands-responses
                                                               account
                                                               chat
                                                               contacts)
                            (commands-model/requested-responses access-scope->commands-responses
                                                                account
                                                                chat
                                                                contacts
                                                                (vals (:requests chat)))))))

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
  [{:keys [current-chat-id] :as db}]
  (let [input-text    (get-in db [:chats current-chat-id :input-text])
        seq-arguments (get-in db [:chats current-chat-id :seq-arguments])
        selection     (get-in db [:chat-ui-props current-chat-id :selection])
        chat-command  (selected-chat-command db)]
    (current-chat-argument-position chat-command input-text selection seq-arguments)))

(defn command-completion
  "Returns one of the following values indicating a command's completion status:
   * `:complete` means that the command is complete and can be sent;
   * `:less-than-needed` means that the command is not complete and additional arguments should be provided;
   * `:more-than-needed` means that the command is more than complete and contains redundant arguments;
   * `:no-command` means that there is no selected command."
  [{:keys [args] :as chat-command}]
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
      :no-command)))

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
  (let [command      (selected-chat-command db)
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
