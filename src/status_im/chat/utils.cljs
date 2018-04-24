(ns status-im.chat.utils
  (:require
   [status-im.ui.screens.accounts.utils :as accounts.utils]
   [status-im.utils.handlers-macro :as handlers-macro]
   [status-im.i18n :as i18n]
   [goog.string :as gstring]
   goog.string.format
   [taoensso.timbre :as log]
   [status-im.constants :as constants]
   [status-im.chat.console :as console-chat]))

(defn faucet-base-url->url [url]
  (str url "/donate/0x%s"))

(defn- faucet-response-event [message-id content]
  [:chat-received-message/add
   (console-chat/console-message {:message-id   message-id
                                  :content      content
                                  :content-type constants/text-content-type})])

(def console-commands->fx
  {"faucet"
   (fn [{:keys [db random-id] :as cofx} {:keys [params]}]
     (let [{:accounts/keys [accounts current-account-id]} db
           current-address (get-in accounts [current-account-id :address])
           faucet-url      (faucet-base-url->url (:url params))]
       {:http-get {:url                   (gstring/format faucet-url current-address)
                   :success-event-creator (fn [_]
                                            (faucet-response-event
                                             random-id
                                             (i18n/label :t/faucet-success)))
                   :failure-event-creator (fn [event]
                                            (log/error "Faucet error" event)
                                            (faucet-response-event
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
                                                  {:message-id   random-id
                                                   :content      (i18n/label :t/debug-enabled)
                                                   :content-type constants/text-content-type})]]
                                               [[:stop-debugging]])}
                                (accounts.utils/account-update {:debug?       debug?
                                                                :last-updated now}))))})

(def commands-names (set (keys console-commands->fx)))

(def commands-with-delivery-status
  (disj commands-names "faucet" "debug"))
