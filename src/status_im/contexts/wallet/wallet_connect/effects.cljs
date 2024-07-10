(ns status-im.contexts.wallet.wallet-connect.effects
  (:require
    [promesa.core :as promesa]
    [re-frame.core :as rf]
    [react-native.wallet-connect :as wallet-connect]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.wallet-connect.signing :as signing]
    [status-im.contexts.wallet.wallet-connect.transactions :as transactions]
    [utils.i18n :as i18n]
    [utils.security.core :as security]))

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
   (wallet-connect/register-handler
    {:web3-wallet web3-wallet
     :event       wc-event
     :handler     handler})))

(rf/reg-fx
 :effects.wallet-connect/pair
 (fn [{:keys [web3-wallet url on-success on-fail]}]
   (when web3-wallet
     (-> (wallet-connect/core-pairing-pair web3-wallet url)
         (promesa/then on-success)
         (promesa/catch on-fail)))))

(rf/reg-fx
 :effects.wallet-connect/disconnect
 (fn [{:keys [web3-wallet topic on-success on-fail]}]
   (-> (wallet-connect/core-pairing-disconnnect web3-wallet topic)
       (promesa/then on-success)
       (promesa/catch on-fail))))

(rf/reg-fx
 :effects.wallet-connect/approve-session
 (fn [{:keys [web3-wallet proposal supported-namespaces on-success on-fail]}]
   (let [{:keys [params id]} proposal
         approved-namespaces (wallet-connect/build-approved-namespaces
                              params
                              supported-namespaces)]
     (-> (wallet-connect/approve-session
          {:web3-wallet         web3-wallet
           :id                  id
           :approved-namespaces approved-namespaces})
         (promesa/then on-success)
         (promesa/catch on-fail)))))

(rf/reg-fx
 :effects.wallet-connect/fetch-active-sessions
 (fn [{:keys [web3-wallet on-success on-fail]}]
   (-> (wallet-connect/get-active-sessions web3-wallet)
       (promesa/then on-success)
       (promesa/catch on-fail))))

(rf/reg-fx
 :effects.wallet-connect/sign-message
 (fn [{:keys [password address data rpc-method on-success on-error]}]
   (let [password (security/safe-unmask-data password)]
     (-> (condp =
           rpc-method
           :personal-sign
           (signing/personal-sign password address data)

           :eth-sign
           (signing/eth-sign password address data)

           (signing/personal-sign password address data))
         (promesa/then on-success)
         (promesa/catch on-error)))))

(rf/reg-fx
 :effects.wallet-connect/sign-transaction
 (fn [{:keys [password address chain-id tx on-success on-error]}]
   (-> (transactions/sign-transaction (security/safe-unmask-data password)
                                      address
                                      tx
                                      chain-id)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/send-transaction
 (fn [{:keys [password address chain-id tx on-success on-error]}]
   (-> (transactions/send-transaction (security/safe-unmask-data password)
                                      address
                                      tx
                                      chain-id)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/sign-typed-data
 (fn [{:keys [password address data version chain-id on-success on-error]}]
   (-> (signing/eth-sign-typed-data (security/safe-unmask-data password)
                                    address
                                    data
                                    chain-id
                                    version)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/respond-session-request
 (fn [{:keys [web3-wallet topic id result error on-success on-error]}]
   (-> (wallet-connect/respond-session-request
        {:web3-wallet web3-wallet
         :topic       topic
         :id          id
         :result      result
         :error       error})
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/reject-session-proposal
 (fn [{:keys [web3-wallet proposal on-success on-error]}]
   (let [{:keys [id]} proposal
         reason       (wallet-connect/get-sdk-error
                       constants/wallet-connect-user-rejected-error-key)]
     (-> (wallet-connect/reject-session
          {:web3-wallet web3-wallet
           :id          id
           :reason      reason})
         (promesa/then on-success)
         (promesa/catch on-error)))))
