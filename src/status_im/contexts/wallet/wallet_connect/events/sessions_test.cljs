(ns status-im.contexts.wallet.wallet-connect.events.sessions-test
  (:require
    [cljs.test :refer-macros [is are testing]]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    [status-im.contexts.wallet.networks.config :as networks.config]
    status-im.contexts.wallet.wallet-connect.events.session-responses
    status-im.contexts.wallet.wallet-connect.events.sessions
    [test-helpers.unit :as h]))

(def ethereum-chain-id networks.config/ethereum-chain-id)
(def optimism-chain-id networks.config/optimism-chain-id)
(def sepolia-chain-id networks.config/sepolia-chain-id)

(defn- find-fx
  [fx name]
  (some #(when (= name (first %)) %) fx))

(defn- get-fx-arg
  "Finds the arg value for an effect, if present in the fx vector"
  [fx fx-name arg-fn]
  (-> fx
      (find-fx fx-name)
      second
      arg-fn))

(h/deftest-event :wallet-connect/on-session-delete
  [event-id dispatch]
  (testing "successfully deletes the session"
    (reset! rf-db/app-db {:profile/profile         {:test-networks-enabled? false}
                          :wallet                  {:networks {:prod [{:chain-id ethereum-chain-id}
                                                                      {:chain-id optimism-chain-id}]}}
                          :wallet-connect/sessions [{:topic  "topic"
                                                     :chains ["eip155:1"]}]})
    (let [fx             (:fx (dispatch [event-id
                                         {:topic  "topic"
                                          :chains ["eip155:1"]}]))
          get-rpc-fx-arg (partial get-fx-arg fx :json-rpc/call)]
      (are [expected result] (match? expected result)
       :json-rpc/call                           (-> fx (find-fx :json-rpc/call) first)
       "wallet_disconnectWalletConnectSession"  (get-rpc-fx-arg (comp :method first))
       ["topic"]                                (get-rpc-fx-arg (comp :params first))
       [:wallet-connect/delete-session "topic"] (get-rpc-fx-arg (comp :on-success first)))))

  (testing "ignore the deletion if session topic not found"
    (let [topic-1 "topic-1"
          topic-2 "topic-2"]
      (reset! rf-db/app-db {:profile/profile         {:test-networks-enabled? false}
                            :wallet                  {:networks {:prod [{:chain-id ethereum-chain-id}
                                                                        {:chain-id optimism-chain-id}]}}
                            :wallet-connect/sessions [{:topic  topic-1
                                                       :chains ["eip155:1"]}]})
      (is (match? nil
                  (dispatch [event-id
                             {:topic  topic-2
                              :chains ["eip155:1"]}])))))

  (testing "ignore the deletion if the event is for the wrong network mode (testnet/mainnet)"
    (let [topic "topic"]
      (reset! rf-db/app-db {:profile/profile         {:test-networks-enabled? true}
                            :wallet                  {:networks {:test [{:chain-id sepolia-chain-id}]}}
                            :wallet-connect/sessions [{:topic  topic
                                                       :chains ["eip155:1"]}]})
      (is (match? nil
                  (dispatch [event-id
                             {:topic  topic
                              :chains ["eip155:11155111"]}]))))))

(h/deftest-event :wallet-connect/disconnect-dapp
  [event-id dispatch]
  (testing "disconnecting from dApp when online"
    (reset! rf-db/app-db {:network/status             :online
                          :wallet-connect/web3-wallet "mock"})
    (let [fx (:fx (dispatch [event-id {:topic "topic"}]))]
      (are [expected result] (match? expected result)
       :effects.wallet-connect/disconnect (ffirst fx)
       "topic"                            (-> fx first second :topic)
       "mock"                             (-> fx first second :web3-wallet))))

  (testing "showing no-internet toast when offline"
    (reset! rf-db/app-db {:network/status :offline})
    (is (match? :wallet-connect/no-internet-toast
                (-> (dispatch [event-id {:topic "topic"}])
                    :fx
                    first
                    second
                    first)))))

(h/deftest-event :wallet-connect/get-sessions
  [event-id dispatch]
  (testing "the fx includes only for the available accounts"
    (reset! rf-db/app-db {:wallet                     {:accounts {"available"    {:address   "0x123"
                                                                                  :operable? true}
                                                                  "watch-only"   {:address     "0x456"
                                                                                  :operable?   true
                                                                                  :watch-only? true}
                                                                  "non-operable" {:address   "0x789"
                                                                                  :operable? false}}}
                          :network/status             :online
                          :wallet-connect/web3-wallet "mock"})
    (let [fx                  (:fx (dispatch [event-id]))
          get-sessions-fx-arg (partial get-fx-arg fx :effects.wallet-connect/get-sessions)]
      (are [expected result] (match? expected result)
       :effects.wallet-connect/get-sessions (-> fx (find-fx :effects.wallet-connect/get-sessions) first)
       true                                 (get-sessions-fx-arg :online?)
       '("0x123")                           (get-sessions-fx-arg :addresses)
       "mock"                               (get-sessions-fx-arg :web3-wallet)))))

(h/deftest-event :wallet-connect/get-sessions-success
  [event-id dispatch]
  (testing "sessions are stored in the db"
    (let [sessions '({:topic "123"} {:topic "456"})]
      (is (match? {:db {:wallet-connect/sessions sessions}}
                  (dispatch [event-id sessions]))))))

(h/deftest-event :wallet-connect/on-new-session
  [event-id dispatch]
  (testing "new session is added to db"
    (let [sessions    '({:topic "123"} {:topic "456"})
          new-session {:topic "789"}]
      (reset! rf-db/app-db {:wallet-connect/sessions sessions})
      (is (match? {:db {:wallet-connect/sessions (conj sessions new-session)}}
                  (dispatch [event-id new-session]))))))

(h/deftest-event :wallet-connect/delete-session
  [event-id dispatch]
  (testing "session is deleted from db"
    (let [sessions '({:topic "123"} {:topic "456"})
          expected '({:topic "123"})]
      (reset! rf-db/app-db {:wallet-connect/sessions sessions})
      (is (match? {:db {:wallet-connect/sessions expected}}
                  (dispatch [event-id "456"]))))))
