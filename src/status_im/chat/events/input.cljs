(ns status-im.chat.events.input
  (:require [clojure.string :as str]
            [re-frame.core :refer [reg-fx reg-cofx inject-cofx dispatch trim-v]]
            [taoensso.timbre :as log]
            [status-im.chat.constants :as const]
            [status-im.chat.utils :as chat-utils]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.suggestions :as suggestions]
            [status-im.components.react :as react-comp]
            [status-im.components.status :as status]
            [status-im.utils.datetime :as time]
            [status-im.utils.handlers :refer [register-handler-db register-handler-fx]]
            [status-im.utils.random :as random]
            [status-im.i18n :as i18n]))

;;;; Coeffects

(reg-cofx
 :now
 (fn [coeffects _]
   (assoc coeffects :now (time/now-ms))))

(reg-cofx
 :random-id
 (fn [coeffects _]
   (assoc coeffects :random-id (random/id))))

;;;; Effects

(reg-fx
 ::focus-rn-component
 (fn [ref]
   (try
     (.focus ref)
     (catch :default e
       (log/debug "Cannot focus the reference")))))

(reg-fx
 ::blur-rn-component
 (fn [ref]
   (try
     (.blur ref)
     (catch :default e
       (log/debug "Cannot blur the reference")))))

(reg-fx
 ::dismiss-keyboard
 (fn [_]
   (react-comp/dismiss-keyboard!)))

(reg-fx
 ::set-native-props
 (fn [{:keys [ref props]}]
   (.setNativeProps ref (clj->js props))))

(reg-fx
 :chat-fx/call-jail-function
 (fn [{:keys [chat-id function callback-events-creator] :as opts}]
   (let [path   [:functions function]
         params (select-keys opts [:parameters :context])]
     (status/call-jail
      {:jail-id chat-id
       :path    path
       :params  params
       :callback (fn [jail-response]
                   (doseq [event (if callback-events-creator
                                   (callback-events-creator jail-response)
                                   [[:received-bot-response
                                     {:chat-id chat-id}
                                     jail-response]])
                           :when event]
                     (dispatch event)))}))))

;;;; Handlers

(register-handler-db
 :update-input-data
 (fn [db]
   (input-model/modified-db-after-change db)))

(register-handler-fx
 :set-chat-input-text
 [trim-v]
 (fn [{{:keys [current-chat-id chats chat-ui-props] :as db} :db} [text chat-id]]
   (let [chat-id (or chat-id current-chat-id)]
     {:db (assoc-in db
                    [:chats chat-id :input-text]
                    (input-model/text->emoji text))
      :dispatch [:update-suggestions chat-id text]})))

(register-handler-fx
 :add-to-chat-input-text
 [trim-v]
 (fn [{{:keys [chats current-chat-id]} :db} [text-to-add]]
   (let [input-text (get-in chats [current-chat-id :input-text])]
     {:dispatch [:set-chat-input-text (str input-text text-to-add)]})))

(register-handler-fx
 :select-chat-input-command
 [trim-v]
 (fn [{{:keys [current-chat-id chat-ui-props]} :db}
      [{:keys [prefill prefill-bot-db sequential-params name] :as command} metadata prevent-auto-focus?]]
   (cond-> {:dispatch-n [[:set-chat-input-text (str (chat-utils/command-name command)
                                                    const/spacing-char
                                                    (when-not sequential-params
                                                      (input-model/join-command-args prefill)))]
                         [:clear-bot-db]
                         [:set-chat-input-metadata metadata]
                         [:set-chat-ui-props {:show-suggestions?   false
                                              :result-box          nil
                                              :validation-messages nil
                                              :prev-command        name}]
                         [:load-chat-parameter-box command 0]]}

     prefill-bot-db
     (update :dispatch-n conj [:update-bot-db {:bot current-chat-id
                                               :db prefill-bot-db}])

     (not (and sequential-params
               prevent-auto-focus?))
     (update :dispatch-n conj [:chat-input-focus :input-ref])

     sequential-params
     (assoc :dispatch-later (cond-> [{:ms 100 :dispatch [:set-chat-seq-arg-input-text
                                                         (str/join const/spacing-char prefill)]}]
                              (not prevent-auto-focus?)
                              (conj {:ms 100 :dispatch [:chat-input-focus :seq-input-ref]}))))))

(register-handler-db
 :set-chat-input-metadata
 [trim-v]
 (fn [{:keys [current-chat-id] :as db} [data chat-id]]
   (let [chat-id (or chat-id current-chat-id)]
     (assoc-in db [:chats chat-id :input-metadata] data))))

(register-handler-fx
 :set-command-argument
 [trim-v]
 (fn [{{:keys [current-chat-id] :as db} :db} [[index arg move-to-next?]]]
   (let [command     (-> (get-in db [:chats current-chat-id :input-text])
                         (input-model/split-command-args))
         seq-params? (-> (input-model/selected-chat-command db current-chat-id)
                         (get-in [:command :sequential-params]))
         event-to-dispatch (if seq-params?
                             [:set-chat-seq-arg-input-text arg]
                             (let [arg          (str/replace arg (re-pattern const/arg-wrapping-char) "")
                                   command-name (first command)
                                   command-args (into [] (rest command))
                                   command-args (if (< index (count command-args))
                                                  (assoc command-args index arg)
                                                  (conj command-args arg))]
                               [:set-chat-input-text (str command-name
                                                          const/spacing-char
                                                          (input-model/join-command-args command-args)
                                                          (when (and move-to-next?
                                                                     (= index (dec (count command-args))))
                                                            const/spacing-char))]))]
     {:dispatch event-to-dispatch})))

(register-handler-fx
 :chat-input-focus
 [trim-v]
 (fn [{{:keys [current-chat-id chat-ui-props]} :db} [ref]]
   (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
     {::focus-rn-component cmp-ref})))

(register-handler-fx
 :chat-input-blur
 [trim-v]
 (fn [{{:keys [current-chat-id chat-ui-props]} :db} [ref]]
   (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
     {::blur-rn-component cmp-ref})))

(register-handler-fx
 :update-suggestions
 [trim-v]
 (fn [{{:keys [current-chat-id] :as db} :db} [chat-id text]]
   (let [chat-id         (or chat-id current-chat-id)
         chat-text       (str/trim (or text (get-in db [:chats chat-id :input-text]) ""))
         requests        (->> (suggestions/get-request-suggestions db chat-text)
                              (remove (fn [{:keys [type]}]
                                        (= type :grant-permissions))))
         commands        (suggestions/get-command-suggestions db chat-text)
         global-commands (suggestions/get-global-command-suggestions db chat-text)
         all-commands    (->> (into global-commands commands)
                              (remove (fn [[k {:keys [hidden?]}]] hidden?))
                              (into {}))
         {:keys [dapp?]} (get-in db [:contacts/contacts chat-id])
         new-db          (cond-> db
                           true (assoc-in [:chats chat-id :request-suggestions] requests)
                           true (assoc-in [:chats chat-id :command-suggestions] all-commands)
                           (and dapp?
                                (str/blank? chat-text))
                           (assoc-in [:chats chat-id :parameter-boxes :message] nil))
         new-event       (when (and dapp?
                                    (not (str/blank? chat-text))
                                    (every? empty? [requests commands]))
                           [::check-dapp-suggestions chat-id chat-text])]
     (cond-> {:db new-db}
       new-event (assoc :dispatch new-event)))))

(register-handler-fx
 :load-chat-parameter-box
 [trim-v]
 (fn [{{:keys [current-chat-id bot-db current-account-id] :as db} :db}
      [{:keys [name type bot owner-id] :as command}]]
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
         {:chat-fx/call-jail {:jail-id (or bot owner-id current-chat-id)
                              :path    path
                              :params  params
                              :callback-events-creator (fn [jail-response]
                                                         [[:received-bot-response
                                                           {:chat-id         current-chat-id
                                                            :command         command
                                                            :parameter-index parameter-index}
                                                           jail-response]])}})))))

(register-handler-fx
 ::send-message
 [trim-v]
 (fn [{{:keys [current-public-key current-account-id] :as db} :db} [command chat-id]]
   (let [text (get-in db [:chats chat-id :input-text])
         data   {:message  text
                 :command  command
                 :chat-id  chat-id
                 :identity current-public-key
                 :address  current-account-id}
         events [[:set-chat-input-text nil chat-id]
                 [:set-chat-input-metadata nil chat-id]
                 [:set-chat-ui-props {:sending-in-progress? false}]]]
     {:dispatch-n (if command
                    (conj events [:check-commands-handlers! data])
                    (if (str/blank? text)
                      events
                      (conj events [:prepare-message data])))})))

(register-handler-fx
 :proceed-command
 [trim-v]
 (fn [_ [{{:keys [bot]} :command :as content} chat-id]]
   (let [params                   {:content content
                                   :chat-id chat-id
                                   :jail-id (or bot chat-id)}
         on-send-params           (merge params
                                         {:data-type           :on-send
                                          :event-after-creator (fn [_ jail-response]
                                                                 [::send-command jail-response content chat-id])})
         after-validation-events  [[::request-command-data on-send-params]]
         validation-params        (merge params
                                         {:data-type           :validator
                                          :event-after-creator (fn [_ jail-response]
                                                                 [::proceed-validation
                                                                  jail-response
                                                                  after-validation-events])})]
     {:dispatch [::request-command-data validation-params]})))

(register-handler-fx
 ::proceed-validation
 [trim-v]
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

(register-handler-fx
 ::execute-validation-handler
 [trim-v]
 (fn [_ [validation-handler-name params error-events-creator proceed-events]]
   (let [error-events (when-let [validator (input-model/validation-handler validation-handler-name)]
                        (validator params error-events-creator))]
     {:dispatch-n (or error-events proceed-events)})))

(register-handler-fx
 ::send-command
 [trim-v]
 (fn [_ [on-send {{:keys [fullscreen bot]} :command :as content} chat-id]]
   (if on-send
     {:dispatch-n (cond-> [[:set-chat-ui-props {:result-box           on-send
                                                :sending-in-progress? false}]]
                    fullscreen
                    (conj [:choose-predefined-expandable-height :result-box :max]))
      ::dismiss-keyboard nil}
     {:dispatch [::request-command-data
                 {:content             content
                  :chat-id             chat-id
                  :jail-id             (or bot chat-id)
                  :data-type           :preview
                  :event-after-creator (fn [command-message _]
                                         [::send-message command-message chat-id])}]})))

(register-handler-fx
 ::request-command-data
 [trim-v (inject-cofx :random-id) (inject-cofx :now)]
 (fn [{{:keys [bot-db] :contacts/keys [contacts]} :db
       message-id :random-id
       current-time :now}
      [{{:keys [command
                metadata
                args]
         :as   content} :content
        :keys  [chat-id jail-id data-type event-after-creator]}]]
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
     {:dispatch [:request-command-data request-data data-type]})))

(register-handler-fx
 :send-current-message
 (fn [{{:keys [current-chat-id] :as db} :db} _]
   (let [chat-command (input-model/selected-chat-command db current-chat-id)
         seq-command? (get-in chat-command [:command :sequential-params])
         chat-command (if seq-command?
                        (let [args (get-in db [:chats current-chat-id :seq-arguments])]
                          (assoc chat-command :args args))
                        (update chat-command :args #(remove str/blank? %)))
         set-chat-ui-props-event [:set-chat-ui-props {:sending-in-progress? true}]
         additional-events (if (:command chat-command)
                             (if (= :complete (input-model/command-completion chat-command))
                               [[:proceed-command chat-command current-chat-id]
                                [:clear-seq-arguments current-chat-id]]
                               (let [text (get-in db [:chats current-chat-id :input-text])]
                                 [[:set-chat-ui-props {:sending-in-progress? false}]
                                  (when-not (input-model/text-ends-with-space? text)
                                    [:set-chat-input-text (str text const/spacing-char)])]))
                             [[::send-message nil current-chat-id]])]
     {:dispatch-n (into [set-chat-ui-props-event]
                        (remove nil? additional-events))})))

(register-handler-fx
 ::check-dapp-suggestions
 [trim-v]
 (fn [{{:keys [current-account-id] :as db} :db} [chat-id text]]
   (let [data (get-in db [:local-storage chat-id])]
     {:chat-fx/call-jail-function {:chat-id    chat-id
                                   :function   :on-message-input-change
                                   :parameters {:message text}
                                   :context    {:data data
                                                :from current-account-id}}})))

(register-handler-db
 :clear-seq-arguments
 [trim-v]
 (fn [{:keys [current-chat-id chats] :as db} [chat-id]]
   (let [chat-id (or chat-id current-chat-id)]
     (-> db
         (assoc-in [:chats chat-id :seq-arguments] [])
         (assoc-in [:chats chat-id :seq-argument-input-text] nil)))))

(register-handler-db
 ::update-seq-arguments
 [trim-v]
 (fn [{:keys [current-chat-id chats] :as db} [chat-id]]
   (let [chat-id (or chat-id current-chat-id)
         text    (get-in chats [chat-id :seq-argument-input-text])]
     (-> db
         (update-in [:chats chat-id :seq-arguments] #(into [] (conj % text)))
         (assoc-in [:chats chat-id :seq-argument-input-text] nil)))))

(register-handler-fx
 :send-seq-argument
 [trim-v]
 (fn [{{:keys [current-chat-id chats] :as db} :db} [chat-id]]
   (let [chat-id          (or chat-id current-chat-id)
         text             (get-in chats [chat-id :seq-argument-input-text])
         seq-arguments    (get-in chats [chat-id :seq-arguments])
         command          (-> (input-model/selected-chat-command db chat-id)
                              (assoc :args (into [] (conj seq-arguments text))))]
     {:dispatch [::request-command-data
                 {:content             command
                  :chat-id             chat-id
                  :jail-id             (or (get-in command [:command :bot]) chat-id)
                  :data-type           :validator
                  :event-after-creator (fn [_ jail-response]
                                         [::proceed-validation
                                          jail-response
                                          [[::update-seq-arguments chat-id]
                                           [:send-current-message]]])}]})))

(register-handler-db
 :set-chat-seq-arg-input-text
 [trim-v]
 (fn [{:keys [current-chat-id] :as db} [text chat-id]]
   (let [chat-id (or chat-id current-chat-id)]
     (assoc-in db [:chats chat-id :seq-argument-input-text] text))))

(register-handler-fx
 :update-text-selection
 [trim-v]
 (fn [{{:keys [current-chat-id] :as db} :db} [selection]]
   (let [input-text (get-in db [:chats current-chat-id :input-text])
         command    (input-model/selected-chat-command db current-chat-id input-text)]
     (cond-> {:dispatch-n [[:set-chat-ui-props {:selection selection}]
                           [:load-chat-parameter-box (:command command)]]}

       (and (= selection (+ (count const/command-char)
                            (count (get-in command [:command :name]))
                            (count const/spacing-char)))
            (get-in command [:command :sequential-params]))
       (update :dispatch-n conj [:chat-input-focus :seq-input-ref])))))

(register-handler-fx
 :select-prev-argument
 (fn [{{:keys [chat-ui-props current-chat-id] :as db} :db} _]
   (let [input-text (get-in db [:chats current-chat-id :input-text])
         command    (input-model/selected-chat-command db current-chat-id input-text)]
     (if (get-in command [:command :sequential-params])
       {:dispatch-n [[:set-command-argument [0 "" false]]
                     [:set-chat-seq-arg-input-text ""]
                     [:load-chat-parameter-box (:command command)]]}
       (let [arg-pos (input-model/argument-position db current-chat-id)]
         (when (pos? arg-pos)
           (let [input-text (get-in db [:chats current-chat-id :input-text])
                 new-sel    (->> (input-model/split-command-args input-text)
                                 (take (inc arg-pos))
                                 (input-model/join-command-args)
                                 (count))
                 ref        (get-in chat-ui-props [current-chat-id :input-ref])]
             {::set-native-props {:ref ref
                                  :props {:selection {:start new-sel :end new-sel}}}
              :dispatch [:update-text-selection new-sel]})))))))

(register-handler-fx
 :select-next-argument
 (fn [{{:keys [chat-ui-props current-chat-id] :as db} :db} _]
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
       {::set-native-props {:ref ref
                            :props {:selection {:start new-sel :end new-sel}}}
        :dispatch [:update-text-selection new-sel]}))))
