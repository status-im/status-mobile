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
  :update-input-data
  (fn [db]
    (input-model/modified-db-after-change db)))

(handlers/register-handler :set-chat-input-text
  (fn [{:keys [current-chat-id chats chat-ui-props] :as db} [_ text chat-id]]
    (let [chat-id          (or chat-id current-chat-id)
          ends-with-space? (input-model/text-ends-with-space? text)]
      (dispatch [:update-suggestions chat-id text])
      (->> text
           (input-model/text->emoji)
           (assoc-in db [:chats chat-id :input-text])))))

(handlers/register-handler :add-to-chat-input-text
  (handlers/side-effect!
    (fn [{:keys [chats current-chat-id]} [_ text-to-add]]
      (let [input-text (get-in chats [current-chat-id :input-text])]
        (dispatch [:set-chat-input-text (str input-text text-to-add)])))))

(handlers/register-handler :select-chat-input-command
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chat-ui-props] :as db}
         [_ {:keys [prefill prefill-bot-db sequential-params name] :as command} metadata prevent-auto-focus?]]
      (dispatch [:set-chat-input-text (str (chat-utils/command-name command)
                                           const/spacing-char
                                           (when-not sequential-params
                                             (input-model/join-command-args prefill)))])
      (dispatch [:clear-bot-db])
      (when prefill-bot-db
        (dispatch [:update-bot-db {:bot current-chat-id
                                   :db  prefill-bot-db}]))
      (dispatch [:set-chat-input-metadata metadata])
      (dispatch [:set-chat-ui-props {:show-suggestions?   false
                                     :result-box          nil
                                     :validation-messages nil
                                     :prev-command        name}])
      (dispatch [:load-chat-parameter-box command 0])
      (if sequential-params
        (js/setTimeout
          #(do (when-not prevent-auto-focus?
                 (dispatch [:chat-input-focus :seq-input-ref]))
               (dispatch [:set-chat-seq-arg-input-text (str/join const/spacing-char prefill)]))
          100)
        (when-not prevent-auto-focus?
          (dispatch [:chat-input-focus :input-ref]))))))

(handlers/register-handler :set-chat-input-metadata
  (fn [{:keys [current-chat-id] :as db} [_ data chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (assoc-in db [:chats chat-id :input-metadata] data))))

(handlers/register-handler :set-command-argument
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ [index arg move-to-next?]]]
      (let [command     (-> (get-in db [:chats current-chat-id :input-text])
                            (input-model/split-command-args))
            seq-params? (-> (input-model/selected-chat-command db current-chat-id)
                            (get-in [:command :sequential-params]))]
        (if seq-params?
          (dispatch [:set-chat-seq-arg-input-text arg])
          (let [arg          (str/replace arg (re-pattern const/arg-wrapping-char) "")
                command-name (first command)
                command-args (into [] (rest command))
                command-args (if (< index (count command-args))
                               (assoc command-args index arg)
                               (conj command-args arg))]
            (dispatch [:set-chat-input-text (str command-name
                                                 const/spacing-char
                                                 (input-model/join-command-args command-args)
                                                 (when (and move-to-next?
                                                            (= index (dec (count command-args))))
                                                   const/spacing-char))])))))))

(handlers/register-handler :chat-input-focus
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chat-ui-props] :as db} [_ ref]]
      (try
        (when-let [ref (get-in chat-ui-props [current-chat-id ref])]
          (.focus ref))
        (catch :default e
          (log/debug "Cannot focus the reference"))))))

(handlers/register-handler :chat-input-blur
  (handlers/side-effect!
    (fn [{:keys [current-chat-id chat-ui-props] :as db} [_ ref]]
      (try
        (when-let [ref (get-in chat-ui-props [current-chat-id ref])]
          (.blur ref))
        (catch :default e
          (log/debug "Cannot blur the reference"))))))

(handlers/register-handler :update-suggestions
  (fn [{:keys [current-chat-id] :as db} [_ chat-id text]]
    (let [chat-id         (or chat-id current-chat-id)
          chat-text       (str/trim (or text (get-in db [:chats chat-id :input-text]) ""))
          requests        (->> (suggestions/get-request-suggestions db chat-text)
                               (remove (fn [{:keys [type]}]
                                         (= type :grant-permissions))))
          commands        (suggestions/get-command-suggestions db chat-text)
          global-commands (suggestions/get-global-command-suggestions db chat-text)
          all-commands (->> (into global-commands commands)
                            (remove (fn [[k {:keys [hidden?]}]] hidden?))
                            (into {}))
          {:keys [dapp?]} (get-in db [:contacts/contacts chat-id])]
      (when dapp?
        (if (str/blank? chat-text)
          (dispatch [:set-in [:chats chat-id :parameter-boxes :message] nil])
          (when (every? empty? [requests commands])
            (dispatch [::check-dapp-suggestions chat-id chat-text]))))
      (-> db
          (assoc-in [:chats chat-id :request-suggestions] requests)
          (assoc-in [:chats chat-id :command-suggestions] all-commands)))))

(handlers/register-handler :load-chat-parameter-box
  (handlers/side-effect!
    (fn [{:keys [current-chat-id bot-db current-account-id] :as db}
         [_ {:keys [name type bot owner-id] :as command}]]
      (let [parameter-index (input-model/argument-position db current-chat-id)]
        (when (and command (> parameter-index -1))
          (let [data    (get-in db [:local-storage current-chat-id])
                bot-db  (get bot-db (or bot current-chat-id))
                path    [(if (= :command type) :commands :responses)
                         name
                         :params
                         parameter-index
                         :suggestions]
                args    (-> (get-in db [:chats current-chat-id :input-text])
                            (input-model/split-command-args)
                            (rest))
                seq-arg (get-in db [:chats current-chat-id :seq-argument-input-text])
                to      (get-in db [:contacts/contacts current-chat-id :address])
                params  {:parameters {:args    args
                                      :bot-db  bot-db
                                      :seq-arg seq-arg}
                         :context    (merge {:data data
                                             :from current-account-id
                                             :to   to}
                                            (input-model/command-dependent-context-params current-chat-id command))}]
            (status/call-jail
              {:jail-id  (or bot owner-id current-chat-id)
               :path     path
               :params   params
               :callback #(dispatch [:received-bot-response
                                     {:chat-id         current-chat-id
                                      :command         command
                                      :parameter-index parameter-index}
                                     %])})))))))

(handlers/register-handler ::send-message
  (handlers/side-effect!
    (fn [{:keys [current-public-key current-account-id] :as db} [_ command chat-id]]
      (let [text (get-in db [:chats chat-id :input-text])
            data {:message  text
                  :command  command
                  :chat-id  chat-id
                  :identity current-public-key
                  :address  current-account-id}]
        (dispatch [:set-chat-input-text nil chat-id])
        (dispatch [:set-chat-input-metadata nil chat-id])
        (dispatch [:set-chat-ui-props {:sending-in-progress? false}])
        (cond
          command
          (dispatch [:check-commands-handlers! data])
          (not (str/blank? text))
          (dispatch [:prepare-message data]))))))

(handlers/register-handler :proceed-command
  (handlers/side-effect!
    (fn [db [_ {{:keys [bot]} :command :as content} chat-id]]
      (let [params            {:content content
                               :chat-id chat-id
                               :jail-id (or bot chat-id)}
            on-send-params    (merge params
                                     {:data-type :on-send
                                      :after     #(dispatch [::send-command %2 content chat-id])})
            after-validation  #(dispatch [::request-command-data on-send-params])
            validation-params (merge params
                                     {:data-type :validator
                                      :after     #(dispatch [::proceed-validation %2 after-validation])})]

        (dispatch [::request-command-data validation-params])))))

(handlers/register-handler ::proceed-validation
  (handlers/side-effect!
    (fn [db [_ {:keys [markup validationHandler parameters]} proceed-fn]]
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

(handlers/register-handler ::execute-validation-handler
  (handlers/side-effect!
    (fn [_ [_ name params set-errors proceed]]
      (when-let [validator (input-model/validation-handler name)]
        (validator params set-errors proceed)))))

(handlers/register-handler ::send-command
  (handlers/side-effect!
    (fn [db [_ on-send {{:keys [fullscreen bot]} :command :as content} chat-id]]
      (if on-send
        (do
          (when fullscreen
            (dispatch [:choose-predefined-expandable-height :result-box :max]))
          (dispatch [:set-chat-ui-props {:result-box           on-send
                                         :sending-in-progress? false}])
          (react-comp/dismiss-keyboard!))
        (dispatch [::request-command-data
                   {:content   content
                    :chat-id   chat-id
                    :jail-id   (or bot chat-id)
                    :data-type :preview
                    :after     #(dispatch [::send-message % chat-id])}])))))

(handlers/register-handler ::request-command-data
  (handlers/side-effect!
    (fn [{:keys [bot-db]
          :contacts/keys [contacts] :as db}
         [_ {{:keys [command
                     metadata
                     args]
              :as   content} :content
             :keys     [chat-id jail-id data-type after]}]]
      (let [{:keys [dapp? dapp-url name]} (get contacts chat-id)
            message-id      (random/id)
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
                             :created-at (time/now-ms)
                             :id         message-id
                             :chat-id    chat-id
                             :jail-id    (or owner-id jail-id)}

            request-data    {:message-id   message-id
                             :chat-id      chat-id
                             :jail-id      (or owner-id jail-id)
                             :content      {:command (:name command)
                                            :params  params
                                            :type    (:type command)}
                             :on-requested #(after command-message %)}]
        (dispatch [:request-command-data request-data data-type])))))

(handlers/register-handler :send-current-message
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

(handlers/register-handler ::check-dapp-suggestions
  (handlers/side-effect!
    (fn [{:keys [current-account-id] :as db} [_ chat-id text]]
      (let [data (get-in db [:local-storage chat-id])]
        (status/call-function!
          {:chat-id    chat-id
           :function   :on-message-input-change
           :parameters {:message text}
           :context    {:data data
                        :from current-account-id}})))))

(handlers/register-handler :clear-seq-arguments
  (fn [{:keys [current-chat-id chats] :as db} [_ chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (-> db
          (assoc-in [:chats chat-id :seq-arguments] [])
          (assoc-in [:chats chat-id :seq-argument-input-text] nil)))))

(handlers/register-handler :update-seq-arguments
  (fn [{:keys [current-chat-id chats] :as db} [_ chat-id]]
    (let [chat-id (or chat-id current-chat-id)
          text    (get-in chats [chat-id :seq-argument-input-text])]
      (-> db
          (update-in [:chats chat-id :seq-arguments] #(into [] (conj % text)))
          (assoc-in [:chats chat-id :seq-argument-input-text] nil)))))

(handlers/register-handler :send-seq-argument
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
                   {:content   command
                    :chat-id   chat-id
                    :jail-id   (or (get-in command [:command :bot]) chat-id)
                    :data-type :validator
                    :after     #(dispatch [::proceed-validation %2 after-validation])}])))))

(handlers/register-handler :set-chat-seq-arg-input-text
  (fn [{:keys [current-chat-id] :as db} [_ text chat-id]]
    (let [chat-id (or chat-id current-chat-id)]
      (assoc-in db [:chats chat-id :seq-argument-input-text] text))))

(handlers/register-handler :update-text-selection
  (handlers/side-effect!
    (fn [{:keys [current-chat-id] :as db} [_ selection]]
      (let [input-text (get-in db [:chats current-chat-id :input-text])
            command    (input-model/selected-chat-command db current-chat-id input-text)]
        (when (and (= selection (+ (count const/command-char)
                                   (count (get-in command [:command :name]))
                                   (count const/spacing-char)))
                   (get-in command [:command :sequential-params]))
          (dispatch [:chat-input-focus :seq-input-ref]))
        (dispatch [:set-chat-ui-props {:selection selection}])
        (dispatch [:load-chat-parameter-box (:command command)])))))

(handlers/register-handler :select-prev-argument
  (handlers/side-effect!
    (fn [{:keys [chat-ui-props current-chat-id] :as db} _]
      (let [input-text (get-in db [:chats current-chat-id :input-text])
            command    (input-model/selected-chat-command db current-chat-id input-text)]
        (if (get-in command [:command :sequential-params])
          (do
            (dispatch [:set-command-argument [0 "" false]])
            (dispatch [:set-chat-seq-arg-input-text ""])
            (dispatch [:load-chat-parameter-box (:command command)]))
          (let [arg-pos (input-model/argument-position db current-chat-id)]
            (when (pos? arg-pos)
              (let [input-text (get-in db [:chats current-chat-id :input-text])
                    new-sel    (->> (input-model/split-command-args input-text)
                                    (take (inc arg-pos))
                                    (input-model/join-command-args)
                                    (count))
                    ref        (get-in chat-ui-props [current-chat-id :input-ref])]
                (.setNativeProps ref (clj->js {:selection {:start new-sel :end new-sel}}))
                (dispatch [:update-text-selection new-sel])))))))))

(handlers/register-handler :select-next-argument
  (handlers/side-effect!
    (fn [{:keys [chat-ui-props current-chat-id] :as db} _]
      (let [arg-pos (input-model/argument-position db current-chat-id)]
        (let [input-text   (get-in db [:chats current-chat-id :input-text])
              command-args (cond-> (input-model/split-command-args input-text)
                                   (input-model/text-ends-with-space? input-text) (conj ""))
              new-sel      (->> command-args
                                (take (+ 3 arg-pos))
                                (input-model/join-command-args)
                                count
                                (min (count input-text)))
              ref          (get-in chat-ui-props [current-chat-id :input-ref])]
          (.setNativeProps ref (clj->js {:selection {:start new-sel :end new-sel}}))
          (dispatch [:update-text-selection new-sel]))))))
