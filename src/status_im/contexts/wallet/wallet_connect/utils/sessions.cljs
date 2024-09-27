(ns status-im.contexts.wallet.wallet-connect.utils.sessions
  (:require
    [clojure.string :as string]
    [promesa.core :as promesa]
    [react-native.wallet-connect :as wallet-connect]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
    [taoensso.timbre :as log]
    [utils.transforms :as transforms]))

(defn sdk-session->db-session
  [{:keys [topic expiry pairingTopic] :as session}]
  {:topic        topic
   :expiry       expiry
   :sessionJson  (transforms/clj->json session)
   :pairingTopic pairingTopic
   :name         (get-in session [:peer :metadata :name])
   :iconUrl      (get-in session [:peer :metadata :icons 0])
   :url          (get-in session [:peer :metadata :url])
   :accounts     (get-in session [:namespaces :eip155 :accounts])
   :chains       (get-in session [:namespaces :eip155 :chains])
   :disconnected false})

(defn filter-operable-accounts
  [accounts]
  (filter #(and (:operable? %)
                (not (:watch-only? %)))
          accounts))

(defn filter-sessions-for-account-addresses
  [account-addresses sessions]
  (filter (fn [{:keys [accounts]}]
            (some (fn [account]
                    (some (fn [account-address]
                            (string/includes? account account-address))
                          account-addresses))
                  accounts))
          sessions))

(defn latest-connected-account-address
  [sessions]
  (let [all-accounts (->> sessions
                          (sort-by :expiry >)
                          first
                          :accounts)]
    (-> all-accounts
        first
        (string/split #":")
        last)))

(defn- parse-session-accounts
  [{:keys [sessionJson] :as session}]
  (assoc session
         :accounts
         (-> sessionJson
             :namespaces
             :eip155
             :accounts)))

(defn- find-inactive-sessions
  [active-sessions persisted-sessions]
  (->> persisted-sessions
       (filter #(->> %
                     :topic
                     (contains? (->> active-sessions
                                     (map :topic)
                                     set))
                     not))))

(defn get-persisted-sessions
  []
  (-> (rpc/wallet-get-persisted-sessions)
      (promesa/then #(map parse-session-accounts %))
      (promesa/catch (fn [err]
                       (throw (ex-info "Failed to get persisted WalletConnect sessions"
                                       {:error err
                                        :code  :error/wc-get-persisted-sessions}))))))

(defn get-active-sessions
  [web3-wallet addresses]
  (-> (wallet-connect/get-active-sessions web3-wallet)
      (promesa/then #(->>
                       (transforms/js->clj %)
                       vals
                       (map sdk-session->db-session)
                       (filter-sessions-for-account-addresses addresses)))
      (promesa/catch (fn [err]
                       (throw (ex-info "Failed to get active WalletConnect sessions"
                                       {:error err
                                        :code  :error/wc-get-active-sessions}))))))

(defn sync-persisted-sessions
  [active-sessions persisted-sessions]
  (-> (promesa/all
       (for [topic (find-inactive-sessions active-sessions
                                           persisted-sessions)]
         (do (log/info "Syncing disconnected session with persistance" topic)
             (rpc/wallet-disconnect-persisted-session topic))))
      (promesa/catch (fn [err]
                       (throw (ex-info "Failed to synchronize persisted sessions"
                                       {:error err
                                        :code  :error/wc-sync-persisted-sessions}))))))

(defn get-sessions
  [web3-wallet addresses online?]
  (promesa/let [persisted-sessions (get-persisted-sessions)]
    (if online?
      (promesa/let [active-sessions (get-active-sessions web3-wallet addresses)]
        (sync-persisted-sessions active-sessions persisted-sessions)
        active-sessions)
      persisted-sessions)))

(defn disconnect
  [web3-wallet topic]
  (let [reason (wallet-connect/get-sdk-error constants/wallet-connect-user-disconnected-reason-key)]
    (->
      (promesa/do
        (wallet-connect/disconnect-session {:web3-wallet web3-wallet
                                            :topic       topic
                                            :reason      reason})
        (rpc/wallet-disconnect-persisted-session topic))
      (promesa/catch (fn [err]
                       (throw (ex-info "Failed to disconnect dapp"
                                       {:err  err
                                        :code :error/wc-disconnect-dapp})))))))





;; TODO:
;; 2. approve session + add to persistance


