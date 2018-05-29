(ns status-im.test.chat.models
  (:require [cljs.test :refer-macros [deftest is testing]]
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
          response      (chat/upsert-chat chat-props cofx)
          actual-chat   (get-in response [:db :chats chat-id])
          store-chat-fx (:data-store/tx response)]
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
        (is (= chat-id (-> actual-chat :contacts first))))
      (testing "it adds the fx to store a chat"
        (is store-chat-fx))))
  (testing "upserting an existing chat"
    (let [chat-id        "some-chat-id"
          chat-props     {:chat-id chat-id
                          :name "new-name"
                          :extra-prop "some"}
          cofx           {:db {:chats {chat-id {:is-active true
                                                :name "old-name"}}}}
          response      (chat/upsert-chat chat-props cofx)
          actual-chat   (get-in response [:db :chats chat-id])
          store-chat-fx (:data-store/tx response)]
      (testing "it adds the chat to the chats collection"
        (is actual-chat))
      (testing "it adds the extra props"
        (is (= "some" (:extra-prop actual-chat))))
      (testing "it updates existins props"
        (is (= "new-name" (:name actual-chat))))
      (testing "it adds the fx to store a chat"
        (is store-chat-fx))))
  (testing "upserting a deleted chat"
    (let [chat-id        "some-chat-id"
          contact-name   "contact-name"
          chat-props     {:chat-id chat-id
                          :name "new-name"
                          :extra-prop "some"}
          cofx           {:some-cofx "b"
                          :db {:chats {chat-id {:is-active false
                                                :name "old-name"}}}}]
      (testing "it updates it if is-active is passed"
        (is (get-in (chat/upsert-chat (assoc chat-props :is-active true) cofx) [:db :chats chat-id :is-active])))
      (testing "it returns the db unchanged"
        (is (= {:db (:db cofx)} (chat/upsert-chat chat-props cofx)))))))

(deftest add-group-chat
  (let [chat-id "chat-id"
        chat-name "chat-name"
        admin "admin"
        participants ["a"]
        fx (chat/add-group-chat chat-id chat-name admin participants {})
        store-fx   (:data-store/tx fx)
        group-chat (get-in fx [:db :chats chat-id])]
    (testing "it saves the chat in the database"
      (is store-fx))
    (testing "it sets the name"
      (is (= chat-name (:name group-chat))))
    (testing "it sets the admin"
      (is (= admin (:group-admin group-chat))))
    (testing "it sets the participants"
      (is (= participants (:contacts group-chat))))
    (testing "it sets the chat-id"
      (is (= chat-id (:chat-id group-chat))))
    (testing "it sets the group-chat flag"
      (is (:group-chat group-chat)))
    (testing "it does not sets the public flag"
      (is (not (:public? group-chat))))))

(deftest add-public-chat
  (let [topic "topic"
        fx (chat/add-public-chat topic {})
        store-fx   (:data-store/tx fx)
        chat (get-in fx [:db :chats topic])]
    (testing "it saves the chat in the database"
      (is store-fx))
    (testing "it sets the name"
      (is (= topic (:name chat))))
    (testing "it sets the participants"
      (is (= [] (:contacts chat))))
    (testing "it sets the chat-id"
      (is (= topic (:chat-id chat))))
    (testing "it sets the group-chat flag"
      (is (:group-chat chat)))
    (testing "it does not sets the public flag"
      (is (:public? chat)))))

(deftest clear-history-test
  (let [chat-id "1"
        cofx    {:db {:chats {chat-id {:message-groups {:something "a"}
                                       :messages       {"1" {:clock-value 1}
                                                        "2" {:clock-value 10}
                                                        "3" {:clock-value 2}}}}}}]
    (testing "it deletes all the messages"
      (let [actual (chat/clear-history chat-id cofx)]
        (is (= {} (get-in actual [:db :chats chat-id :messages])))))
    (testing "it deletes all the message groups"
      (let [actual (chat/clear-history chat-id cofx)]
        (is (= {} (get-in actual [:db :chats chat-id :message-groups])))))
    (testing "it sets a deleted-at-clock-value equal to the last message clock-value"
      (let [actual (chat/clear-history chat-id cofx)]
        (is (= 10 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it does not override the deleted-at-clock-value when there are no messages"
      (let [actual (chat/clear-history chat-id
                                       (update-in cofx
                                                  [:db :chats chat-id]
                                                  assoc
                                                  :messages {}
                                                  :deleted-at-clock-value 100))]
        (is (= 100 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it set the deleted-at-clock-value to now the chat has no messages nor previous deleted-at"
      (with-redefs [utils.clocks/send (constantly 42)]
        (let [actual (chat/clear-history chat-id
                                         (update-in cofx
                                                    [:db :chats chat-id]
                                                    assoc
                                                    :messages {}))]
          (is (= 42 (get-in actual [:db :chats chat-id :deleted-at-clock-value]))))))
    (testing "it adds the relevant transactions for realm"
      (let [actual (chat/clear-history chat-id cofx)]
        (is (:data-store/tx actual))
        (is (= 2 (count (:data-store/tx actual))))))))

(deftest remove-chat-test
  (let [chat-id "1"
        cofx    {:db {:transport/chats {chat-id {}}
                      :chats {chat-id {:messages {"1" {:clock-value 1}
                                                  "2" {:clock-value 10}
                                                  "3" {:clock-value 2}}}}}}]
    (testing "it deletes all the messages"
      (let [actual (chat/remove-chat chat-id cofx)]
        (is (= {} (get-in actual [:db :chats chat-id :messages])))))
    (testing "it sets a deleted-at-clock-value equal to the last message clock-value"
      (let [actual (chat/remove-chat chat-id cofx)]
        (is (= 10 (get-in actual [:db :chats chat-id :deleted-at-clock-value])))))
    (testing "it sets the chat as inactive"
      (let [actual (chat/remove-chat chat-id cofx)]
        (is (= false (get-in actual [:db :chats chat-id :is-active])))))
    (testing "it removes it from transport if it's a public chat"
      (let [actual (chat/remove-chat chat-id
                                     (update-in
                                      cofx
                                      [:db :chats chat-id]
                                      assoc
                                      :group-chat true
                                      :public? true))]
        (is (not (get-in actual [:db :transport/chats chat-id])))))
    (testing "it sends a leave group request if it's a group-chat"
      (let [actual (chat/remove-chat chat-id
                                     (assoc-in
                                      cofx
                                      [:db :chats chat-id :group-chat]
                                      true))]
        (is (:shh/post  actual))
        (testing "it does not remove transport, only after send is successful"
          (is (get-in actual [:db :transport/chats chat-id])))))
    (testing "it does not remove it from transport if it's a one-to-one"
      (let [actual (chat/remove-chat chat-id cofx)]
        (is (get-in actual [:db :transport/chats chat-id]))))
    (testing "it adds the relevant transactions for realm"
      (let [actual (chat/remove-chat chat-id cofx)]
        (is (:data-store/tx actual))
        (is (= 3 (count (:data-store/tx actual))))))))

(deftest multi-user-chat?
  (let [chat-id "1"]
    (testing "it returns true if it's a group chat"
      (let [cofx {:db {:chats {chat-id {:group-chat true}}}}]
        (is (chat/multi-user-chat? chat-id cofx))))
    (testing "it returns true if it's a public chat"
      (let [cofx {:db {:chats {chat-id {:public? true :group-chat true}}}}]
        (is (chat/multi-user-chat? chat-id cofx))))
    (testing "it returns false if it's a 1-to-1 chat"
      (let [cofx {:db {:chats {chat-id {}}}}]
        (is (not (chat/multi-user-chat? chat-id cofx)))))))

(deftest group-chat?
  (let [chat-id "1"]
    (testing "it returns true if it's a group chat"
      (let [cofx {:db {:chats {chat-id {:group-chat true}}}}]
        (is (chat/group-chat? chat-id cofx))))
    (testing "it returns false if it's a public chat"
      (let [cofx {:db {:chats {chat-id {:public? true :group-chat true}}}}]
        (is (not (chat/group-chat? chat-id cofx)))))
    (testing "it returns false if it's a 1-to-1 chat"
      (let [cofx {:db {:chats {chat-id {}}}}]
        (is (not (chat/group-chat? chat-id cofx)))))))
