(ns status-im.contexts.wallet.wallet-connect.utils.session
  (:require
    [clojure.string :as string]
    [promesa.core :as promesa]
    [react-native.wallet-connect :as wallet-connect]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils.account :as account-utils]
    [status-im.contexts.wallet.wallet-connect.utils.dapp :as dapp-utils]
    [status-im.contexts.wallet.wallet-connect.utils.rpc :as rpc]
    [taoensso.timbre :as log]
    [utils.transforms :as transforms]))

(defn- dapp-icon
  [session]
  (-> session
      (get-in [:peer :metadata :icons])
      dapp-utils/best-icon))

(defn- dapp-url
  [session]
  (get-in session [:peer :metadata :url]))

(defn- dapp-name
  [session]
  (-> session
      (get-in [:peer :metadata :name])
      (or (dapp-url session))))

(defn- addresses
  [session]
  (get-in session [:namespaces :eip155 :accounts]))

(defn- chains
  [session]
  (get-in session [:namespaces :eip155 :chains]))

(defn session->db
  [{:keys [topic expiry pairingTopic] :as session}]
  {:topic        topic
   :expiry       expiry
   :sessionJson  (transforms/clj->json session)
   :pairingTopic pairingTopic
   :name         (dapp-name session)
   :iconUrl      (dapp-icon session)
   :url          (dapp-url session)
   :accounts     (addresses session)
   :chains       (chains session)
   :disconnected false})

(defn filter-for-accounts
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
  (let [recent-session (->> sessions
                            (sort-by :expiry >)
                            first)]
    (-> recent-session
        :accounts
        first
        account-utils/from-eip155)))

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
      (promesa/then #(map :accounts %))
      (promesa/catch (fn [err]
                       (throw (ex-info "Failed to get persisted WalletConnect sessions"
                                       {:error err
                                        :code  :error/wc-get-persisted-sessions}))))))

(defn get-active-sessions
  [web3-wallet account-addresses]
  (-> (wallet-connect/get-active-sessions web3-wallet)
      (promesa/then #(->>
                       (transforms/js->clj %)
                       vals
                       (map session->db)
                       (filter-for-accounts account-addresses)))
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
  [web3-wallet accounts online?]
  (promesa/let [persisted-sessions (get-persisted-sessions)]
    (if online?
      (promesa/let [active-sessions (get-active-sessions web3-wallet accounts)]
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
        session-accounts    (-> (partial account-utils/to-eip155 address)
                                (map session-networks))]
    (-> (promesa/let [session
                      (wallet-connect/approve-session
                       {:web3-wallet         web3-wallet
                        :id                  id
                        :approved-namespaces (->>
                                               {:eip155
                                                {:chains   session-networks
                                                 :accounts session-accounts
                                                 :methods  constants/wallet-connect-supported-methods
                                                 :events   constants/wallet-connect-supported-events}}
                                               (wallet-connect/build-approved-namespaces params))})]
          (rpc/wallet-persist-session session)
          (transforms/js->clj session))
        (promesa/catch (fn [err]
                         (throw (ex-info "Failed to approve session"
                                         {:err  err
                                          :code :error/wc-approve})))))))
