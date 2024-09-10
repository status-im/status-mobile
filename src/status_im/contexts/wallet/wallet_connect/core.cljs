(ns status-im.contexts.wallet.wallet-connect.core
  (:require [clojure.string :as string]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.utils.networks :as networks]
            [utils.string]
            [utils.transforms :as transforms]))

(def method-to-screen
  {constants/wallet-connect-personal-sign-method        :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-typed-method       :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-method             :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-sign-typed-v4-method    :screen/wallet-connect.sign-message
   constants/wallet-connect-eth-send-transaction-method :screen/wallet-connect.send-transaction
   constants/wallet-connect-eth-sign-transaction-method :screen/wallet-connect.sign-transaction})

(defn extract-native-call-signature
  [data]
  (-> data transforms/json->clj :result))

(defn get-request-method
  [event]
  (get-in event [:params :request :method]))

(defn get-request-params
  [event]
  (get-in event [:params :request :params]))

(defn get-db-current-request-event
  [db]
  (get-in db [:wallet-connect/current-request :event]))

(defn get-session-dapp-metadata
  [proposal]
  (let [metadata (get-in proposal [:params :proposer :metadata])
        origin   (get-in proposal [:verifyContext :verified :origin])]
    (or metadata {:url origin})))

(defn get-current-request-dapp
  [request sessions]
  (let [dapp-url (get-in request [:event :verifyContext :verified :origin])]
    (->> sessions
         (filter (fn [session]
                   (= (utils.string/remove-trailing-slash dapp-url)
                      (utils.string/remove-trailing-slash (get session :url)))))
         first)))

(defn get-dapp-redirect-url
  [session]
  (get-in session [:peer :metadata :redirect :native]))

(defn get-db-current-request-params
  [db]
  (-> db
      get-db-current-request-event
      get-request-params))

(defn event-should-be-handled?
  [db {:keys [topic]}]
  (let [testnet-mode? (get-in db [:profile/profile :test-networks-enabled?])]
    (some #(and (= (:topic %) topic)
                (networks/session-networks-allowed? testnet-mode? %))
          (:wallet-connect/sessions db))))

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
                            (clojure.string/includes? account account-address))
                          account-addresses))
                  accounts))
          sessions))

(defn compute-dapp-name
  "Sometimes dapps have no name or an empty name. Return url as name in that case"
  [name url]
  (if (seq name)
    name
    (when (seq url)
      (-> url
          utils.string/remove-trailing-slash
          utils.string/remove-http-prefix
          string/capitalize))))

(defn compute-dapp-icon-path
  "Some dapps have icons with relative paths, make paths absolute in those cases, send nil if icon is missing"
  [icon-path url]
  (when (and (seq icon-path)
             (seq url))
    (if (string/starts-with? icon-path "http")
      icon-path
      (str (utils.string/remove-trailing-slash url) icon-path))))
