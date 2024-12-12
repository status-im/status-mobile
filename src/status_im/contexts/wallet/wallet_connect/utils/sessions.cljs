(ns status-im.contexts.wallet.wallet-connect.utils.sessions
  (:require
    [clojure.string :as string]
    [promesa.core :as promesa]
    [react-native.wallet-connect :as wallet-connect]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
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
             transforms/json->clj
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
       (for [session (find-inactive-sessions active-sessions
                                             persisted-sessions)]
         (do (log/info "Syncing disconnected session with persistance" session)
             (rpc/wallet-disconnect-persisted-session (:topic session)))))
      (promesa/catch (fn [err]
                       (throw (ex-info "Failed to synchronize persisted sessions"
                                       {:error err
                                        :code  :error/wc-sync-persisted-sessions}))))))

(defn get-sessions
  [web3-wallet addresses online?]
  (promesa/let [persisted-sessions (get-persisted-sessions)]
    (if online?
      (promesa/let [active-sessions (get-active-sessions web3-wallet addresses)]
        (log/info "Got active Wallet Connect sessions" (map :topic active-sessions))
        ;; NOTE: handling the error here, so that if persistance fails, it doesn't affect the active
        ;; sessions
        (-> (sync-persisted-sessions active-sessions persisted-sessions)
            (promesa/catch #(log/error %)))
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

(defn approve
  [{:keys [web3-wallet address session-networks proposal-request]}]
  (let [{:keys [params id]} proposal-request
        accounts            (-> (partial networks/format-eip155-address address)
                                (map session-networks))]
    (-> (promesa/let [session
                      (wallet-connect/approve-session
                       {:web3-wallet         web3-wallet
                        :id                  id
                        :approved-namespaces (->>
                                               {:eip155
                                                {:chains   session-networks
                                                 :accounts accounts
                                                 :methods  constants/wallet-connect-supported-methods
                                                 :events   constants/wallet-connect-supported-events}}
                                               (wallet-connect/build-approved-namespaces params))})]
          (rpc/wallet-persist-session session)
          (transforms/js->clj session))
        (promesa/catch (fn [err]
                         (throw (ex-info "Failed to approve session"
                                         {:err  err
                                          :code :error/wc-approve})))))))
