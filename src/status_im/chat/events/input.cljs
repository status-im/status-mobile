(ns status-im.chat.events.input
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as constants]
            [status-im.chat.models :as model]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.commands :as commands-model]
            [status-im.chat.models.message :as message-model]
            [status-im.chat.events.commands :as commands-events]
            [status-im.bots.events :as bots-events]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.ui.components.react :as react-comp]
            [status-im.utils.handlers :as handlers]))

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

(defn set-chat-input-text
  "Set input text for current-chat and updates suggestions relevant to current input.
  Takes db, input text and `:append?` flag as arguments and returns new db.
  When `:append?` is false or not provided, resets the current chat input with input text,
  otherwise input text is appended to the current chat input."
  [{:keys [current-chat-id] :as db} new-input & {:keys [append?]}]
  (let [current-input (get-in db [:chats current-chat-id :input-text])
        {:keys [dapp?]} (get-in db [:contacts/contacts current-chat-id])
        chat-text (if append?
                    (str current-input new-input)
                    new-input)]
    (cond-> (model/set-chat-ui-props db {:validation-messages nil})
      true
      (assoc-in [:chats current-chat-id :input-text] (input-model/text->emoji chat-text))

      (and dapp? (string/blank? chat-text))
      (assoc-in [:chats current-chat-id :parameter-boxes :message] nil))))

;; TODO janherich: this is super fragile and won't work at all for group chats with bots.
;; The proper way how to do it is to check each chat participant and call `:on-message-input-change`
;; jail function in each participant's jail
(defn call-on-message-input-change
  "Calls bot's `on-message-input-change` function"
  [{:keys [current-chat-id chats local-storage] :as db}]
  (let [chat-text       (string/trim (or (get-in chats [current-chat-id :input-text]) ""))
        {:keys [dapp?]} (get-in db [:contacts/contacts current-chat-id])
        address         (get-in db [:account/account :address])]
    (cond-> {:db db}
      (and dapp? (not (string/blank? chat-text)))
      (assoc :call-jail-function {:chat-id    current-chat-id
                                  :function   :on-message-input-change
                                  :parameters {:message chat-text}
                                  :context    {:data (get local-storage current-chat-id)
                                               :from address}}))))

(defn set-chat-input-metadata
  "Set input metadata for active chat. Takes db and metadata and returns updated db."
  [{:keys [current-chat-id] :as db} metadata]
  (assoc-in db [:chats current-chat-id :input-metadata] metadata))

(defn set-command-argument
  "Sets command argument in active chat"
  [{:keys [current-chat-id] :as db} index arg move-to-next?]
  (let [command     (-> (get-in db [:chats current-chat-id :input-text])
                        (input-model/split-command-args))]
    (let [arg          (string/replace arg (re-pattern constants/arg-wrapping-char) "")
          command-name (first command)
          command-args (into [] (rest command))
          command-args (if (< index (count command-args))
                         (assoc command-args index arg)
                         (conj command-args arg))
          input-text   (str command-name
                            constants/spacing-char
                            (input-model/join-command-args command-args)
                            (when (and move-to-next?
                                       (= index (dec (count command-args))))
                              constants/spacing-char))]
      (set-chat-input-text db input-text))))

(defn load-chat-parameter-box
  "Returns fx for loading chat parameter box for active chat"
  [{:keys [current-chat-id bot-db] :as db}
   {:keys [name scope-bitmask type bot owner-id] :as command}]
  (let [parameter-index (input-model/argument-position db)]
    (when (and command (> parameter-index -1))
      (let [data    (get-in db [:local-storage current-chat-id])
            bot-db  (get bot-db owner-id)
            path    [(if (= :command type) :commands :responses)
                     [name scope-bitmask]
                     :params
                     parameter-index
                     :suggestions]
            args    (-> (get-in db [:chats current-chat-id :input-text])
                        input-model/split-command-args
                        rest)
            to      (get-in db [:contacts/contacts current-chat-id :address])
            from    (get-in db [:account/account :address])
            params  {:parameters {:args    args
                                  :bot-db  bot-db}
                     :context    {:data data
                                  :from from
                                  :to   to}}]
        {:call-jail [{:jail-id                owner-id
                      :path                   path
                      :params                 params
                      :callback-event-creator (fn [jail-response]
                                                [:chat-received-message/bot-response
                                                 {:chat-id         current-chat-id
                                                  :command         command
                                                  :parameter-index parameter-index}
                                                 jail-response])}]}))))

(defn chat-input-focus
  "Returns fx for focusing on active chat input reference"
  [{:keys [current-chat-id chat-ui-props]} ref]
  (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
    {::focus-rn-component cmp-ref}))

(defn update-text-selection
  "Updates text selection in active chat input"
  [db selection]
  (let [command               (input-model/selected-chat-command db)
        new-db                (model/set-chat-ui-props db {:selection selection})
        chat-parameter-box-fx (when command
                                (load-chat-parameter-box new-db (:command command)))]
    (cond-> {:db new-db}

      chat-parameter-box-fx
      (merge chat-parameter-box-fx))))

(defn select-chat-input-command
  "Selects command + (optional) arguments as input for active chat"
  [{:keys [prefill prefill-bot-db name owner-id] :as command} metadata prevent-auto-focus? {:keys [db]}]
  (let [{:keys [current-chat-id chat-ui-props]} db
        db' (-> db
                (bots-events/clear-bot-db owner-id)
                (model/set-chat-ui-props {:show-suggestions?   false
                                          :result-box          nil})
                (set-chat-input-metadata metadata)
                (set-chat-input-text (str (commands-model/command-name command)
                                          constants/spacing-char
                                          (input-model/join-command-args prefill))))
        fx  (assoc (load-chat-parameter-box db' command) :db db')]
    (cond-> fx
      prefill-bot-db (update :db bots-events/update-bot-db {:db  prefill-bot-db
                                                            :bot owner-id})

      (not prevent-auto-focus?)
      (merge (chat-input-focus (:db fx) :input-ref)))))

;; TODO(goranjovic) - generalize setting something as a command argument
(defn set-contact-as-command-argument
  "Sets contact as command argument for active chat"
  [db {:keys [bot-db-key contact arg-index]}]
  (let [name    (string/replace (:name contact) (re-pattern constants/arg-wrapping-char) "")
        contact (select-keys contact [:address :whisper-identity :name :photo-path :dapp?])
        command-owner (get-in (input-model/selected-chat-command db) [:command :owner-id])]
    (-> db
        (set-command-argument arg-index name true)
        (bots-events/set-in-bot-db {:bot   command-owner
                                    :path  [:public (keyword bot-db-key)]
                                    :value contact})
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

;; TODO(goranjovic) - generalize setting something as a command argument
(defn set-asset-as-command-argument
  "Sets asset as command argument for active chat"
  [db {:keys [bot-db-key asset arg-index]}]
  (let [name          (string/replace (name (:symbol asset)) (re-pattern constants/arg-wrapping-char) "")
        command-owner (get-in (input-model/selected-chat-command db) [:command :owner-id])]
    (-> db
        (set-command-argument arg-index name true)
        (bots-events/set-in-bot-db {:bot   command-owner
                                    :path  [:public (keyword bot-db-key)]
                                    :value asset})
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

;; function creating "message shaped" data from command, because that's what `request-command-message-data` expects
(defn- command->message
  [{:keys [bot-db current-chat-id chats]} {:keys [command] :as command-params}]
  (message-model/add-message-type
   {:chat-id current-chat-id
    :content {:bot                   (:owner-id command)
              :command               (:name command)
              :type                  (:type command)
              :command-scope-bitmask (:scope-bitmask command)
              :params                (assoc (input-model/args->params command-params)
                                            :bot-db (get bot-db (:owner-id command)))}}
   (get chats current-chat-id)))

(defn proceed-command
  "Proceed with command processing by creating command message + setting up and executing chain of events:
  1. Params validation
  2. Short preview fetching
  3. Preview fetching"
  [{:keys [bot-db current-chat-id chats] :as db} {:keys [command metadata] :as command-params} message-id current-time]
  (let [message     (command->message db command-params)
        cmd-params  {:command    command
                     :params     (get-in message [:content :params])
                     :to-message (:to-message-id metadata)
                     :created-at current-time
                     :id         message-id
                     :chat-id    current-chat-id}
        event-chain {:data-type             :validator
                     :proceed-event-creator (fn [validation-response]
                                              [::proceed-validation
                                               validation-response
                                               [[:request-command-message-data
                                                 message
                                                 {:data-type             :short-preview
                                                  :proceed-event-creator (fn [short-preview]
                                                                           [:request-command-message-data
                                                                            message
                                                                            {:data-type             :preview
                                                                             :proceed-event-creator (fn [preview]
                                                                                                      [::send-command
                                                                                                       (update cmd-params :command merge
                                                                                                               {:short-preview short-preview
                                                                                                                :preview       preview})])}])}]]])}]
    (commands-events/request-command-message-data db message event-chain)))

;;;; Handlers

(handlers/register-handler-fx
 :set-chat-input-text
 [re-frame/trim-v]
 (fn [{:keys [db]} [text]]
   (let [new-db (set-chat-input-text db text)
         fx     (call-on-message-input-change new-db)]
     (if-let [{:keys [command]} (input-model/selected-chat-command new-db)]
       (merge fx (load-chat-parameter-box new-db command))
       fx))))

(handlers/register-handler-fx
 :select-chat-input-command
 [re-frame/trim-v]
 (fn [cofx [command metadata prevent-auto-focus?]]
   (select-chat-input-command command metadata prevent-auto-focus? cofx)))

(handlers/register-handler-db
 :set-command-argument
 [re-frame/trim-v]
 (fn [db [[index arg move-to-next?]]]
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
 ::proceed-validation
 [re-frame/trim-v]
 (fn [_ [{:keys [markup parameters]} proceed-events]]
   (let [error-events-creator (fn [validator-result]
                                [[:set-chat-ui-props {:validation-messages  validator-result
                                                      :sending-in-progress? false}]])
         events (if markup
                  (error-events-creator markup)
                  proceed-events)]
     {:dispatch-n events})))

(defn cleanup-chat-command [db]
  (-> (model/set-chat-ui-props db {:sending-in-progress? false})
      (set-chat-input-metadata nil)
      (set-chat-input-text nil)))

(handlers/register-handler-fx
 :cleanup-chat-command
 (fn [{:keys [db]}]
   {:db (cleanup-chat-command db)}))

(handlers/register-handler-fx
 ::send-command
 message-model/send-interceptors
 (fn [{:keys [db] :as cofx} [command-message]]
   (let [{:keys [current-chat-id current-public-key]} db
         new-db  (cleanup-chat-command db)
         address (get-in db [:account/account :address])]
     (merge {:db new-db}
            (message-model/process-command (assoc cofx :db new-db)
                                           {:message  (get-in db [:chats current-chat-id :input-text])
                                            :command  command-message
                                            :chat-id  current-chat-id
                                            :identity current-public-key
                                            :address  address})))))

(defn command-complete?
  [chat-command]
  (= :complete (input-model/command-completion chat-command)))

(defn command-complete-fx
  "command is complete, set `:sendint-in-progress?` flag and proceed with command processing"
  [db chat-command message-id current-time]
  (-> db
      (model/set-chat-ui-props {:sending-in-progress? true})
      (proceed-command chat-command message-id current-time)))

(defn command-not-complete-fx
  "command is not complete, just add space after command if necessary"
  [db input-text]
  {:db (cond-> db
         (not (input-model/text-ends-with-space? input-text))
         (set-chat-input-text constants/spacing-char :append? true))})

(defn plain-text-message-fx
  "no command detected, when not empty, proceed by sending text message without command processing"
  [db cofx input-text current-chat-id current-public-key]
  (when-not (string/blank? input-text)
    (message-model/send-message (assoc cofx :db (-> db
                                                    (set-chat-input-metadata nil)
                                                    (set-chat-input-text nil)))
                                {:message-text input-text
                                 :chat-id      current-chat-id
                                 :identity     current-public-key})))

(handlers/register-handler-fx
 :send-current-message
 message-model/send-interceptors
 (fn [{{:keys [current-chat-id current-public-key] :as db} :db message-id :random-id current-time :now
       :as cofx} _]
   (when-not (get-in db [:chat-ui-props current-chat-id :sending-in-progress?])
     (let [input-text   (get-in db [:chats current-chat-id :input-text])
           chat-command (-> (input-model/selected-chat-command db)
                            (update :args (partial remove string/blank?)))]
       (if (:command chat-command)
          ;; Returns true if current input contains command
         (if (command-complete? chat-command)
           (command-complete-fx db chat-command message-id current-time)
           (command-not-complete-fx db input-text))
         (plain-text-message-fx db cofx input-text current-chat-id current-public-key))))))

(handlers/register-handler-fx
 :update-text-selection
 [re-frame/trim-v]
 (fn [{:keys [db]} [selection]]
   (update-text-selection db selection)))

(handlers/register-handler-fx
 :set-contact-as-command-argument
 [re-frame/trim-v]
 (fn [{:keys [db]} [params]]
   (set-contact-as-command-argument db params)))

(handlers/register-handler-fx
 :set-asset-as-command-argument
 [re-frame/trim-v]
 (fn [{:keys [db]} [params]]
   (set-asset-as-command-argument db params)))

(handlers/register-handler-db
 :show-suggestions
 (fn [db _]
   (-> db
       (model/toggle-chat-ui-prop :show-suggestions?)
       (model/set-chat-ui-props {:validation-messages nil}))))
