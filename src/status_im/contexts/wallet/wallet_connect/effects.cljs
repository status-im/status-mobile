(ns status-im.contexts.wallet.wallet-connect.effects
  (:require
    [cljs-bean.core :as bean]
    [native-module.core :as native-module]
    [promesa.core :as promesa]
    [re-frame.core :as rf]
    [react-native.wallet-connect :as wallet-connect]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
    [status-im.contexts.wallet.wallet-connect.transactions :as transactions]
    [utils.i18n :as i18n]
    [utils.security.core :as security]
    [utils.transforms :as transforms]))

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
 :effects.wallet-connect/fetch-pairings
 (fn [{:keys [web3-wallet on-success on-fail]}]
   (-> (.. web3-wallet -core -pairing)
       (.getPairings)
       (promesa/then on-success)
       (promesa/catch on-fail))))

(rf/reg-fx
 :effects.wallet-connect/pair
 (fn [{:keys [web3-wallet url on-success on-fail]}]
   (when web3-wallet
     (-> (.. web3-wallet -core -pairing)
         (.pair (clj->js {:uri url}))
         (promesa/then on-success)
         (promesa/catch on-fail)))))

(rf/reg-fx
 :effects.wallet-connect/disconnect
 (fn [{:keys [web3-wallet topic on-success on-fail]}]
   (-> (.. web3-wallet -core -pairing)
       (.disconnect (clj->js {:topic topic}))
       (promesa/then on-success)
       (promesa/catch on-fail))))

(rf/reg-fx
 :effects.wallet-connect/fetch-active-sessions
 (fn [{:keys [web3-wallet on-success on-fail]}]
   (-> (.getActiveSessions web3-wallet)
       (promesa/then on-success)
       (promesa/catch on-fail))))

(rf/reg-fx
 :effects.wallet-connect/approve-session
 (fn [{:keys [web3-wallet proposal supported-namespaces on-success on-fail]}]
   (let [{:keys [params id]} proposal
         approved-namespaces (wallet-connect/build-approved-namespaces
                              params
                              supported-namespaces)]
     (-> (.approveSession web3-wallet
                          (clj->js {:id         id
                                    :namespaces approved-namespaces}))
         (promesa/then on-success)
         (promesa/catch on-fail)))))

(rf/reg-fx
 :effects.wallet-connect/sign-message
 (fn [{:keys [password address data on-success on-error]}]
   (-> {:data     data
        :account  address
        :password (security/safe-unmask-data password)}
       bean/->js
       transforms/clj->json
       native-module/sign-message
       (promesa/then wallet-connect-core/extract-native-call-signature)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/sign-transaction
 (fn [{:keys [password address chain-id tx on-success on-error]}]
   (-> (transactions/sign-transaction (security/safe-unmask-data password) address tx chain-id)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/send-transaction
 (fn [{:keys [password address chain-id tx on-success on-error]}]
   (-> (transactions/send-transaction (security/safe-unmask-data password) address tx chain-id)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/sign-typed-data
 (fn [{:keys [password address data version on-success on-error]}]
   (-> (wallet-connect-core/sign-typed-data version data address (security/safe-unmask-data password))
       (promesa/then wallet-connect-core/extract-native-call-signature)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/respond-session-request
 (fn [{:keys [web3-wallet topic id result on-success on-error]}]
   (->
     (.respondSessionRequest web3-wallet
                             (clj->js {:topic    topic
                                       :response {:id      id
                                                  :jsonrpc "2.0"
                                                  :result  result}}))
     (promesa/then on-success)
     (promesa/catch on-error))))
