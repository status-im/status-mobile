(ns status-im.test.chat.models
  (:require [cljs.test :refer-macros [deftest is testing]]
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
