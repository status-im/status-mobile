(ns status-im.test.transport.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.fx :as fx]
            status-im.transport.impl.receive
            [status-im.protocol.core :as protocol]
            [status-im.transport.core :as transport]
            [status-im.transport.message.core :as message]))

(deftest init-whisper
  (let [cofx {:db {:multiaccount {:public-key "1"}}}]
    (testing "custom mailservers"
      (let [ms-1            {:id :a
                             :fleet :eth.beta
                             :name "name-1"
                             :address "address-1"
                             :password "password-1"}
            ms-2            {:id :b
                             :fleet :eth.beta
                             :name "name-2"
                             :address "address-2"
                             :password "password-2"}
            ms-3            {:id :c
                             :fleet :eth.test
                             :name "name-3"
                             :address "address-3"
                             :password "password-3"}
            expected-mailservers {:eth.beta {:a (-> ms-1
                                                    (dissoc :fleet)
                                                    (assoc :user-defined true))
                                             :b (-> ms-2
                                                    (dissoc ms-2 :fleet)
                                                    (assoc :user-defined true))}
                                  :eth.test {:c (-> ms-3
                                                    (dissoc :fleet)
                                                    (assoc :user-defined true))}}]
        (is (= expected-mailservers
               (-> (get-in
                    (protocol/initialize-protocol cofx {:mailservers [ms-1 ms-2 ms-3]
                                                        :mailserver-ranges {}
                                                        :mailserver-topics {}
                                                        :default-mailserver true})
                    [:db :mailserver/mailservers])
                   (update-in [:eth.beta :a] dissoc :generating-sym-key?)
                   (update-in [:eth.beta :b] dissoc :generating-sym-key?)
                   (update-in [:eth.test :c] dissoc :generating-sym-key?))))))))

(def sig "0x04325367620ae20dd878dbb39f69f02c567d789dd21af8a88623dc5b529827c2812571c380a2cd8236a2851b8843d6486481166c39debf60a5d30b9099c66213e4")

(def messages [(clj->js {:id "someid"
                         :metadata {:author {:publicKey sig}}
                         :payload "[\"~#c4\",[\"F\",\"text/plain\",\"~:public-group-user-message\",152769201438101,1527692014377]]"
                         :message {:sig       sig
                                   :ttl       10
                                   :timestamp 1527692015
                                   :topic     "0x9c22ff5f"
                                   :payload   "0x5b227e236334222c5b2246222c22746578742f706c61696e222c227e3a7075626c69632d67726f75702d757365722d6d657373616765222c3135323736393230313433383130312c313532373639323031343337375d5d"
                                   :padding   "0xbf06347cc7f9aa18b4a846032264a88f559d9b14079975d14b10648847c0543a77a80624e101c082d19b502ae3b4f97958d18abf59eb0a82afc1301aa22470495fac739a30c2f563599fa8d8e09363a43d39311596b7f119dee7b046989c08224f1ef5cdc385"
                                   :pow       0.002631578947368421
                                   :hash      "0x220ef9994a4fae64c112b27ed07ef910918159cbe6fcf8ac515ee2bf9a6711a0"}})])

(deftest message-envelopes
  (let [chat-id "chat-id"
        from "from"
        message-id "message-id"
        initial-cofx {:db {:chats {chat-id {:messages {message-id {:from from}}}}}}]

    (testing "a single envelope message"
      (let [cofx (message/set-message-envelope-hash initial-cofx chat-id message-id :message-type 1)]
        (testing "it sets the message-infos"
          (is (= {:chat-id chat-id
                  :message-id message-id
                  :message-type :message-type}
                 (get-in cofx [:db :transport/message-envelopes message-id]))))
        (testing "the message is sent"
          (is (= :sent
                 (get-in
                  (message/update-envelope-status cofx message-id :sent)
                  [:db :chats chat-id :messages message-id :outgoing-status]))))

        (testing "the message is not sent"
          (is (= :not-sent
                 (get-in
                  (message/update-envelope-status cofx message-id :not-sent)
                  [:db :chats chat-id :messages message-id :outgoing-status]))))))
    (testing "multi envelope message"
      (testing "only inserts"
        (let [cofx (fx/merge
                    initial-cofx
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/set-message-envelope-hash chat-id message-id :message-type 3))]
          (testing "it sets the message count"
            (is (= {:pending-confirmations 3}
                   (get-in cofx [:db :transport/message-ids->confirmations message-id]))))))
      (testing "message sent correctly"
        (let [cofx (fx/merge
                    initial-cofx
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/update-envelope-status message-id :sent)
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/update-envelope-status message-id :sent)
                    (message/update-envelope-status message-id :sent))]
          (testing "it removes the confirmations"
            (is (not (get-in cofx [:db :transport/message-ids->confirmations message-id]))))
          (testing "the message is sent"
            (is (= :sent
                   (get-in
                    cofx
                    [:db :chats chat-id :messages message-id :outgoing-status]))))))
      (testing "message not sent"
        (let [cofx (fx/merge
                    initial-cofx
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/update-envelope-status message-id :sent)
                    (message/set-message-envelope-hash chat-id message-id :message-type 3)
                    (message/update-envelope-status message-id :not-sent)
                    (message/update-envelope-status message-id :sent))]
          (testing "it removes the confirmations"
            (is (not (get-in cofx [:db :transport/message-ids->confirmations message-id]))))
          (testing "the message is sent"
            (is (= :not-sent
                   (get-in
                    cofx
                    [:db :chats chat-id :messages message-id :outgoing-status])))))))))
