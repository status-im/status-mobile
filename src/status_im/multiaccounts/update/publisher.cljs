(ns status-im.multiaccounts.update.publisher
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.waku.core :as waku]
            [taoensso.timbre :as log]))

;; Publish updates every 48 hours
(def publish-updates-interval (* 48 60 60 1000))

(defn publish-update! [{:keys [db now] :as cofx}]
  (let [peers-count (:peers-count db)
        last-updated (get-in
                      db
                      [:multiaccount :last-updated])]
    (when (and (pos? peers-count)
               (pos? last-updated)
               (< publish-updates-interval
                  (- now last-updated)))
      (let [multiaccount (:multiaccount db)
            {:keys [name preferred-name photo-path]} multiaccount]

        (log/debug "sending contact updates")
        (json-rpc/call {:method (json-rpc/call-ext-method (waku/enabled? cofx) "sendContactUpdates")
                        :params [(or preferred-name name) photo-path]
                        :on-failure #(do
                                       (log/warn "failed to send contact updates")
                                       (re-frame/dispatch [:multiaccounts.update.callback/failed-to-publish]))
                        :on-success #(do
                                       (log/debug "sent contact updates")
                                       (re-frame/dispatch [:multiaccounts.update.callback/published]))})))))
