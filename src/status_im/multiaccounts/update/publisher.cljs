(ns status-im.multiaccounts.update.publisher
  (:require [status-im.constants :as constants]
            [status-im.multiaccounts.update.core :as multiaccounts]
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
      (let [public-keys  (multiaccounts/contact-public-keys {:db db})
            payload      (multiaccounts/multiaccount-update-message {:db db})
            sync-message (pairing/sync-installation-multiaccount-message {:db db})]
        (doseq [pk public-keys]
          (shh/send-direct-message! {:pubKey pk
                                     :sig my-public-key
                                     :chat constants/contact-discovery
                                     :payload payload}
                                    [:multiaccounts.update.callback/published]
                                    [:multiaccounts.update.callback/failed-to-publish]
                                    1))
        (shh/send-direct-message! {:pubKey my-public-key
                                   :sig my-public-key
                                   :chat constants/contact-discovery
                                   :payload sync-message}
                                  [:multiaccounts.update.callback/published]
                                  [:multiaccounts.update.callback/failed-to-publish]
                                  1)))))
