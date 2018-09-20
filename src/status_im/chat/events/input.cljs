(ns status-im.chat.events.input
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.constants :as constants]
            [status-im.chat.constants :as chat-constants]
            [status-im.chat.models :as model]
            [status-im.chat.models.input :as input-model]
            [status-im.chat.models.message :as message-model]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.input :as commands-input]
            [status-im.chat.commands.sending :as commands-sending]
            [status-im.ui.components.react :as react-comp]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.handlers-macro :as handlers-macro]))

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

(defn chat-input-focus
  "Returns fx for focusing on active chat input reference"
  [ref {{:keys [current-chat-id chat-ui-props]} :db}]
  (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
    {::focus-rn-component cmp-ref}))

;;;; Handlers

(handlers/register-handler-fx
 :set-chat-input-text
 (fn [cofx [_ text]]
   (input-model/set-chat-input-text text cofx)))

(handlers/register-handler-fx
 :select-chat-input-command
 (fn [{:keys [db] :as cofx} [_ command params metadata]]
   (handlers-macro/merge-fx cofx
                            (input-model/set-chat-input-metadata metadata)
                            (commands-input/select-chat-input-command command params)
                            (chat-input-focus :input-ref))))

(handlers/register-handler-fx
 :set-command-parameter
 (fn [cofx [_ last-param? index value]]
   (commands-input/set-command-parameter last-param? index value cofx)))

(handlers/register-handler-fx
 :chat-input-focus
 (fn [cofx [_ ref]]
   (chat-input-focus ref cofx)))

(handlers/register-handler-fx
 :chat-input-blur
 (fn [{{:keys [current-chat-id chat-ui-props]} :db} [_ ref]]
   (when-let [cmp-ref (get-in chat-ui-props [current-chat-id ref])]
     {::blur-rn-component cmp-ref})))

(defn command-complete-fx
  "command is complete, set `:sending-in-progress?` flag and proceed with command processing"
  [input-text command {:keys [db now random-id] :as cofx}]
  (handlers-macro/merge-fx
   cofx
   {:db (model/set-chat-ui-props db {:sending-in-progress? true})}
   (commands-sending/validate-and-send input-text command)))

(defn command-not-complete-fx
  "command is not complete, just add space after command if necessary"
  [input-text current-chat-id {:keys [db]}]
  {:db (cond-> db
         (not (input-model/text-ends-with-space? input-text))
         (assoc-in [:chats current-chat-id :input-text]
                   (str input-text chat-constants/spacing-char)))})

(defn plain-text-message-fx
  "no command detected, when not empty, proceed by sending text message without command processing"
  [input-text current-chat-id {:keys [db] :as cofx}]
  (when-not (string/blank? input-text)
    (handlers-macro/merge-fx
     cofx
     (input-model/set-chat-input-text nil)
     (input-model/set-chat-input-metadata nil)
     (message-model/send-message {:chat-id      current-chat-id
                                  :content-type constants/text-content-type
                                  :content      input-text}))))

(handlers/register-handler-fx
 :send-current-message
 message-model/send-interceptors
 (fn [{{:keys [current-chat-id id->command access-scope->command-id] :as db} :db :as cofx} _]
   (when-not (get-in db [:chat-ui-props current-chat-id :sending-in-progress?])
     (let [input-text   (get-in db [:chats current-chat-id :input-text])
           command      (commands-input/selected-chat-command
                         input-text nil (commands/chat-commands id->command
                                                                access-scope->command-id
                                                                (get-in db [:chats current-chat-id])))]
       (if command
          ;; Returns true if current input contains command
         (if (= :complete (:command-completion command))
           (command-complete-fx input-text command cofx)
           (command-not-complete-fx input-text current-chat-id cofx))
         (plain-text-message-fx input-text current-chat-id cofx))))))

(handlers/register-handler-fx
 :update-text-selection
 (fn [{:keys [db]} [_ selection]]
   {:db (model/set-chat-ui-props db {:selection selection})}))

(handlers/register-handler-fx
 :show-suggestions
 (fn [{:keys [db]} _]
   {:db (-> db
            (model/toggle-chat-ui-prop :show-suggestions?)
            (model/set-chat-ui-props {:validation-messages nil}))}))
