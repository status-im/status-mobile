(ns status-im.chat.handlers.commands
  (:require [re-frame.core :refer [enrich after dispatch]]
            [status-im.utils.handlers :refer [register-handler] :as u]
            [status-im.components.react :as react-comp]
            [status-im.components.status :as status]
            [status-im.models.commands :as commands]
            [status-im.chat.utils :refer [console? not-console?]]
            [clojure.string :as str]
            [status-im.commands.utils :as cu]
            [status-im.utils.phone-number :as pn]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as time]
            [status-im.utils.random :as random]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(defn content-by-command
  [{:keys [type]} content]
  (if (and (= :command type) content)
    (subs content (count cu/command-prefix))
    content))

(defn command-dependent-context-params
  [{:keys [name] :as command}]
  (case name
    "phone" {:suggestions (pn/get-examples)}
    {}))

(defn invoke-suggestions-handler!
  [{:keys [current-chat-id canceled-command] :as db} _]
  (when-not canceled-command
    (let [{:keys [command content params]} (get-in db [:chats current-chat-id :command-input])
          data     (get-in db [:local-storage current-chat-id])
          {:keys [name type bot]} command
          path     [(if (= :command type) :commands :responses)
                    name
                    :params
                    0
                    :suggestions]
          params   {:parameters (or params {})
                    :context    (merge {:data data}
                                       (command-dependent-context-params command))}
          identity (or bot current-chat-id)]
      (dispatch
        [:check-and-load-commands!
         identity
         (fn []
           (status/call-jail
             identity
             path
             params
             #(dispatch [:suggestions-handler {:command command
                                               :content content
                                               :chat-id current-chat-id} %])))]))))

(defn cancel-command!
  [{:keys [canceled-command]}]
  (when canceled-command
    (dispatch [:start-cancel-command])))

(defn current-command
  [{:keys [current-chat-id] :as db} k]
  (get-in db [:chats current-chat-id :command-input :command k]))

(register-handler :set-chat-command-content
  [(after (fn [db]
            (let [trigger (keyword (current-command db :suggestions-trigger))]
              (when (= :on-change trigger)
                (invoke-suggestions-handler! db nil)))))
   (after cancel-command!)
   (after #(dispatch [:clear-validation-errors]))]
  (fn [{:keys [current-chat-id] :as db} [_ content]]
    (let [starts-as-command? (str/starts-with? content cu/command-prefix)
          command?           (= :command (current-command db :type))
          {:keys [parameter-idx command]} (commands/get-command-input db)
          parameter-name     (-> command :params (get parameter-idx) :name)]
      (as-> db db
            (commands/set-chat-command-content db content)
            (commands/set-command-parameter db parameter-name content)
            (assoc-in db [:chats current-chat-id :input-text] nil)
            (assoc db :canceled-command (and command? (not starts-as-command?)))))))

(register-handler :fill-chat-command-content
  (u/side-effect!
    (fn [db [_ content]]
      (let [command? (= :command (current-command db :type))]
        (dispatch
          [:set-chat-command-content
           (if command?
             (str cu/command-prefix content)
             content)])))))

(defn command-input
  ([{:keys [current-chat-id] :as db}]
   (command-input db current-chat-id))
  ([db chat-id]
   (get-in db [:chats chat-id :command-input])))

(register-handler ::validate!
  (u/side-effect!
    (fn [_ [_ command-input {:keys [chat-id handler]} {:keys [error result]}]]
      ;; todo handle error
      (when-not error
        (let [{:keys [errors validationHandler parameters]} (:returned result)]
          (cond errors
                (do
                  (dispatch [:set-chat-ui-props :sending-disabled? false])
                  (dispatch [::add-validation-errors chat-id errors]))

                validationHandler
                (do
                  (dispatch [:set-chat-ui-props :sending-disabled? false])
                  (dispatch [::validation-handler!
                             command-input
                             chat-id
                             validationHandler
                             parameters]))

                :else (if handler
                        (handler)
                        (dispatch [::finish-command-staging command-input chat-id]))))))))

(register-handler :validate-command
  (u/side-effect!
    (fn [{:keys [current-chat-id current-account-id] :as db} [_ command-input command]]
      (let [command-input (or command-input (commands/get-command-input db))
            command       (or command (commands/get-chat-command db))]
        (dispatch [::start-command-validation! {:command-input command-input
                                                :command       command
                                                :chat-id       current-chat-id
                                                :address       current-account-id}])))))

(register-handler ::finish-command-staging
  [(after #(dispatch [:start-cancel-command]))]
  (u/side-effect!
    (fn [db [_ command-input chat-id :as parameters]]
      (let [db           (assoc-in db [:chats chat-id :input-text] nil)
            {:keys [command to-message-id params]} (or command-input (command-input db))
            message-id   (random/id)
            command-info {:command    command
                          :params     params
                          :to-message to-message-id
                          :created-at (time/now-ms)
                          :id         message-id
                          :chat-id    chat-id}
            request-data {:message-id   message-id
                          :chat-id      chat-id
                          :content      {:command (:name command)
                                         :params  params
                                         :type    (:type command)}
                          :on-requested #(dispatch [:send-chat-message command-info])}]
        (dispatch [:set-in [:command->chat (:id command-info)] chat-id])
        (dispatch [:request-command-preview request-data])))))

(defn set-chat-command
  [{:keys [current-chat-id] :as db} [_ command type]]
  (log/debug :set-chat-command command type)
  (-> db
      (commands/set-command-input (or type :commands) command)
      (assoc-in [:chats current-chat-id :command-input :content] cu/command-prefix)
      (assoc :disable-input true)
      (assoc :just-set-command? true)))

(register-handler :set-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:set-soft-input-mode :resize]))
   (after #(dispatch [:command-edit-mode]))]
  set-chat-command)

(defn set-response-command [db [_ to-message-id command-key params]]
  (-> db
      (commands/set-command-input :responses to-message-id command-key params)
      (assoc :canceled-command false)))

(register-handler ::set-response-chat-command
  [(after invoke-suggestions-handler!)
   (after #(dispatch [:command-edit-mode]))
   (after #(dispatch [:set-chat-input-text ""]))]
  set-response-command)

(register-handler :set-response-chat-command
  (u/side-effect!
    (fn [{:keys [current-chat-id] :as db}
         [_ to-message-id command-key params]]
      (when (get-in db [:chats current-chat-id :responses command-key])
        (dispatch [::set-response-chat-command to-message-id command-key params])))))

(register-handler ::add-validation-errors
  (after #(dispatch [:fix-response-height]))
  (fn [db [_ chat-id errors]]
    (assoc-in db [:custom-validation-errors chat-id]
              (map cu/generate-hiccup errors))))

(register-handler :clear-validation-errors
  (fn [db]
    (dissoc db :validation-errors :custom-validation-errors)))

(defn dispatch-error!
  [chat-id title description]
  (letfn [(wrap-params [p] (if (seqable? p) p [p]))]
    (dispatch [::set-validation-error
               chat-id
               {:title       (apply i18n/label (wrap-params title))
                :description (apply i18n/label (wrap-params description))}])))

(def validation-handlers
  {:phone (fn [command-input chat-id [number]]
            (if (pn/valid-mobile-number? number)
              (dispatch [::finish-command-staging command-input chat-id])
              (dispatch-error! chat-id :t/phone-number :t/invalid-phone)))})

(defn validator [name]
  (validation-handlers (keyword name)))

(register-handler ::validation-handler!
  (u/side-effect!
    (fn [_ [_ command-input chat-id name params]]
      (when-let [handler (validator name)]
        (handler command-input chat-id params)))))

(register-handler ::set-validation-error
  (after #(dispatch [:fix-response-height]))
  (fn [db [_ chat-id error]]
    (assoc-in db [:validation-errors chat-id] [error])))

(register-handler :invoke-commands-suggestions!
  (u/side-effect!
    invoke-suggestions-handler!))

(register-handler :send-command!
  (u/side-effect!
    (fn [{:keys [current-chat-id current-account-id] :as db}]
      (let [{:keys [params] :as command} (commands/get-chat-command db)
            {:keys [parameter-idx]} (commands/get-command-input db)

            last-parameter? (= (inc parameter-idx) (count params))

            parameters      {:command command :input command-input}

            {:keys [command content]} (command-input db)
            content'        (content-by-command command content)]
        (dispatch [:set-command-parameter
                   {:value     content'
                    :parameter (params parameter-idx)}])
        (if last-parameter?
          (dispatch [:check-suggestions-trigger! parameters])
          (dispatch [::start-command-validation!
                     {:chat-id current-chat-id
                      :address current-account-id
                      :handler #(dispatch [:next-command-parameter])}]))))))

(register-handler ::start-command-validation!
  (u/side-effect!
    (fn [db [_ {:keys [command-input chat-id address] :as data}]]
      (let [command-input'    (or command-input (commands/get-command-input db))
            {:keys [parameter-idx params command]} command-input'
            {:keys [name type bot]} command
            current-parameter (-> command
                                  :params
                                  (get parameter-idx)
                                  :name)
            to                (get-in db [:contacts chat-id :address])
            context           {:current-parameter current-parameter
                               :from              address
                               :to                to}
            path              [(if (= :command type) :commands :responses)
                               name
                               :validator]
            parameters        {:context    context
                               :parameters params}
            identity          (or bot chat-id)]
        (dispatch
          [:check-and-load-commands!
           identity
           (fn []
             (status/call-jail identity
                               path
                               parameters
                               #(dispatch [::validate! command-input data %])))])))))

(register-handler :request-command-preview
  (u/side-effect!
    (fn [{:keys [chats]} [_ {{:keys [command params content-command type]} :content
               :keys [message-id chat-id on-requested] :as message} data-type]]
      (if-not (get-in chats [chat-id :commands-loaded])
        (do (dispatch [:add-commands-loading-callback
                       chat-id
                       #(dispatch [:request-command-preview message data-type])])
            (dispatch [:load-commands! chat-id]))
        (let [data-type (or data-type :preview)
              path      [(if (= :response (keyword type)) :responses :commands)
                         (if content-command content-command command)
                         data-type]
              params    {:parameters params
                         :context    (merge {:platform platform/platform} i18n/delimeters)}
              callback  #(do (when-let [result (get-in % [:result :returned])]
                               (dispatch [:set-in [:message-data data-type message-id]
                                          (if (string? result)
                                            result
                                            (cu/generate-hiccup result))]))
                             (when on-requested (on-requested %)))]
          (status/call-jail chat-id path params callback))))))

(register-handler :set-command-parameter
  (fn [db [_ {:keys [value parameter]}]]
    (let [name (:name parameter)]
      (commands/set-command-parameter db name value))))

(register-handler :next-command-parameter
  (fn [db _]
    (commands/next-command-parameter db)))

(register-handler :check-suggestions-trigger!
  (u/side-effect!
    (fn [_ [_ {:keys [command]}]]
      (let [suggestions-trigger (keyword (:suggestions-trigger command))]
        (if (= :on-send suggestions-trigger)
          (do
            (dispatch [:invoke-commands-suggestions!])
            (react-comp/dismiss-keyboard!))
          (do
            (dispatch [:set-chat-ui-props :sending-disabled? true])
            (dispatch [:validate-command])))))))

(defn fib-lazy
  ([] (fib-lazy 0 1))
  ([x1 x2] (cons x1 (lazy-seq (fib-lazy x2 (+ x1 x2))))))
