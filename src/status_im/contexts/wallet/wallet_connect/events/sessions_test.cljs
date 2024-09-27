(ns status-im.contexts.wallet.wallet-connect.events.sessions-test
  (:require
    [cljs.test :refer-macros [is testing]]
    [clojure.string :as string]
    matcher-combinators.test
    [re-frame.db :as rf-db]
    status-im.contexts.wallet.wallet-connect.events.session-responses
    [test-helpers.unit :as h]))

#_{:clj-kondo/ignore [:syntax]}
(h/deftest-event :wallet-connect/on-session-delete
  [event-id dispatch]
  (testing "deletes the session"
    (reset! rf-db/app-db {:profile/profile         {:test-networks-enabled? false}
                          :wallet-connect/sessions [{:topic  "topic"
                                                     :chains ["eip155:1"]}]})
    (let [fx       (:fx (dispatch [event-id
                                   {:topic  "topic"
                                    :chains ["eip155:1"]}]))
          rpc-args (-> fx first second first)]
      (is (and (= :json-rpc/call
                  (ffirst fx))
               (= "wallet_disconnectWalletConnectSession"
                  (:method rpc-args))
               (= "topic"
                  (-> rpc-args :params first))
               (= :wallet-connect/delete-session
                  (-> rpc-args :on-success first))
               (= "topic"
                  (-> rpc-args :on-success second))))))

  (testing "ignore the deletion if session topic not found"
    (let [topic-1 "topic-1"
          topic-2 "topic-2"]
      (reset! rf-db/app-db {:profile/profile         {:test-networks-enabled? false}
                            :wallet-connect/sessions [{:topic  topic-1
                                                       :chains ["eip155:1"]}]})
      (is (match? nil
                  (dispatch [event-id
                             {:topic  topic-2
                              :chains ["eip155:1"]}])))))

  (testing "ignore the deletion if the event is for the wrong network mode (testnet/mainnet)"
    (let [topic "topic"]
      (reset! rf-db/app-db {:profile/profile         {:test-networks-enabled? true}
                            :wallet-connect/sessions [{:topic  topic
                                                       :chains ["eip155:1"]}]})
      (is (match? nil
                  (dispatch [event-id
                             {:topic  topic
                              :chains ["eip155:11155111"]}]))))))

;;(cljs.test/run-tests)
