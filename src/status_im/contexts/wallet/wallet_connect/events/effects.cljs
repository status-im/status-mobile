(ns status-im.contexts.wallet.wallet-connect.events.effects
  (:require
    [promesa.core :as promesa]
    [react-native.wallet-connect :as wallet-connect]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.wallet-connect.utils.sessions :as sessions]
    [status-im.contexts.wallet.wallet-connect.utils.signing :as signing]
    [status-im.contexts.wallet.wallet-connect.utils.transactions :as transactions]
    [status-im.contexts.wallet.wallet-connect.utils.typed-data :as typed-data]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(rf/reg-fx
 :effects.wallet-connect/init
 (fn [{:keys [on-success on-fail]}]
   (let
     [project-id config/WALLET_CONNECT_PROJECT_ID
      metadata   {:name        (i18n/label :t/status)
                  :description (i18n/label :t/status-is-a-secure-messaging-app)
                  :url         constants/wallet-connect-metadata-url
                  :icons       [constants/wallet-connect-metadata-icon]
                  :redirect    {:native constants/wallet-connect-metadata-redirect-native}}]
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
   (-> (sessions/disconnect web3-wallet topic)
       (promesa/then on-success)
       (promesa/catch on-fail))))

(rf/reg-fx
 :effects.wallet-connect/approve-session
 (fn [{:keys [web3-wallet proposal-request session-networks address on-success on-fail]}]
   (-> (sessions/approve
        {:web3-wallet      web3-wallet
         :proposal-request proposal-request
         :address          address
         :session-networks session-networks})
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-fail)))))

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
         (promesa/then (partial rf/call-continuation on-success))
         (promesa/catch (partial rf/call-continuation on-error))))))

(rf/reg-fx
 :effects.wallet-connect/prepare-transaction
 (fn [{:keys [tx chain-id on-success on-error]}]
   (-> (transactions/prepare-transaction tx
                                         chain-id
                                         transactions/default-tx-priority)
       (promesa/then on-success)
       (promesa/catch on-error))))

(rf/reg-fx
 :effects.wallet-connect/sign-transaction
 (fn [{:keys [password address chain-id tx-hash tx-args on-success on-error]}]
   (-> (transactions/sign-transaction (security/safe-unmask-data password)
                                      address
                                      tx-hash
                                      tx-args
                                      chain-id)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(rf/reg-fx
 :effects.wallet-connect/send-transaction
 (fn [{:keys [password address chain-id tx-hash tx-args on-success on-error]}]
   (-> (transactions/send-transaction (security/safe-unmask-data password)
                                      address
                                      tx-hash
                                      tx-args
                                      chain-id)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

(rf/reg-fx
 :effects.wallet-connect/sign-typed-data
 (fn [{:keys [password address data version chain-id on-success on-error]}]
   (-> (typed-data/sign (security/safe-unmask-data password)
                        address
                        data
                        chain-id
                        version)
       (promesa/then (partial rf/call-continuation on-success))
       (promesa/catch (partial rf/call-continuation on-error)))))

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

(rf/reg-fx
 :effects.wallet-connect/get-sessions
 (fn [{:keys [web3-wallet addresses online? on-success on-error]}]
   (-> (sessions/get-sessions web3-wallet addresses online?)
       (promesa/then on-success)
       (promesa/catch on-error))))
