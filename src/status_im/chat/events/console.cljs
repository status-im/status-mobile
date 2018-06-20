(ns status-im.chat.events.console
  (:require [status-im.constants :as constants]
            [status-im.i18n :as i18n]
            [status-im.chat.console :as console-chat]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [goog.string :as gstring]
            goog.string.format))

(defn console-respond-command-messages
  [{:keys [name] :as command} handler-data {:keys [random-id-seq now]}]
  (when-let [messages (case name
                        "js" (let [{:keys [err data messages]} handler-data
                                   content                     (or err data)
                                   message-events              (mapv (fn [{:keys [message type]} id]
                                                                       (console-chat/console-message
                                                                        {:message-id   id
                                                                         :timestamp    now
                                                                         :content      (str type ": " message)
                                                                         :content-type constants/text-content-type}))
                                                                     messages random-id-seq)]
                               (conj message-events
                                     (console-chat/console-message
                                      {:message-id   (first random-id-seq)
                                       :timestamp    now
                                       :content      (str content)
                                       :content-type constants/text-content-type})))
                        (log/debug "ignoring command: " name))]
    {:dispatch [:chat-received-message/add messages]}))

(defn faucet-base-url->url [url]
  (str url "/donate/0x%s"))

(defn- faucet-response-event [now message-id content]
  [:chat-received-message/add
   [(console-chat/console-message
     {:message-id   message-id
      :timestamp    now
      :content      content
      :content-type constants/text-content-type})]])

(def console-commands->fx
  {"faucet"
   (fn [{:keys [db random-id now]} {:keys [params]}]
     (let [current-address (get-in db [:account/account :address])
           faucet-url      (faucet-base-url->url (:url params))]
       {:http-get {:url                   (gstring/format faucet-url current-address)
                   :success-event-creator (fn [_]
                                            (faucet-response-event
                                             now
                                             random-id
                                             (i18n/label :t/faucet-success)))
                   :failure-event-creator (fn [event]
                                            (log/error "Faucet error" event)
                                            (faucet-response-event
                                             now
                                             random-id
                                             (i18n/label :t/faucet-error)))}}))})

(def commands-names (set (keys console-commands->fx)))

(def commands-with-delivery-status
  (disj commands-names "faucet"))
