(ns status-im.chat.handlers.send-message
  (:require [status-im.utils.handlers :refer [register-handler] :as u]
            [clojure.string :as s]
            [status-im.data-store.messages :as messages]
            [status-im.components.status :as status]
            [status-im.utils.random :as random]
            [status-im.utils.datetime :as time]
            [re-frame.core :refer [enrich after dispatch path]]
            [status-im.chat.utils :as cu]
            [status-im.constants :refer [text-content-type
                                         content-type-command
                                         content-type-command-request
                                         default-number-of-messages]]
            [status-im.utils.datetime :as datetime]
            [status-im.protocol.core :as protocol]
            [taoensso.timbre :refer-macros [debug] :as log]
            [status-im.constants :refer [console-chat-id]]
            [status-im.chat.handlers.console :as console]))

(defn prepare-command
  [identity chat-id clock-value
   {:keys [id preview preview-string params command to-message handler-data]}]
  (let [content {:command (command :name)
                 :params  params}]
    {:message-id       id
     :from             identity
     :to               chat-id
     :timestamp        (time/now-ms)
     :content          (assoc content :preview preview-string
                                      :handler-data handler-data)
     :content-type     content-type-command
     :outgoing         true
     :preview          preview-string
     :rendered-preview preview
     :to-message       to-message
     :type             (:type command)
     :has-handler      (:has-handler command)
     :clock-value      (inc clock-value)}))

(register-handler :send-chat-message
  (u/side-effect!
    (fn [{:keys [current-chat-id current-public-key current-account-id] :as db}]
      (let [staged-commands (vals (get-in db [:chats current-chat-id :staged-commands]))
            text            (get-in db [:chats current-chat-id :input-text])
            data            {:commands staged-commands
                             :message  text
                             :chat-id  current-chat-id
                             :identity current-public-key
                             :address  current-account-id}]
        (dispatch [:clear-input current-chat-id])
        (cond
          (seq staged-commands)
          (dispatch [::check-commands-handlers! data])
          (not (s/blank? text))
          (dispatch [::prepare-message data]))))))

(defn console-command? [chat-id command-name]
  (and (= console-chat-id chat-id)
       (console/commands-names (keyword command-name))))

(register-handler ::check-commands-handlers!
  (u/side-effect!
    (fn [_ [_ {:keys [commands message chat-id] :as params}]]
      (doseq [{:keys [command] :as message} commands]
        (let [params' (assoc params :staged-command message)
              command-name (:name (:command message))]
          (if (:sent-to-jail? message)
            ;; todo there could be other reasons for "long-running"
            ;; hanling of the command besides sendTransaction
            (dispatch [:navigate-to :confirm])
            (cond
              (console-command? chat-id command-name)
              (dispatch [:invoke-console-command-handler! params'])

              (:has-handler command)
              (dispatch [::invoke-command-handlers! params'])

              :else
              (dispatch [:prepare-command! params'])))))
      (when-not (s/blank? message)
        (dispatch [::prepare-message params])))))

(register-handler :clear-input
  (path :chats)
  (fn [db [_ chat-id]]
    (assoc-in db [chat-id :input-text] nil)))

(register-handler :prepare-command!
  (u/side-effect!
    (fn [{:keys [current-public-key] :as db}
         [_ {:keys [chat-id staged-command handler-data] :as params}]]
      (let [{:keys [clock-value]} (get-in db [:chats chat-id])
            command' (->> (assoc staged-command :handler-data handler-data)
                          (prepare-command current-public-key chat-id clock-value)
                          (cu/check-author-direction db chat-id))]
        (dispatch [:clear-command chat-id (:id staged-command)])
        (dispatch [::send-command! (assoc params :command command')])))))

(register-handler :clear-command
  (fn [db [_ chat-id id]]
    (if chat-id
      (update-in db [:chats chat-id :staged-commands] dissoc id)
      db)))

(register-handler ::send-command!
  (u/side-effect!
    (fn [_ [_ params]]
      (dispatch [::add-command params])
      (dispatch [::save-command! params])
      (dispatch [::dispatch-responded-requests! params])
      (dispatch [::send-command-protocol! params]))))

(register-handler ::add-command
  (after (fn [_ [_ {:keys [handler]}]]
           (when handler (handler))))
  (fn [db [_ {:keys [chat-id command]}]]
    (cu/add-message-to-db db chat-id command)))

(register-handler ::save-command!
  (u/side-effect!
    (fn [_ [_ {:keys [command chat-id]}]]
      (messages/save
        chat-id
        (dissoc command :rendered-preview :to-message :has-handler)))))

(register-handler ::dispatch-responded-requests!
  (u/side-effect!
    (fn [_ [_ {:keys [command chat-id]}]]
      (let [{:keys [to-message]} command]
        (when to-message
          (dispatch [:request-answered! chat-id to-message]))))))

(register-handler ::invoke-command-handlers!
  (u/side-effect!
    (fn [db [_ {:keys [chat-id address staged-command]
                :as   parameters}]]
      (let [{:keys [id command params]} staged-command
            {:keys [type name]} command
            path   [(if (= :command type) :commands :responses)
                    name
                    :handler]
            to     (get-in db [:contacts chat-id :address])
            params {:parameters params
                    :context    {:from       address
                                 :to         to
                                 :message-id id}}]
        (dispatch [::command-in-processing chat-id id])
        (status/call-jail
          chat-id
          path
          params
          #(dispatch [:command-handler! chat-id parameters %]))))))

(register-handler ::command-in-processing
  (fn [db [_ chat-id id]]
    (assoc-in db [:chats chat-id :staged-commands id :sent-to-jail?] true)))

(register-handler ::prepare-message
  (u/side-effect!
    (fn [db [_ {:keys [chat-id identity message] :as params}]]
      (let [{:keys [group-chat clock-value]} (get-in db [:chats chat-id])
            message'  (cu/check-author-direction
                        db chat-id
                        {:message-id   (random/id)
                         :chat-id      chat-id
                         :content      message
                         :from         identity
                         :content-type text-content-type
                         :outgoing     true
                         :timestamp    (time/now-ms)
                         :clock-value  (inc clock-value)})
            message'' (if group-chat
                        (assoc message' :group-id chat-id :message-type :group-user-message)
                        (assoc message' :to chat-id :message-type :user-message))
            params'   (assoc params :message message'')]
        (dispatch [::add-message params'])
        (dispatch [::save-message! params'])))))

(register-handler ::add-message
  (fn [db [_ {:keys [chat-id message]}]]
    (cu/add-message-to-db db chat-id message)))

(register-handler ::save-message!
  (after (fn [_ [_ params]]
           (dispatch [::send-message! params])))
  (u/side-effect!
    (fn [_ [_ {:keys [chat-id message]}]]
      (messages/save chat-id message))))

(register-handler ::send-message!
  (u/side-effect!
    (fn [{:keys [web3 chats]} [_ {{:keys [message-type]
                                   :as   message} :message
                                  chat-id         :chat-id}]]
      (when (and message (cu/not-console? chat-id))
        (let [message' (select-keys message [:from :message-id])
              payload  (select-keys message [:timestamp :content :content-type :clock-value])
              options  {:web3    web3
                        :message (assoc message' :payload payload)}]
          (if (= message-type :group-user-message)
            (let [{:keys [public-key private-key]} (chats chat-id)]
              (protocol/send-group-message! (assoc options
                                              :group-id chat-id
                                              :keypair {:public  public-key
                                                        :private private-key})))
            (protocol/send-message! (assoc-in options
                                              [:message :to] (:to message))))
          (dispatch [:inc-clock chat-id]))))))

(register-handler ::send-command-protocol!
  (u/side-effect!
    (fn [{:keys [web3 current-public-key chats] :as db} [_ {:keys [chat-id command]}]]
      (let [{:keys [content message-id clock-value]} command]
        (when (cu/not-console? chat-id)
          (let [{:keys [public-key private-key]} (chats chat-id)
                {:keys [group-chat]} (get-in db [:chats chat-id])
                payload {:content      content
                         :content-type content-type-command
                         :timestamp    (datetime/now-ms)
                         :clock-value  clock-value}
                options {:web3    web3
                         :message {:from        current-public-key
                                   :message-id  message-id
                                   :payload     payload}}]
            (if group-chat
              (protocol/send-group-message! (assoc options
                                              :group-id chat-id
                                              :keypair {:public  public-key
                                                        :private private-key}))
              (protocol/send-message! (assoc-in options
                                                [:message :to] chat-id)))
            (dispatch [:inc-clock chat-id])))))))
