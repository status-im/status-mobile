(ns status-im.chat.events.sign-up
  (:require [re-frame.core :as re-frame]
            [status-im.constants :as const]
            [status-im.chat.console :as console-chat]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.phone-number :as phone-number-util]
            [status-im.utils.sms-listener :as sms-listener]
            [status-im.ui.screens.accounts.events :as accounts-events]
            [status-im.ui.screens.contacts.events :as contacts-events]
            [taoensso.timbre :as log]))

;;;; Helpers fns

(defn sign-up
  "Creates effects for signing up"
  [db phone-number message-id]
  (let [current-account-id (:accounts/current-account-id db)
        {:keys [public-key address]} (get-in db [:accounts/accounts current-account-id])]
    {:http-post {:action                "sign-up"
                 :data                  {:phone-number     (phone-number-util/format-phone-number phone-number)
                                         :whisper-identity public-key
                                         :address          address}
                 :success-event-creator (fn [_]
                                          [::sign-up-success message-id])
                 :failure-event-creator (fn [_]
                                          [::http-request-failure [::sign-up phone-number message-id]])}}))

(defn sign-up-confirm
  "Creates effects for sign-up confirmation"
  [db confirmation-code message-id]
  {:http-post {:action                "sign-up-confirm"
               :data                  {:code confirmation-code}
               :success-event-creator (fn [body]
                                        [::sign-up-confirm-response body message-id])
               :failure-event-creator (fn [_]
                                        [::http-request-failure [::sign-up-confirm confirmation-code message-id]])}})

;;;; Handlers

(handlers/register-handler-fx
  ::sign-up
  [re-frame/trim-v]
  (fn [{:keys [db]} [phone-number message-id]]
    (sign-up db phone-number message-id)))

(defn- message-seen [{:keys [db] :as fx} message-id]
  (let [statuses-path [:chats const/console-chat-id :messages message-id :user-statuses]
        statuses      (-> (get-in db statuses-path)
                          (assoc const/console-chat-id :seen))]
    (-> fx
        (assoc-in (into [:db] statuses-path) statuses)
        (assoc :update-message {:message-id     message-id
                                :user-statuses  statuses}))))

(handlers/register-handler-fx
  :start-listening-confirmation-code-sms
  [re-frame/trim-v]
  (fn [{:keys [db]} [sms-listener]]
    {:db (if-not (:confirmation-code-sms-listener db)
           (assoc db :confirmation-code-sms-listener sms-listener)
           db)}))

(defn stop-listening-confirmation-code-sms [{:keys [db] :as fx}]
  (-> fx
      (update :db dissoc :confirmation-code-sms-listener)
      (assoc ::remove-sms-listener (:confirmation-code-sms-listener db))))

(re-frame/reg-fx
  ::remove-sms-listener
  (fn [subscription]
    (sms-listener/remove-sms-listener subscription)))

(defn- sms-receive-handler [{confirmation-code :body}]
  (when-let [matches (re-matches #"(\d{4})" confirmation-code)]
    (re-frame/dispatch [::sign-up-confirm (second matches)])))

(def start-listening-confirmation-code-sms-event
  [:request-permissions
   [:receive-sms]
   (fn []
     (let [listener (sms-listener/add-sms-listener sms-receive-handler)]
       (re-frame/dispatch [:start-listening-confirmation-code-sms listener])))])

(handlers/register-handler-fx
  ::sign-up-success
  [re-frame/trim-v (re-frame/inject-cofx :random-id)]
  (fn [{:keys [db random-id]} [message-id]]
    (-> {:db         db
         :dispatch-n [;; create manual way for entering confirmation code
                      [:chat-received-message/add console-chat/enter-confirmation-code-message]
                      ;; create automatic way for receiving confirmation code
                      start-listening-confirmation-code-sms-event]}
        (message-seen message-id))))

(defn- extract-last-phone-number [chats]
  (let [phone-message (->> (get-in chats [const/console-chat-id :messages])
                           (map second)
                           (some (fn [{:keys [type content] :as message}]
                                   (when (and (= type :response)
                                              (= (:command content) "phone"))
                                     message))))]
    (get-in phone-message [:content :params :phone])))

(handlers/register-handler-fx
  ::sign-up-confirm
  (fn [{:keys [db]} [confirmation-code message-id]]
    (sign-up-confirm db confirmation-code message-id)))

(defn- sign-up-confirmed [{:keys [db] :as fx} now]
  (let [last-phone-number (extract-last-phone-number (:chats db))
        fx (-> (stop-listening-confirmation-code-sms fx)
               (update :dispatch-n conj
                       [:request-permissions [:read-contacts]
                        #(re-frame/dispatch [:sync-contacts (fn [contacts]
                                                              [::contacts-synced contacts])])]))]
    (cond-> fx
            last-phone-number (accounts-events/account-update {:phone        last-phone-number
                                                               :last-updated now}))))

(handlers/register-handler-fx
  ::sign-up-confirm-response
  [re-frame/trim-v (re-frame/inject-cofx :random-id)]
  (fn [{:keys [db random-id now]} [{:keys [message status]} message-id]]
    (let [messages (cond-> []

                           true
                           (conj (console-chat/console-message {:content      message
                                                                :content-type const/text-content-type}))

                           (= "failed" status)
                           (conj console-chat/incorrect-confirmation-code-message))]

      (cond-> {:db         db
               :dispatch-n (mapv #(vector :chat-received-message/add %) messages)}

              message-id
              (message-seen message-id)

              (= "confirmed" status)
              (sign-up-confirmed now)))))

(handlers/register-handler-fx
  ::contacts-synced
  [re-frame/trim-v (re-frame/inject-cofx :random-id)]
  (fn [{:keys [db random-id now] :as cofx} [contacts]]
    (-> {:db db}
        (accounts-events/account-update {:signed-up?   true
                                         :last-updated now})
        (assoc :dispatch [:chat-received-message/add console-chat/contacts-synchronised-message]))))

(handlers/register-handler-fx
  ::http-request-failure
  [re-frame/trim-v]
  (fn [_ [original-event-vector]]
    ;; TODO(janherich): in case of http request failure, we will try to hit http endpoint in loop forever,
    ;; maybe it's better to cut it after N tries and display error message with explanation to user
    {:dispatch-later [{:ms 1000 :dispatch original-event-vector}]}))
