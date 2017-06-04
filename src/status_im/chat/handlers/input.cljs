(ns status-im.chat.handlers.input
  (:require [re-frame.core :refer [enrich after dispatch]]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as const]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.suggestions :as suggestions]
            [status-im.components.react :as react-comp]
            [status-im.components.status :as status]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.random :as random]
            [status-im.i18n :as i18n]
            [clojure.string :as str]))

(handlers/register-handler
  :set-chat-input-text
  (fn [{:keys [current-chat-id chats chat-ui-props] :as db} [_ text chat-id]]
    (let [chat-id          (or chat-id current-chat-id)
          ends-with-space? (input-model/text-ends-with-space? text)]
      (dispatch [:update-suggestions chat-id text])

      (if-let [{command :command} (input-model/selected-chat-command db chat-id text)]
        (let [{old-args :args} (input-model/selected-chat-command db chat-id)
              text-splitted  (input-model/split-command-args text)
              new-args       (rest text-splitted)
              new-input-text (input-model/make-input-text text-splitted old-args)]
          (assoc-in db [:chats chat-id :input-text] new-input-text))
        (assoc-in db [:chats chat-id :input-text] text)))))

(handlers/register-handler
  :add-to-chat-input-text
  (handlers/side-effect!
    (fn [{:keys [chats current-chat-id]} [_ text-to-add]]
      (let [input-text (get-in chats [current-chat-id :input-text])]
        (dispatch [:set-chat-input-text (str input-text text-to-add)])))))

(handlers/register-handler
  :select-chat-input-command
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chat-ui-props] :as db}
         [_ {:keys [prefill sequential-params] :as command} metadata]]
      (dispatch [:set-chat-input-text (str (chat-utils/command-name command)
                                           const/spacing-char
                                           (when-not sequential-params
                                             (input-model/join-command-args prefill)))])
      (dispatch [:set-chat-input-metadata metadata])
      (dispatch [:set-chat-ui-props {:show-suggestions?   false
                                     :result-box          nil
                                     :validation-messages nil}])
      (dispatch [:load-chat-parameter-box command 0])
      (if sequential-params
        (js/setTimeout
          #(do (dispatch [:chat-input-focus :seq-input-ref])
               (dispatch [:set-chat-seq-arg-input-text (str/join const/spacing-char prefill)]))
          100)
        (dispatch [:chat-input-focus :input-ref])))))

(handlers/register-handler
  :set-chat-input-metadata
  (fn [{:keys [current-chat-id] :as db} [_ data chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (assoc-in db [:chats chat-id :input-metadata] data))))

(handlers/register-handler
  :set-command-argument
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ [index arg]]]
      (let [command     (-> (get-in db [:chats current-chat-id :input-text])
                            (input-model/split-command-args))
            seq-params? (-> (input-model/selected-chat-command db current-chat-id)
                            (get-in [:command :sequential-params]))]
        (if seq-params?
          (dispatch [:set-chat-seq-arg-input-text arg])
          (let [command-name (first command)
                command-args (into [] (rest command))
                command-args (if (< index (count command-args))
                               (assoc command-args index arg)
                               (conj command-args arg))]
            (dispatch [:set-chat-input-text (str command-name
                                                 const/spacing-char
                                                 (input-model/join-command-args command-args)
                                                 const/spacing-char)])))))))

(handlers/register-handler
  :chat-input-focus
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chat-ui-props] :as db} [_ ref]]
      (try
        (when-let [ref (get-in chat-ui-props [current-chat-id ref])]
          (.focus ref))
        (catch :default e
          (log/debug "Cannot focus the reference"))))))

(handlers/register-handler
  :update-suggestions
  (fn [{:keys [current-chat-id] :as db} [_ chat-id text]]
    (let [chat-id         (or chat-id current-chat-id)
          chat-text       (or text (get-in db [:chats chat-id :input-text]) "")
          requests        (->> (suggestions/get-request-suggestions db chat-text)
                               (remove (fn [{:keys [type]}]
                                         (= type :grant-permissions))))
          suggestions     (suggestions/get-command-suggestions db chat-text)
          global-commands (suggestions/get-global-command-suggestions db chat-text)
          {:keys [dapp?]} (get-in db [:contacts chat-id])]
      (if (and dapp? (str/blank? chat-text))
        (dispatch [:set-in [:chats chat-id :parameter-boxes :message] nil])
        (dispatch [::check-dapp-suggestions chat-id chat-text]))
      (-> db
          (assoc-in [:chats chat-id :request-suggestions] requests)
          (assoc-in [:chats chat-id :command-suggestions] (into suggestions global-commands))))))

(handlers/register-handler
  :load-chat-parameter-box
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ {:keys [name type bot] :as command}]]
      (let [parameter-index (input-model/argument-position db current-chat-id)]
        (when (and command (> parameter-index -1))
          (let [jail-id (or bot current-chat-id)
                data    (get-in db [:local-storage current-chat-id])
                path    [(if (= :command type) :commands :responses)
                         name
                         :params
                         parameter-index
                         :suggestions]
                args    (-> (get-in db [:chats current-chat-id :input-text])
                            (input-model/split-command-args)
                            (rest))
                params  {:parameters {:args args}
                         :context    (merge {:data data}
                                            (input-model/command-dependent-context-params command))}]
            (status/call-jail
              {:jail-id  jail-id
               :path     path
               :params   params
               :callback #(dispatch [:received-bot-response
                                     {:chat-id         current-chat-id
                                      :command         command
                                      :parameter-index parameter-index}
                                     %])})))))))

(handlers/register-handler
  ::send-message
  (handlers/side-effect!
    (fn [{:keys [current-public-key current-account-id] :as db} [_ command-message chat-id]]
      (let [text (get-in db [:chats chat-id :input-text])
            data {:message  text
                  :command  command-message
                  :chat-id  chat-id
                  :identity current-public-key
                  :address  current-account-id}]
        (dispatch [:set-chat-input-text nil chat-id])
        (dispatch [:set-chat-input-metadata nil chat-id])
        (dispatch [:set-chat-ui-props {:sending-in-progress? false}])
        (cond
          command-message
          (dispatch [:check-commands-handlers! data])
          (not (str/blank? text))
          (dispatch [:prepare-message data]))))))

(handlers/register-handler
  :proceed-command
  (handlers/side-effect!
    (fn [db [_ command chat-id]]
      (let [jail-id (or (get-in command [:command :bot]) chat-id)]
        ;:check-and-load-commands!
        (let [params
              {:command command
               :chat-id chat-id
               :jail-id jail-id}

              on-send-params
              (merge params
                     {:data-type :on-send
                      :after     (fn [_ res]
                                   (dispatch [::send-command res command chat-id]))})

              after-validation
              #(dispatch [::request-command-data on-send-params])

              validation-params
              (merge params
                     {:data-type :validator
                      :after     #(dispatch [::proceed-validation-messages
                                             command chat-id %2 after-validation])})]

          (dispatch [::request-command-data validation-params]))))))

(handlers/register-handler
  ::proceed-validation-messages
  (handlers/side-effect!
    (fn [db [_ command chat-id {:keys [markup validationHandler parameters]} proceed-fn]]
      (let [set-errors #(do (dispatch [:set-chat-ui-props {:validation-messages  %
                                                           :sending-in-progress? false}]))]
        (cond
          markup
          (set-errors markup)

          validationHandler
          (do (dispatch [::execute-validation-handler validationHandler parameters set-errors proceed-fn])
              (dispatch [:set-chat-ui-props {:sending-in-progress? false}]))

          :default
          (proceed-fn))))))

(handlers/register-handler
  ::execute-validation-handler
  (handlers/side-effect!
    (fn [_ [_ name params set-errors proceed]]
      (when-let [validator (input-model/validation-handler name)]
        (validator params set-errors proceed)))))

(handlers/register-handler
  ::send-command
  (handlers/side-effect!
    (fn [db [_ on-send {{:keys [fullscreen]} :command :as command} chat-id]]
      (if on-send
        (do
          (when fullscreen
            (dispatch [:choose-predefined-expandable-height :result-box :max]))
          (dispatch [:set-chat-ui-props {:result-box           on-send
                                         :sending-in-progress? false}])
          (react-comp/dismiss-keyboard!))
        (dispatch [::request-command-data
                   {:command   command
                    :chat-id   chat-id
                    :jail-id   (get-in command [:command :bot])
                    :data-type :preview
                    :after     #(dispatch [::send-message % chat-id])}])))))

(handlers/register-handler
  ::request-command-data
  (handlers/side-effect!
    (fn [{:keys [contacts] :as db}
         [_ {{:keys [command metadata args] :as c} :command
             :keys                                 [message-id chat-id jail-id data-type after]}]]
      (let [{:keys [dapp? dapp-url name]} (get contacts chat-id)
            message-id      (random/id)
            metadata        (merge metadata
                                   (when dapp?
                                     {:url  (i18n/get-contact-translated chat-id :dapp-url dapp-url)
                                      :name (i18n/get-contact-translated chat-id :name name)}))
            params          (input-model/args->params c)
            command-message {:command    command
                             :params     params
                             :to-message (:to-message-id metadata)
                             :created-at (time/now-ms)
                             :id         message-id
                             :chat-id    chat-id
                             :jail-id    jail-id}
            request-data    {:message-id   message-id
                             :chat-id      chat-id
                             :jail-id      jail-id
                             :content      {:command (:name command)
                                            :params  (assoc params :metadata metadata)
                                            :type    (:type command)}
                             :on-requested #(after command-message %)}]
        (dispatch [:request-command-data request-data data-type])))))

(handlers/register-handler
  :send-current-message
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ chat-id]]
      (dispatch [:set-chat-ui-props {:sending-in-progress? true}])
      (let [chat-id      (or chat-id current-chat-id)
            chat-command (input-model/selected-chat-command db chat-id)
            seq-command? (get-in chat-command [:command :sequential-params])
            chat-command (if seq-command?
                           (let [args (get-in db [:chats chat-id :seq-arguments])]
                             (assoc chat-command :args args))
                           (update chat-command :args #(remove str/blank? %)))]
        (if (:command chat-command)
          (if (= :complete (input-model/command-completion chat-command))
            (do
              (dispatch [:proceed-command chat-command chat-id])
              (dispatch [:clear-seq-arguments chat-id]))
            (let [text (get-in db [:chats chat-id :input-text])]
              (dispatch [:set-chat-ui-props {:sending-in-progress? false}])
              (when-not (input-model/text-ends-with-space? text)
                (dispatch [:set-chat-input-text (str text const/spacing-char)]))))
          (dispatch [::send-message nil chat-id]))))))

(handlers/register-handler
  ::check-dapp-suggestions
  (handlers/side-effect!
    (fn [{:keys [current-account-id] :as db} [_ chat-id text]]
      (let [data (get-in db [:local-storage chat-id])]
        (status/call-function!
          {:chat-id    chat-id
           :function   :on-message-input-change
           :parameters {:message text}
           :context    {:data data
                        :from current-account-id}})))))

(handlers/register-handler
  :clear-seq-arguments
  (fn [{:keys [current-chat-id chats] :as db} [_ chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (-> db
          (assoc-in [:chats chat-id :seq-arguments] [])
          (assoc-in [:chats chat-id :seq-argument-input-text] nil)))))

(handlers/register-handler
  :update-seq-arguments
  (fn [{:keys [current-chat-id chats] :as db} [_ chat-id]]
    (let [chat-id (or chat-id current-chat-id)
          text    (get-in chats [chat-id :seq-argument-input-text])]
      (-> db
          (update-in [:chats chat-id :seq-arguments] #(into [] (conj % text)))
          (assoc-in [:chats chat-id :seq-argument-input-text] nil)))))

(handlers/register-handler
  :send-seq-argument
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chats] :as db} [_ chat-id]]
      (let [chat-id          (or chat-id current-chat-id)
            text             (get-in chats [chat-id :seq-argument-input-text])
            seq-arguments    (get-in db [:chats chat-id :seq-arguments])
            command          (-> (input-model/selected-chat-command db chat-id)
                                 (assoc :args (into [] (conj seq-arguments text))))
            args             (get-in chats [chat-id :seq-arguments])
            after-validation #(do
                                (dispatch [:update-seq-arguments chat-id])
                                (dispatch [:send-current-message]))]
        (dispatch [::request-command-data
                   {:command   command
                    :chat-id   chat-id
                    :data-type :validator
                    :after     #(dispatch [::proceed-validation-messages
                                           command chat-id %2 after-validation])}])))))

(handlers/register-handler
  :set-chat-seq-arg-input-text
  (fn [{:keys [current-chat-id] :as db} [_ text chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (assoc-in db [:chats chat-id :seq-argument-input-text] text))))
