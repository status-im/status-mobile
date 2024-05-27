(ns status-im.contexts.wallet.wallet-connect.effects
  (:require [promesa.core :as promesa]
            [re-frame.core :as rf]
            [react-native.wallet-connect :as wallet-connect]
            [status-im.config :as config]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]))

(rf/reg-fx
 :effects.wallet-connect/init
 (fn [{:keys [on-success on-fail]}]
   (let
     [project-id config/WALLET_CONNECT_PROJECT_ID
      metadata   {:name        (i18n/label :t/status)
                  :description (i18n/label :t/status-is-a-secure-messaging-app)
                  :url         constants/wallet-connect-metadata-url
                  :icons       [constants/wallet-connect-metadata-icon]}]
     (-> (wallet-connect/init project-id metadata)
         (promesa/then on-success)
         (promesa/catch on-fail)))))

(rf/reg-fx
 :effects.wallet-connect/register-event-listener
 (fn [[web3-wallet wc-event handler]]
   (.on web3-wallet
        wc-event
        (fn [js-proposal]
          (-> js-proposal
              (js->clj :keywordize-keys true)
              handler)))))

(rf/reg-fx
 :effects.wallet-connect/pair
 (fn [{:keys [web3-wallet url on-success on-fail]}]
   (-> (.. web3-wallet -core -pairing)
       (.pair (clj->js {:uri url}))
       (promesa/then on-success)
       (promesa/catch on-fail))))

(rf/reg-fx
 :effects.wallet-connect/approve-session
 (fn [{:keys [web3-wallet proposal supported-namespaces on-success on-fail]}]
   (let [{:keys [params id]} proposal
         approved-namespaces (wallet-connect/build-approved-namespaces params
                                                                       supported-namespaces)]
     (-> (.approveSession web3-wallet
                          (clj->js {:id         id
                                    :namespaces approved-namespaces}))
         (promesa/then on-success)
         (promesa/catch on-fail)))))
