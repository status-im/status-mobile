(ns status-im.multiaccounts.update.publisher
  (:require [taoensso.timbre :as log]
            [status-im.constants :as constants]
            [status-im.multiaccounts.update.core :as multiaccounts]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.pairing.core :as pairing]
            [status-im.transport.shh :as shh]))

;; Publish updates every 48 hours
(def publish-updates-interval (* 48 60 60 1000))

(defn publish-update! [{:keys [db now]}]
  (let [my-public-key (get-in db [:multiaccount :public-key])
        peers-count (:peers-count db)
        last-updated (get-in
                      db
                      [:multiaccount :last-updated])]
    (when (and (pos? peers-count)
               (pos? last-updated)
               (< publish-updates-interval
                  (- now last-updated)))
      (let [multiaccount (:multiaccount db)
            {:keys [name preferred-name photo-path address]} multiaccount]

        (log/debug "sending contact updates")
        (json-rpc/call {:method "shhext_sendContactUpdates"
                        :params [(or preferred-name name) photo-path]
                        :on-success #(log/debug "sent contact updates")})))))
