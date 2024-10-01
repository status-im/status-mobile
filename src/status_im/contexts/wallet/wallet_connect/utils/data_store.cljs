(ns status-im.contexts.wallet.wallet-connect.utils.data-store
  (:require
    [clojure.string :as string]
    [status-im.constants :as constants]
    utils.string
    [utils.transforms :as transforms]))

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

(defn- find-networks
  [current-request current-proposal request db]
  (cond
    current-request
    (get-in current-request [:params :chainId])

    current-proposal
    (->> (get-in db [:wallet-connect/current-proposal :session-networks])
         vec
         (string/join ","))

    request
    (->> (or (get-in request [:params :requiredNamespaces :eip155 :chains])
             (get-in request [:requiredNamespaces :eip155 :chains]))
         vec
         (string/join ","))

    :else
    []))

(defn- find-url
  [current-request current-proposal request]
  (cond
    current-request
    (get-in current-request [:verifyContext :verified :origin])

    current-proposal
    (get-in current-proposal [:params :proposer :metadata :url])

    request
    (or (get-in request [:params :proposer :metadata :url])
        (get-in request [:peer :metadata :url]))

    :else
    nil))

(defn get-dapp-name-and-networks
  [db & [request]]
  (let [current-request  (get-in db [:wallet-connect/current-request :event])
        current-proposal (get-in db [:wallet-connect/current-proposal :request])
        session-networks (find-networks current-request current-proposal request db)
        dapp-url         (find-url current-request current-proposal request)
        dapp-name        (compute-dapp-name nil dapp-url)]
    [dapp-name session-networks]))
