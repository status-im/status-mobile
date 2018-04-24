(ns status-im.chat.events.console
  (:require [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.chat.console :as console-chat]
            [status-im.ui.screens.accounts.utils :as accounts-utils]
            [status-im.chat.utils :as chat.utils]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [goog.string :as gstring]
            goog.string.format
            [status-im.utils.handlers-macro :as handlers-macro]))

;;;; Helper fns

(defn console-respond-command-messages
  [{:keys [name] :as command} handler-data random-id-seq]
  (when command
    (case name
      "js" (let [{:keys [err data messages]} handler-data
                 content                     (or err data)
                 message-events              (mapv (fn [{:keys [message type]} id]
                                                     (console-chat/console-message
                                                      {:message-id id
                                                       :content (str type ": " message)
                                                       :content-type constants/text-content-type}))
                                                   messages random-id-seq)]
             (conj message-events
                   (console-chat/console-message
                    {:message-id   (first random-id-seq)
                     :content      (str content)
                     :content-type constants/text-content-type})))
      (log/debug "ignoring command: " name))))

(def console-commands->fx
  {"faucet"
   (fn [{:keys [db random-id] :as cofx} {:keys [params]}]
     (let [{:accounts/keys [accounts current-account-id]} db
           current-address (get-in accounts [current-account-id :address])
           faucet-url (chat.utils/faucet-base-url->url (:url params))]
       {:http-get {:url (gstring/format faucet-url current-address)
                   :success-event-creator (fn [_]
                                            (chat.utils/faucet-response-event
                                             random-id
                                             (i18n/label :t/faucet-success)))
                   :failure-event-creator (fn [event]
                                            (log/error "Faucet error" event)
                                            (chat.utils/faucet-response-event
                                             random-id
                                             (i18n/label :t/faucet-error)))}}))

   "debug"
   (fn [{:keys [db random-id now] :as cofx} {:keys [params]}]
     (let [debug? (= "On" (:mode params))]
       (handlers-macro/merge-fx cofx
                                {:dispatch-n (if debug?
                                         [[:initialize-debugging {:force-start? true}]
                                          [:chat-received-message/add
                                           (console-chat/console-message
                                            {:message-id random-id
                                             :content (i18n/label :t/debug-enabled)
                                             :content-type constants/text-content-type})]]
                                         [[:stop-debugging]])}
                                (accounts-utils/account-update {:debug?  debug?
                                                           :last-updated now}))))})

(def commands-names (set (keys console-commands->fx)))

(def commands-with-delivery-status
  (disj commands-names "faucet" "debug"))
