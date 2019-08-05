(ns status-im.test.chat.models
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.chat.models :as chat]))

(deftest upsert-chat-test
  (testing "upserting a non existing chat"
    (let [chat-id        "some-chat-id"
          contact-name   "contact-name"
          chat-props     {:chat-id chat-id
                          :extra-prop "some"}
          cofx           {:now "now"
                          :db {:contacts/contacts {chat-id
                                                   {:name contact-name}}}}
          response      (chat/upsert-chat cofx chat-props)
          actual-chat   (get-in response [:db :chats chat-id])]
      (testing "it adds the chat to the chats collection"
        (is actual-chat))
      (testing "it adds the extra props"
        (is (= "some" (:extra-prop actual-chat))))
      (testing "it adds the chat id"
        (is (= chat-id (:chat-id actual-chat))))
      (testing "it pulls the name from the contacts"
        (is (= contact-name (:name actual-chat))))
      (testing "it sets the timestamp"
        (is (= "now" (:timestamp actual-chat))))
      (testing "it adds the contact-id to the contact field"
        (is (= chat-id (-> actual-chat :contacts first))))))
  (testing "upserting an existing chat"
    (let [chat-id        "some-chat-id"
          chat-props     {:chat-id chat-id
                          :name "new-name"
                          :extra-prop "some"}
          cofx           {:db {:chats {chat-id {:is-active true
                                                :name "old-name"}}}}
          response      (chat/upsert-chat cofx chat-props)
          actual-chat   (get-in response [:db :chats chat-id])]
      (testing "it adds the chat to the chats collection"
        (is actual-chat))
      (testing "it adds the extra props"
        (is (= "some" (:extra-prop actual-chat))))
      (testing "it updates existins props"
        (is (= "new-name" (:name actual-chat)))))))

(deftest add-public-chat
  (let [topic "topic"
        fx (chat/add-public-chat {:db {}} topic)
        store-fx   (:data-store/tx fx)
        chat (get-in fx [:db :chats topic])]
    (testing "it sets the name"
      (is (= topic (:name chat))))
    (testing "it sets the participants"
      (is (= #{} (:contacts chat))))
    (testing "it sets the chat-id"
      (is (= topic (:chat-id chat))))
    (testing "it sets the group-chat flag"
      (is (:group-chat chat)))
    (testing "it does not sets the public flag"
      (is (:public? chat)))))

(deftest clear-history-test
  (let [chat-id "1"
        cofx    {:db {:chats {chat-id {:message-groups          {:something "a"}
                                       :messages                {"1" {:clock-value 1}
                                                                 "2" {:clock-value 10}
                                                                 "3" {:clock-value 2}}
                                       :unviewed-messages-count 1}}}}]
    (testing "it deletes all the messages"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (= {} (get-in actual [:db :chats chat-id :messages])))))
    (testing "it deletes all the message groups"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (= {} (get-in actual [:db :chats chat-id :message-groups])))))
    (testing "it deletes unviewed messages set"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (= 0 (get-in actual [:db :chats chat-id :unviewed-messages-count])))))
    (testing "it sets a deleted-at-clock-value equal to the last message clock-value"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (= 10 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it does not override the deleted-at-clock-value when there are no messages"
      (let [actual (chat/clear-history (update-in cofx
                                                  [:db :chats chat-id]
                                                  assoc
                                                  :messages {}
                                                  :deleted-at-clock-value 100)
                                       chat-id)]
        (is (= 100 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it set the deleted-at-clock-value to now the chat has no messages nor previous deleted-at"
      (with-redefs [utils.clocks/send (constantly 42)]
        (let [actual (chat/clear-history (update-in cofx
                                                    [:db :chats chat-id]
                                                    assoc
                                                    :messages {})
                                         chat-id)]
          (is (= 42 (get-in actual [:db :chats chat-id :deleted-at-clock-value]))))))
    (testing "it adds the relevant rpc calls"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (::json-rpc/call actual))
        (is (= 1 (count (::json-rpc/call actual))))))))

(deftest remove-chat-test
  (let [chat-id "1"
        cofx    {:db {:transport/chats {chat-id {}}
                      :chats {chat-id {:messages {"1" {:clock-value 1}
                                                  "2" {:clock-value 10}
                                                  "3" {:clock-value 2}}}}}}]
    (testing "it deletes all the messages"
      (let [actual (chat/remove-chat cofx chat-id)]
        (is (= {} (get-in actual [:db :chats chat-id :messages])))))
    (testing "it sets a deleted-at-clock-value equal to the last message clock-value"
      (let [actual (chat/remove-chat cofx chat-id)]
        (is (= 10 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it sets the chat as inactive"
      (let [actual (chat/remove-chat cofx chat-id)]
        (is (= false (get-in actual [:db :chats chat-id :is-active])))))
    (testing "it removes it from transport if it's a public chat"
      (let [actual (chat/remove-chat (update-in
                                      cofx
                                      [:db :chats chat-id]
                                      assoc
                                      :group-chat true
                                      :public? true)
                                     chat-id)]
        (is (not (get-in actual [:db :transport/chats chat-id])))))
    #_(testing "it sends a leave group request if it's a group-chat"
        (let [actual (chat/remove-chat (assoc-in
                                        cofx
                                        [:db :chats chat-id :group-chat]
                                        true)
                                       chat-id)]
          (is (:shh/post  actual))
          (testing "it does not remove transport, only after send is successful"
            (is (get-in actual [:db :transport/chats chat-id])))))
    (testing "it adds the relevant transactions for realm"
      (let [actual (chat/remove-chat cofx chat-id)]
        (is (:data-store/tx actual))
        (is (= 3 (count (:data-store/tx actual))))))))

(deftest multi-user-chat?
  (let [chat-id "1"]
    (testing "it returns true if it's a group chat"
      (let [cofx {:db {:chats {chat-id {:group-chat true}}}}]
        (is (chat/multi-user-chat? cofx chat-id))))
    (testing "it returns true if it's a public chat"
      (let [cofx {:db {:chats {chat-id {:public? true :group-chat true}}}}]
        (is (chat/multi-user-chat? cofx chat-id))))
    (testing "it returns false if it's a 1-to-1 chat"
      (let [cofx {:db {:chats {chat-id {}}}}]
        (is (not (chat/multi-user-chat? cofx chat-id)))))))

(deftest group-chat?
  (let [chat-id "1"]
    (testing "it returns true if it's a group chat"
      (let [cofx {:db {:chats {chat-id {:group-chat true}}}}]
        (is (chat/group-chat? cofx chat-id))))
    (testing "it returns false if it's a public chat"
      (let [cofx {:db {:chats {chat-id {:public? true :group-chat true}}}}]
        (is (not (chat/group-chat? cofx chat-id)))))
    (testing "it returns false if it's a 1-to-1 chat"
      (let [cofx {:db {:chats {chat-id {}}}}]
        (is (not (chat/group-chat? cofx chat-id)))))))

(def test-db
  {:multiaccount {:public-key "me"}
   :chats {"status" {:public? true
                     :group-chat true
                     :messages {"4" {} "5" {} "6" {}}
                     :loaded-unviewed-messages-ids #{"6" "5" "4"}}
           "opened" {:loaded-unviewed-messages-ids #{}}
           "1-1"    {:loaded-unviewed-messages-ids #{"6" "5" "4"}}}})

#_(deftest mark-messages-seen
    (testing "Marking messages seen correctly marks loaded messages as seen and updates absolute unviewed set"
      (let [fx (chat/mark-messages-seen {:db test-db} "status")
            me (get-in test-db [:multiaccount :public-key])]
        (is (= '(true true true)
               (map (comp :seen second) (get-in fx [:db :chats "status" :messages]))))
        (is (= 1 (count (:data-store/tx fx))))
      ;; for public chats, no confirmation is sent out
        (is (= nil (:shh/post fx)))))

    (testing "With empty unviewed set, no effects are produced"
      (is (= nil (chat/mark-messages-seen {:db test-db} "opened"))))

    #_(testing "For 1-1 chat, we send seen messages confirmation to the
  recipient as well"
        (is (= #{"4" "5" "6"}
               (set (get-in (chat/mark-messages-seen {:db test-db} "1-1")
                            [:shh/post 0 :message :payload :message-ids]))))))

(deftest update-dock-badge-label
  (testing "When user has unseen private messages"
    (is (= {:set-dock-badge-label 3}
           (chat/update-dock-badge-label
            {:db {:chats {"0x0"    {:is-active                    true
                                    :public?                      false
                                    :unviewed-messages-count      3
                                    :loaded-unviewed-messages-ids #{1 2 3}}
                          "status" {:is-active                    true
                                    :public?                      true
                                    :unviewed-messages-count      2
                                    :loaded-unviewed-messages-ids #{1 2}}}}}))))
  (testing "When user has unseen public messages and no unseen private messages"
    (is (= {:set-dock-badge-label "•"}
           (chat/update-dock-badge-label
            {:db {:chats {"0x0"    {:is-active                    true
                                    :public?                      false
                                    :unviewed-messages-count      0
                                    :loaded-unviewed-messages-ids #{}}
                          "status" {:is-active                    true
                                    :public?                      true
                                    :unviewed-messages-count      2
                                    :loaded-unviewed-messages-ids #{1 2}}}}}))))
  (testing "When user has no unseen messages"
    (is (= {:set-dock-badge-label nil}
           (chat/update-dock-badge-label
            {:db {:chats {"0x0"    {:is-active                    true
                                    :public?                      false
                                    :unviewed-messages-count      0
                                    :loaded-unviewed-messages-ids #{}}
                          "status" {:is-active                    true
                                    :public?                      true
                                    :unviewed-messages-count      0
                                    :loaded-unviewed-messages-ids #{}}}}})))))
