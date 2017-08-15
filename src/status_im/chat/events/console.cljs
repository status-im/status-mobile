(ns status-im.chat.events.console
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]
            [status-im.constants :as const]
            [status-im.i18n :as i18n]
            [status-im.chat.events.sign-up :as sign-up-events]
            [status-im.ui.screens.accounts.events :as accounts-events]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [goog.string :as gstring]
            goog.string.format))

;;;; Helper fns

(defn console-respond-command-events
  [command random-id-seq]
  (let [{:keys [command handler-data]} command]
    (when command
      (let [{:keys [name]} command]
        (case name
          "js" (let [{:keys [err data messages]} handler-data
                     content                     (or err data)
                     message-events              (mapv (fn [{:keys [message type]} id]
                                                         [:received-message
                                                          {:message-id   id
                                                           :content      (str type ": " message)
                                                           :content-type const/text-content-type
                                                           :outgoing     false
                                                           :chat-id      const/console-chat-id
                                                           :from         const/console-chat-id
                                                           :to           "me"}])
                                                       messages random-id-seq)]
                 (conj message-events
                       [:received-message
                        {:message-id   (first random-id-seq)
                         :content      (str content)
                         :content-type const/text-content-type
                         :outgoing     false
                         :chat-id      const/console-chat-id
                         :from         const/console-chat-id
                         :to           "me"}]))
          (log/debug "ignoring command: " command))))))

(def faucet-base-url->url
  {"http://faucet.ropsten.be:3001" "http://faucet.ropsten.be:3001/donate/0x%s"
   "http://46.101.129.137:3001"    "http://46.101.129.137:3001/donate/0x%s"})

(defn- faucet-response-event [message-id content]
  [:received-message
   {:message-id   message-id
    :content      content
    :content-type const/text-content-type
    :outgoing     false
    :chat-id      const/console-chat-id
    :from         const/console-chat-id
    :to           "me"}])

(def console-commands->fx
  {"password"
   (fn [{:keys [db]} {:keys [params]}]
     (accounts-events/create-account db (:password params)))

   "phone"
   (fn [{:keys [db]} {:keys [params id]}]
     (-> db
         (sign-up-events/sign-up (:phone params) id)
         (as-> fx
             (assoc fx :dispatch-n [(:dispatch fx)]))
         (dissoc :dispatch)))

   "confirmation-code"
   (fn [{:keys [db]} {:keys [params id]}]
     (sign-up-events/sign-up-confirm db (:code params) id))

   "faucet"
   (fn [{:keys [db random-id]} {:keys [params id]}]
     (let [{:accounts/keys [accounts current-account-id]} db
           current-address (get-in accounts [current-account-id :address])
           faucet-url (get faucet-base-url->url (:url params))]
       {:http-get {:url (gstring/format faucet-url current-address)
                   :success-event-creator (fn [_]
                                            (faucet-response-event
                                             random-id
                                             (i18n/label :t/faucet-success)))
                   :failure-event-creator (fn [_]
                                            (faucet-response-event
                                             random-id
                                             (i18n/label :t/faucet-error)))}}))

   "debug"
   (fn [{:keys [random-id] :as cofx} {:keys [params id]}]
     (let [debug? (= "On" (:mode params))
           fx (accounts-events/account-update cofx {:debug? debug?})]
       (assoc fx :dispatch-n (if debug?
                               [[:debug-server-start]
                                [:received-message
                                 {:message-id   random-id
                                  :content      (i18n/label :t/debug-enabled)
                                  :content-type const/text-content-type
                                  :outgoing     false
                                  :chat-id      const/console-chat-id
                                  :from         const/console-chat-id
                                  :to           "me"}]]
                               [[:debug-server-stop]]))))})

(def commands-names (set (keys console-commands->fx)))

(def commands-with-delivery-status
  (disj commands-names "password" "faucet" "debug"))

;;;; Handlers

;; TODO(janherich) remove this once send-message events are refactored
(handlers/register-handler-fx
  :invoke-console-command-handler!
  [re-frame/trim-v (re-frame/inject-cofx :random-id) (re-frame/inject-cofx :now)]
  (fn [cofx [{:keys [chat-id command] :as command-params}]]
    (let [fx-fn (get console-commands->fx (-> command :command :name))]
      (-> cofx
          (fx-fn command)
          (update :dispatch-n (fnil conj []) [:prepare-command! chat-id command-params])))))

;; TODO(janherich) remove this once send-message events are refactored
(handlers/register-handler-fx
  :console-respond-command
  [(re-frame/inject-cofx :random-id-seq) re-frame/trim-v]
  (fn [{:keys [random-id-seq]} [command]]
    (when-let [events (console-respond-command-events command random-id-seq)]
      {:dispatch-n events})))
