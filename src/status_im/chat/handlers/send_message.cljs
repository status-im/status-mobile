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
            [taoensso.timbre :refer-macros [debug]]))

(defn prepare-command
  [identity chat-id {:keys [preview preview-string content command to-message]}]
  (let [content {:command (command :name)
                 :content content}]
    {:message-id       (random/id)
     :from             identity
     :to               chat-id
     :content          (assoc content :preview preview-string)
     :content-type     content-type-command
     :outgoing         true
     :preview          preview-string
     :rendered-preview preview
     :to-message       to-message
     :type             (:type command)
     :has-handler      (:has-handler command)}))

(register-handler :send-chat-message
  (u/side-effect!
    (fn [{:keys [current-chat-id current-public-key current-account-id] :as db}]
      (let [staged-commands (vals (get-in db [:chats current-chat-id :staged-commands]))
            text (get-in db [:chats current-chat-id :input-text])
            data {:commands staged-commands
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


(register-handler ::check-commands-handlers!
  (u/side-effect!
    (fn [_ [_ {:keys [commands message] :as params}]]
      (doseq [{:keys [command] :as message} commands]
        (let [params' (assoc params :command message)]
          (if (:sending message)
            (dispatch [:navigate-to :confirm])
            (if (:has-handler command)
              (dispatch [::invoke-command-handlers! params'])
              (dispatch [:prepare-command! params'])))))
      (when-not (s/blank? message)
        (dispatch [::prepare-message params])))))

(register-handler :clear-input
  (path :chats)
  (fn [db [_ chat-id]]
    (assoc-in db [chat-id :input-text] nil)))

(register-handler :prepare-command!
  (u/side-effect!
    (fn [{:keys [current-public-key] :as db} [_ {:keys [chat-id command] :as params}]]
      (let [command' (->> command
                          (prepare-command current-public-key chat-id)
                          (cu/check-author-direction db chat-id))]
        (dispatch [::clear-command chat-id (:id command)])
        (dispatch [::send-command! (assoc params :command command')])))))

(register-handler ::clear-command
  (fn [db [_ chat-id id]]
    (update-in db [:chats chat-id :staged-commands] dissoc id)))

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
    (fn [{:keys [current-public-key]} [_ {:keys [command chat-id]}]]
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
    (fn [db [_ {:keys [chat-id address] :as parameters}]]
      (let [{:keys [command content]} (:command parameters)
            {:keys [type name]} command
            path [(if (= :command type) :commands :responses)
                  name
                  :handler]
            to (get-in db [:contacts chat-id :address])
            params {:value   content
                    :command {:from address
                              :to   to}}]
        (status/call-jail chat-id
                          path
                          params
                          #(dispatch [:command-handler! chat-id parameters %]))))))

(register-handler ::prepare-message
  (u/side-effect!
    (fn [db [_ {:keys [chat-id identity message] :as params}]]
      (let [{:keys [group-chat]} (get-in db [:chats chat-id])
            message' (cu/check-author-direction
                       db chat-id
                       {:message-id      (random/id)
                        :chat-id         chat-id
                        :content         message
                        :from            identity
                        :content-type    text-content-type
                        :outgoing        true
                        :timestamp       (time/now-ms)})
            message'' (if group-chat
                        (assoc message' :group-id chat-id :message-type :group-user-message)
                        (assoc message' :to chat-id :message-type :user-message))
            params' (assoc params :message message'')]
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
              payload (select-keys message [:timestamp :content :content-type])
              options {:web3    web3
                       :message (assoc message' :payload payload)}]
          (if (= message-type :group-user-message)
            (let [{:keys [public-key private-key]} (chats chat-id)]
              (protocol/send-group-message! (assoc options
                                              :group-id chat-id
                                              :keypair {:public  public-key
                                                        :private private-key})))
            (protocol/send-message! (assoc-in options
                                              [:message :to] (:to message)))))))))

(register-handler ::send-command-protocol!
  (u/side-effect!
    (fn [{:keys [web3 current-public-key chats] :as db} [_ {:keys [chat-id command]}]]
      (let [{:keys [content message-id]} command]
        (when (cu/not-console? chat-id)
          (let [{:keys [public-key private-key]} (chats chat-id)
                {:keys [group-chat]} (get-in db [:chats chat-id])
                payload {:content      content
                         :content-type content-type-command
                         :timestamp    (datetime/now-ms)}
                options {:web3    web3
                         :message {:from       current-public-key
                                   :message-id message-id
                                   :payload    payload}}]
            (if group-chat
              (protocol/send-group-message! (assoc options
                                              :group-id chat-id
                                              :keypair {:public  public-key
                                                        :private private-key}))
              (protocol/send-message! (assoc-in options
                                                [:message :to] chat-id)))))))))
