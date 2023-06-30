(ns status-im2.contexts.profile.login.events
  (:require [utils.re-frame :as rf]
            [status-im.ethereum.core :as ethereum]
            [utils.security.core :as security]
            [re-frame.core :as re-frame]
            [native-module.core :as native-module]
            [status-im2.config :as config]
            [status-im2.navigation.events :as navigation]))

(re-frame/reg-fx
 ::login
 (fn [[key-uid hashed-password]]
   (native-module/login-account
    {:keyUid                      key-uid
     :password                    hashed-password
     :openseaAPIKey               config/opensea-api-key

     :poktToken                   config/POKT_TOKEN
     :infuraToken                 config/INFURA_TOKEN

     :alchemyOptimismMainnetToken config/ALCHEMY_OPTIMISM_MAINNET_TOKEN
     :alchemyOptimismGoerliToken  config/ALCHEMY_OPTIMISM_GOERLI_TOKEN
     :alchemyArbitrumMainnetToken config/ALCHEMY_ARBITRUM_MAINNET_TOKEN
     :alchemyArbitrumGoerliToken  config/ALCHEMY_ARBITRUM_GOERLI_TOKEN})))

(rf/defn login
  {:events [:profile.login/login]}
  [{:keys [db]}]
  (let [{:keys [key-uid password]} (:profile/login db)]
    {:db    (assoc-in db [:profile/login :processing] true)
     ::login [key-uid (ethereum/sha3 (security/safe-unmask-data password))]}))

(rf/defn login-with-biometric-if-available
  {:events [:profile.login/login-with-biometric-if-available]}
  [_ key-uid]
  {:keychain/get-auth-method key-uid})

;; result of :keychain/get-auth-method above
(rf/defn get-user-password-success
  {:events [:profile/get-user-password-success]}
  [{:keys [db] :as cofx} password]
  (when password
    (rf/merge
     cofx
     {:db (assoc-in db [:profile/login :password] password)}
     (navigation/init-root :progress)
     (login))))
