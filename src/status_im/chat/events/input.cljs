(ns status-im.chat.events.input
  (:require [clojure.string :as str]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as const]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.models :as model]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.suggestions :as suggestions-model]
            [status-im.chat.events.commands :as commands-events]
            [status-im.chat.events.animation :as animation-events]
            [status-im.bots.events :as bots-events]
            [status-im.components.react :as react-comp]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]
            [status-im.i18n :as i18n]))

;;;; Effects

(re-frame/reg-fx
  ::focus-rn-component
  (fn [ref]
    (try
      (.focus ref)
      (catch :default e
        (log/debug "Cannot focus the reference")))))

(re-frame/reg-fx
  ::blur-rn-component
  (fn [ref]
    (try
      (.blur ref)
      (catch :default e
        (log/debug "Cannot blur the reference")))))

(re-frame/reg-fx
  ::dismiss-keyboard
  (fn [_]
    (react-comp/dismiss-keyboard!)))

(re-frame/reg-fx
  ::set-native-props
  (fn [{:keys [ref props]}]
    (.setNativeProps ref (clj->js props))))

;;;; Helper functions

(defn update-suggestions
  "Update suggestions for current chat input, takes db as the only argument
  and returns map with keys :db (new db with up-to-date suggestions) and (optionally)
  :call-jail-function with jail function call params, if request to jail needs
  to be made as a result of suggestions update."
  [{:keys [chats current-chat-id current-account-id local-storage] :as db}]
  (let [chat-text       (str/trim (or (get-in chats [current-chat-id :input-text]) ""))
        requests        (->> (suggestions-model/get-request-suggestions db chat-text)
                             (remove (fn [{:keys [type]}]
                                       (= type :grant-permissions))))
        commands        (suggestions-model/get-command-suggestions db chat-text)
        global-commands (suggestions-model/get-global-command-suggestions db chat-text)
        all-commands    (->> (into global-commands commands)
                             (remove (fn [[k {:keys [hidden?]}]] hidden?))
                             (into {}))
        {:keys [dapp?]} (get-in db [:contacts/contacts current-chat-id])
        new-db          (cond-> db
                          true (assoc-in [:chats current-chat-id :request-suggestions] requests)
                          true (assoc-in [:chats current-chat-id :command-suggestions] all-commands)
                          (and dapp?
                               (str/blank? chat-text))
                          (assoc-in [:chats current-chat-id :parameter-boxes :message] nil))]
    (cond-> {:db new-db}
      (and dapp?
           (not (str/blank? chat-text))
           (every? empty? [requests commands]))
      (assoc :call-jail-function {:chat-id    current-chat-id
                                  :function   :on-message-input-change
                                  :parameters {:message chat-text}
                                  :context    {:data (get local-storage current-chat-id)
                                               :from current-account-id}}))))

(defn set-chat-input-text
  "Set input text for current-chat and updates suggestions relevant to current input.
  Takes db, input text and `:append?` flag as arguments and returns new db.
  When `:append?` is false or not provided, resets the current chat input with input text,
  otherwise input text is appended to the current chat input."
  [{:keys [current-chat-id] :as db} new-input & {:keys [append?]}]
  (let [current-input (get-in db [:chats current-chat-id :input-text])]
    (assoc-in db
              [:chats current-chat-id :input-text]
              (input-model/text->emoji (if append?
                                         (str current-input new-input)
                                         new-input)))))

(defn set-chat-input-metadata
  "Set input metadata for active chat. Takes db and metadata and returns updated db."
  [{:keys [current-chat-id] :as db} metadata]
  (assoc-in db [:chats current-chat-id :input-metadata] metadata))

(defn set-chat-seq-arg-input-text
  "Sets input text for current sequential argument in active chat"
  [{:keys [current-chat-id] :as db} text]
  (assoc-in db [:chats current-chat-id :seq-argument-input-text] text))

(defn clear-seq-arguments
  "Clears sequential arguments for current-chat"
  [{:keys [current-chat-id chats] :as db}]
  (-> db
      (assoc-in [:chats current-chat-id :seq-arguments] [])
      (assoc-in [:chats current-chat-id :seq-argument-input-text] nil)))

(defn set-command-argument
  "Sets command argument in active chat"
  [{:keys [current-chat-id] :as db} index arg move-to-next?]
  (let [command     (-> (get-in db [:chats current-chat-id :input-text])
                        (input-model/split-command-args))
        seq-params? (-> (input-model/selected-chat-command db current-chat-id)
                        (get-in [:command :sequential-params]))]
    (if seq-params?
      {:db (set-chat-seq-arg-input-text db arg)}
      (let [arg          (str/replace arg (re-pattern const/arg-wrapping-char) "")
            command-name (first command)
            command-args (into [] (rest command))
            command-args (if (< index (count command-args))
                           (assoc command-args index arg)
                           (conj command-args arg))
            input-text   (str command-name
                              const/spacing-char
                              (input-model/join-command-args command-args)
                              (when (and move-to-next?
                                         (= index (dec (count command-args))))
                                const/spacing-char))]
        (-> db
            (set-chat-input-text input-text)
            update-suggestions)))))

(defn load-chat-parameter-box
  "Returns fx for loading chat parameter box for active chat"
  [{:keys [current-chat-id bot-db] :accounts/keys [current-account-id] :as db}
   {:keys [name type bot owner-id] :as command}]
  (let [parameter-index (input-model/argument-position db)]
    (when (and command (> parameter-index -1))
      (let [data    (get-in db [:local-storage current-chat-id])
            bot-db  (get bot-db (or bot current-chat-id))
            path    [(if (= :command type) :commands :responses)
                     name
                     :params
                     parameter-index
                     :suggestions]
            args    (-> (get-in db [:chats current-chat-id :input-text])
                        input-model/split-command-args
                        rest)
            seq-arg (get-in db [:chats current-chat-id :seq-argument-input-text])
            to      (get-in db [:contacts/contacts current-chat-id :address])
            params  {:parameters {:args    args
                                  :bot-db  bot-db
                                  :seq-arg seq-arg}
                     :context    (merge {:data data
                                         :from current-account-id
                                         :to   to}
                                        (input-model/command-dependent-context-params current-chat-id command))}]
        {:call-jail {:jail-id (or bot owner-id current-chat-id)
                     :path    path
                     :params  params
                     :callback-events-creator (fn [jail-response]
                                                [[:received-bot-response
                                                  {:chat-id         current-chat-id
                                                   :command         command
                                                   :parameter-index parameter-index}
                                                  jail-response]])}}))))

(defn chat-input-focus
  "Returns fx for focusing on active chat input reference"
  [{:keys [current-chat-id chat-ui-props]} ref]
  (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
    {::focus-rn-component cmp-ref}))

(defn update-text-selection
  "Updates text selection in active chat input"
  [{:keys [current-chat-id] :as db} selection]
  (let [input-text            (get-in db [:chats current-chat-id :input-text])
        command               (input-model/selected-chat-command db current-chat-id input-text)
        new-db                (model/set-chat-ui-props db {:selection selection})
        chat-parameter-box-fx (load-chat-parameter-box new-db (:command command))]
    (cond-> {:db new-db}

      chat-parameter-box-fx
      (merge chat-parameter-box-fx)

      (and (= selection (+ (count const/command-char)
                           (count (get-in command [:command :name]))
                           (count const/spacing-char)))
           (get-in command [:command :sequential-params]))
      (merge (chat-input-focus new-db :seq-input-ref)))))

(defn select-chat-input-command
  "Selects command + (optional) arguments as input for active chat"
  [{:keys [current-chat-id chat-ui-props] :as db}
   {:keys [prefill prefill-bot-db sequential-params name] :as command} metadata prevent-auto-focus?]
  (let [fx (-> db
               bots-events/clear-bot-db
               clear-seq-arguments
               (model/set-chat-ui-props {:show-suggestions?   false
                                         :show-emoji?         false
                                         :result-box          nil
                                         :validation-messages nil
                                         :prev-command        name})
               (set-chat-input-metadata metadata)
               (set-chat-input-text (str (chat-utils/command-name command)
                                         const/spacing-char
                                         (when-not sequential-params
                                           (input-model/join-command-args prefill))))
               update-suggestions
               (as-> fx'
                   (merge fx' (load-chat-parameter-box (:db fx') command))))]
    (cond-> fx
      prefill-bot-db (update :db bots-events/update-bot-db {:db prefill-bot-db})

      (not (and sequential-params
                prevent-auto-focus?))
      (merge (chat-input-focus (:db fx) :input-ref))

      sequential-params
      (as-> fx'
          (cond-> (update fx' :db
                          set-chat-seq-arg-input-text
                          (str/join const/spacing-char prefill))
            (not prevent-auto-focus?)
            (merge fx' (chat-input-focus (:db fx') :seq-input-ref)))))))

(defn set-contact-as-command-argument
  "Sets contact as command argument for active chat"
  [db {:keys [bot-db-key contact arg-index]}]
  (let [name    (str/replace (:name contact) (re-pattern const/arg-wrapping-char) "")
        contact (select-keys contact [:address :whisper-identity :name :photo-path :dapp?])]
    (-> (set-command-argument db arg-index name true)
        (as-> fx
            (merge fx (bots-events/set-in-bot-db
                       (:db fx)
                       {:path [:public (keyword bot-db-key)]
                        :value contact})))
        (as-> fx
            (let [{:keys [current-chat-id]
                   :as new-db}             (:db fx)
                  arg-position             (input-model/argument-position new-db)
                  input-text               (get-in new-db [:chats current-chat-id :input-text])
                  command-args             (cond-> (input-model/split-command-args input-text)
                                             (input-model/text-ends-with-space? input-text) (conj ""))
                  new-selection            (->> command-args
                                                (take (+ 3 arg-position))
                                                (input-model/join-command-args)
                                                count
                                                (min (count input-text)))]
              (merge fx (update-text-selection new-db new-selection)))))))

(defn- request-command-data
  "Requests command data from jail"
  [{:keys [bot-db] :contacts/keys [contacts] :as db}
   {{:keys [command
            metadata
            args]
     :as   content} :content
    :keys  [chat-id jail-id data-type event-after-creator message-id current-time]}]
  (let [{:keys [dapp? dapp-url name]} (get contacts chat-id)
        metadata        (merge metadata
                               (when dapp?
                                 {:url  (i18n/get-contact-translated chat-id :dapp-url dapp-url)
                                  :name (i18n/get-contact-translated chat-id :name name)}))
        owner-id        (:owner-id command)
        bot-db          (get bot-db chat-id)
        params          (merge (input-model/args->params content)
                               {:bot-db   bot-db
                                :metadata metadata})

        command-message {:command    command
                         :params     params
                         :to-message (:to-message-id metadata)
                         :created-at current-time
                         :id         message-id
                         :chat-id    chat-id
                         :jail-id    (or owner-id jail-id)}

        request-data    {:message-id   message-id
                         :chat-id      chat-id
                         :jail-id      (or owner-id jail-id)
                         :content      {:command (:name command)
                                        :params  params
                                        :type    (:type command)}
                         :on-requested (fn [jail-response]
                                         (event-after-creator command-message jail-response))}]
    (commands-events/request-command-message-data db request-data data-type)))

(defn proceed-command
  "Proceed with command processing by setting up execution chain of events:

   1. Command validation
   2. Checking if command defined `onSend`

  Second check is important because commands defining `onSend` are actually not
  'sendable' and can't be treated as normal command messages, instead of actually
  sending them, returned markup is displayed in the chat result box and command processing
  ends there.
  If it's normal command instead (determined by nil response to `:on-send` jail request),
  processing continues by requesting command preview before actually sending the command
  message."
  [{:keys [current-chat-id] :as db} {{:keys [bot]} :command :as content} message-id current-time]
  (let [params-template          {:content      content
                                  :chat-id      current-chat-id
                                  :jail-id      (or bot current-chat-id)
                                  :message-id   message-id
                                  :current-time current-time}
        on-send-params           (merge params-template
                                        {:data-type           :on-send
                                         :event-after-creator (fn [_ jail-response]
                                                                [::check-command-type
                                                                 jail-response
                                                                 params-template])})
        after-validation-events  [[::request-command-data on-send-params]]
        validation-params        (merge params-template
                                        {:data-type           :validator
                                         :event-after-creator (fn [_ jail-response]
                                                                [::proceed-validation
                                                                 jail-response
                                                                 after-validation-events])})]
    (request-command-data db validation-params)))

;;;; Handlers

(handlers/register-handler-db
  :update-input-data
  (fn [db]
    (input-model/modified-db-after-change db)))

(handlers/register-handler-fx
  :set-chat-input-text
  [re-frame/trim-v]
  (fn [{:keys [db]} [text]]
    (-> db
        (set-chat-input-text text)
        update-suggestions)))

(handlers/register-handler-fx
  :add-to-chat-input-text
  [re-frame/trim-v]
  (fn [{:keys [db]} [text-to-add]]
    (-> db
        (set-chat-input-text text-to-add :append? true)
        update-suggestions)))

(handlers/register-handler-fx
  :select-chat-input-command
  [re-frame/trim-v]
  (fn [{:keys [db]} [command metadata prevent-auto-focus?]]
    (select-chat-input-command db command metadata prevent-auto-focus?)))

(handlers/register-handler-db
  :set-chat-input-metadata
  [re-frame/trim-v]
  (fn [db [data]]
    (set-chat-input-metadata db data)))

(handlers/register-handler-fx
  :set-command-argument
  [re-frame/trim-v]
  (fn [{:keys [db]} [[index arg move-to-next?]]]
    (set-command-argument db index arg move-to-next?)))

(handlers/register-handler-fx
  :chat-input-focus
  [re-frame/trim-v]
  (fn [{:keys [db]} [ref]]
    (chat-input-focus db ref)))

(handlers/register-handler-fx
  :chat-input-blur
  [re-frame/trim-v]
  (fn [{{:keys [current-chat-id chat-ui-props]} :db} [ref]]
    (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
      {::blur-rn-component cmp-ref})))

(handlers/register-handler-fx
  :update-suggestions
  (fn [{:keys [db]} _]
    (update-suggestions db)))

(handlers/register-handler-fx
  :load-chat-parameter-box
  [re-frame/trim-v]
  (fn [{:keys [db]} [command]]
    (load-chat-parameter-box db command)))

(handlers/register-handler-fx
  :proceed-command
  [re-frame/trim-v (re-frame/inject-cofx :random-id) (re-frame/inject-cofx :now)]
  (fn [{:keys [db random-id now]} [content]]
    (proceed-command db content random-id now)))

(handlers/register-handler-fx
  ::proceed-validation
  [re-frame/trim-v]
  (fn [_ [{:keys [markup validationHandler parameters]} proceed-events]]
    (let [error-events-creator (fn [validator-result]
                                 [[:set-chat-ui-props {:validation-messages  validator-result
                                                       :sending-in-progress? false}]])
          events (cond
                   markup
                   (error-events-creator markup)

                   validationHandler
                   [[::execute-validation-handler
                     validationHandler parameters error-events-creator proceed-events]
                    [:set-chat-ui-props {:sending-in-progress? false}]]

                   :default
                   proceed-events)]
      {:dispatch-n events})))

(handlers/register-handler-fx
  ::execute-validation-handler
  [re-frame/trim-v]
  (fn [_ [validation-handler-name params error-events-creator proceed-events]]
    (let [error-events (when-let [validator (input-model/validation-handler validation-handler-name)]
                         (validator params error-events-creator))]
      {:dispatch-n (or error-events proceed-events)})))

(handlers/register-handler-fx
  ::request-command-data
  [re-frame/trim-v]
  (fn [{:keys [db]} [command-params]]
    (request-command-data db command-params)))

(handlers/register-handler-fx
  ::send-command
  [re-frame/trim-v]
  (fn [{{:keys [current-public-key current-chat-id]
         :accounts/keys [current-account-id] :as db} :db} [{:keys [command] :as command-message}]]
    (-> db
        clear-seq-arguments
        (set-chat-input-metadata nil)
        (set-chat-input-text nil)
        (model/set-chat-ui-props {:sending-in-progress? false})
        update-suggestions
        ;; TODO: refactor send-message.cljs to use atomic pure handlers and get rid of this dispatch
        (assoc :dispatch [:check-commands-handlers! {:message (get-in db [:chats current-chat-id :input-text])
                                                     :command  command-message
                                                     :chat-id  current-chat-id
                                                     :identity current-public-key
                                                     :address  current-account-id}]))))

(handlers/register-handler-fx
  ::check-command-type
  [re-frame/trim-v]
  (fn [{{:keys [current-chat-id] :as db} :db} [on-send-jail-response params-template]]
    (if on-send-jail-response
      ;; `onSend` is defined, we have non-sendable command here, like `@browse`
      {:db (-> db
               (model/set-chat-ui-props {:result-box           on-send-jail-response
                                         :sending-in-progress? false})
               (animation-events/choose-predefined-expandable-height :result-box :max))
       ::dismiss-keyboard nil}
      ;; regular command message, we need to fetch preview before sending the command message
      (request-command-data db (merge params-template
                                      {:data-type           :preview
                                       :event-after-creator (fn [command-message _]
                                                              [::send-command command-message])})))))

(handlers/register-handler-fx
  :send-current-message
  [(re-frame/inject-cofx :random-id) (re-frame/inject-cofx :now)]
  (fn [{{:keys [current-chat-id current-public-key] :as db} :db message-id :random-id current-time :now} _]
    (let [input-text   (get-in db [:chats current-chat-id :input-text])
          chat-command (-> db
                           (input-model/selected-chat-command current-chat-id input-text)
                           (as-> selected-command
                               (if (get-in selected-command [:command :sequential-params])
                                 (assoc selected-command :args
                                        (get-in db [:chats current-chat-id :seq-arguments]))
                                 (update selected-command :args (partial remove str/blank?)))))]
      (if (:command chat-command)
        ;; current input contains command
        (if (= :complete (input-model/command-completion chat-command))
          ;; command is complete, clear sequential arguments and proceed with command processing
          (-> db
              clear-seq-arguments
              (model/set-chat-ui-props {:sending-in-progress? true})
              (proceed-command chat-command message-id current-time))
          ;; command is not complete, just add space after command if necessary
          {:db (cond-> db
                 (not (input-model/text-ends-with-space? input-text))
                 (set-chat-input-text const/spacing-char :append? true))})
        ;; no command detected, when not empty, proceed by sending text message without command processing
        (if (str/blank? input-text)
          {:db db}
          (-> db
              (set-chat-input-metadata nil)
              (set-chat-input-text nil)
              update-suggestions
              ;; TODO: refactor send-message.cljs to use atomic pure handlers and get rid of this dispatch
              (assoc :dispatch [:prepare-message {:message  input-text
                                                  :chat-id  current-chat-id
                                                  :identity current-public-key
                                                  :address  (:accounts/current-account-id db)}])))))))

;; TODO: remove this handler and leave only helper fn once all invocations are refactored
(handlers/register-handler-db
  :clear-seq-arguments
  (fn [db]
    (clear-seq-arguments db)))

(handlers/register-handler-db
  ::update-seq-arguments
  [re-frame/trim-v]
  (fn [{:keys [current-chat-id chats] :as db} [chat-id]]
    (let [chat-id (or chat-id current-chat-id)
          text    (get-in chats [chat-id :seq-argument-input-text])]
      (-> db
          (update-in [:chats chat-id :seq-arguments] #(into [] (conj % text)))
          (assoc-in [:chats chat-id :seq-argument-input-text] nil)))))

(handlers/register-handler-fx
  :send-seq-argument
  (fn [{{:keys [current-chat-id chats] :as db} :db} _]
    (let [text             (get-in chats [current-chat-id :seq-argument-input-text])
          seq-arguments    (get-in chats [current-chat-id :seq-arguments])
          command          (-> (input-model/selected-chat-command db current-chat-id)
                               (assoc :args (into [] (conj seq-arguments text))))]
      (request-command-data db {:content             command
                                :chat-id             current-chat-id
                                :jail-id             (or (get-in command [:command :bot]) current-chat-id)
                                :data-type           :validator
                                :event-after-creator (fn [_ jail-response]
                                                       [::proceed-validation
                                                        jail-response
                                                        [[::update-seq-arguments current-chat-id]
                                                         [:send-current-message]]])}))))

(handlers/register-handler-db
  :set-chat-seq-arg-input-text
  [re-frame/trim-v]
  (fn [db [text]]
    (set-chat-seq-arg-input-text db text)))

(handlers/register-handler-fx
  :update-text-selection
  [re-frame/trim-v]
  (fn [{:keys [db]} [selection]]
    (update-text-selection db selection)))

(handlers/register-handler-fx
  :select-prev-argument
  (fn [{{:keys [chat-ui-props current-chat-id] :as db} :db} _]
    (let [input-text (get-in db [:chats current-chat-id :input-text])
          command    (input-model/selected-chat-command db current-chat-id input-text)]
      (if (get-in command [:command :sequential-params])
        (-> (set-command-argument db 0 "" false)
            (update :db set-chat-seq-arg-input-text "")
            (as-> fx
                (merge fx (load-chat-parameter-box (:db fx) (:command command)))))
        (let [arg-pos (input-model/argument-position db)]
          (when (pos? arg-pos)
            (let [input-text (get-in db [:chats current-chat-id :input-text])
                  new-sel    (->> (input-model/split-command-args input-text)
                                  (take (inc arg-pos))
                                  (input-model/join-command-args)
                                  (count))
                  ref        (get-in chat-ui-props [current-chat-id :input-ref])]
              (-> db
                  (update-text-selection new-sel)
                  (assoc ::set-native-props
                         {:ref ref
                          :props {:selection {:start new-sel :end new-sel}}})))))))))

(handlers/register-handler-fx
  :set-contact-as-command-argument
  [re-frame/trim-v]
  (fn [{:keys [db]} [params]]
    (set-contact-as-command-argument db params)))

(handlers/register-handler-fx
  :show-suggestions
  (fn [{:keys [db]} _]
    (-> db
        (model/toggle-chat-ui-prop :show-suggestions?)
        (model/set-chat-ui-props {:validation-messages nil})
        update-suggestions)))
