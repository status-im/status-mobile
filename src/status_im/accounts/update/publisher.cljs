(ns status-im.accounts.update.publisher
  (:require [status-im.constants :as constants]
            [status-im.accounts.update.core :as accounts]
            [status-im.pairing.core :as pairing]
            [status-im.data-store.accounts :as accounts-store]
            [status-im.transport.shh :as shh]
            [status-im.utils.fx :as fx]))

;; Publish updates every 48 hours
(def publish-updates-interval (* 48 60 60 1000))

(defn publish-update! [{:keys [db now web3]}]
  (let [my-public-key (get-in db [:account/account :public-key])
        peers-count (:peers-count db)
        last-updated (get-in
                      db
                      [:account/account :last-updated])]
    (when (and (pos? peers-count)
               (pos? last-updated)
               (< publish-updates-interval
                  (- now last-updated)))
      (let [public-keys  (accounts/contact-public-keys {:db db})
            payload      (accounts/account-update-message {:db db})
            sync-message (pairing/sync-installation-account-message {:db db})]
        (doseq [pk public-keys]
          (shh/send-direct-message!
           web3
           {:pubKey pk
            :sig my-public-key
            :chat constants/contact-discovery
            :payload payload}
           [:accounts.update.callback/published]
           [:accounts.update.callback/failed-to-publish]
           1))
        (shh/send-direct-message!
         web3
         {:pubKey my-public-key
          :sig my-public-key
          :chat constants/contact-discovery
          :payload sync-message}
         [:accounts.update.callback/published]
         [:accounts.update.callback/failed-to-publish]
         1)))))
