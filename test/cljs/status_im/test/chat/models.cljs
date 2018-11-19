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
          response      (chat/upsert-chat cofx chat-props)
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
          response      (chat/upsert-chat cofx chat-props)
          actual-chat   (get-in response [:db :chats chat-id])
          store-chat-fx (:data-store/tx response)]
      (testing "it adds the chat to the chats collection"
        (is actual-chat))
      (testing "it adds the extra props"
        (is (= "some" (:extra-prop actual-chat))))
      (testing "it updates existins props"
        (is (= "new-name" (:name actual-chat))))
      (testing "it adds the fx to store a chat"
        (is store-chat-fx)))))

(deftest add-group-chat
  (let [chat-id "chat-id"
        chat-name "chat-name"
        admin "admin"
        participants ["a"]
        fx (chat/add-group-chat {:db {}} chat-id chat-name admin participants)
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
        fx (chat/add-public-chat {:db {}} topic)
        store-fx   (:data-store/tx fx)
        chat (get-in fx [:db :chats topic])]
    (testing "it saves the chat in the database"
      (is store-fx))
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
        cofx    {:db {:chats {chat-id {:messages              {"1" {:clock-value 1}
                                                               "2" {:clock-value 10}
                                                               "3" {:clock-value 2}}
                                       :unviewed-messages      #{"3"}
                                       :not-loaded-message-ids #{"2" "3"}}}}}]
    (testing "it deletes all the messages"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (= {} (get-in actual [:db :chats chat-id :messages])))))
    (testing "it deletes unviewed messages set"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (= #{} (get-in actual [:db :chats chat-id :unviewed-messages])))))
    (testing "it deletes not loaded message ids set"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (= #{} (get-in actual [:db :chats chat-id :not-loaded-message-ids])))))
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
    (testing "it adds the relevant transactions for realm"
      (let [actual (chat/clear-history cofx chat-id)]
        (is (:data-store/tx actual))
        (is (= 2 (count (:data-store/tx actual))))))))

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
    (testing "it does not remove it from transport if it's a one-to-one"
      (let [actual (chat/remove-chat cofx chat-id)]
        (is (get-in actual [:db :transport/chats chat-id]))))
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
  {:account/account {:public-key "me"}
   :chats {"status" {:public? true
                     :group-chat true
                     :unviewed-messages #{"6" "5" "4" "3" "2" "1"}
                     :message-statuses {"6" {"me" {:message-id "6"
                                                   :chat-id "status"
                                                   :public-key "me"
                                                   :status :received}}
                                        "5" {"me" {:message-id "5"
                                                   :chat-id "status"
                                                   :public-key "me"
                                                   :status :received}}
                                        "4" {"me" {:message-id "4"
                                                   :chat-id "status"
                                                   :public-key "me"
                                                   :status :received}}}}
           "opened" {:unviewed-messages #{}
                     :message-statuses {"1" {"me" {:message-id "1"
                                                   :chat-id "opened"
                                                   :public-key "me"
                                                   :status :seen}}}}
           "1-1"    {:unviewed-messages #{"6" "5" "4" "3" "2" "1"}
                     :message-statuses {"6" {"me" {:message-id "6"
                                                   :chat-id "status"
                                                   :public-key "me"
                                                   :status :received}}
                                        "5" {"me" {:message-id "5"
                                                   :chat-id "status"
                                                   :public-key "me"
                                                   :status :received}}
                                        "4" {"me" {:message-id "4"
                                                   :chat-id "status"
                                                   :public-key "me"
                                                   :status :received}}}}}})

(deftest mark-messages-seen
  (testing "Marking messages seen correctly marks loaded messages as seen and updates absolute unviewed set"
    (let [fx (chat/mark-messages-seen {:db test-db} "status")
          me (get-in test-db [:account/account :public-key])]
      (is (= '(:seen :seen :seen)
             (map (fn [[_ v]]
                    (get-in v [me :status]))
                  (get-in fx [:db :chats "status" :message-statuses]))))
      (is (= 1 (count (:data-store/tx fx))))
      (is (= nil (:shh/post fx))) ;; for public chats, no confirmation is sent out
      (is (= #{"3" "2" "1"} (get-in fx [:db :chats "status" :unviewed-messages])))))

  (testing "With empty unviewed set, no effects are produced"
    (is (= nil (chat/mark-messages-seen {:db test-db} "opened"))))

  (testing "For 1-1 chat, we send seen messages confirmation to the recipient as well"
    (is (= #{"4" "5" "6"}
           (set (get-in (chat/mark-messages-seen {:db test-db} "1-1")
                        [:shh/post 0 :message :payload :message-ids]))))))

(deftest update-dock-badge-label
  (testing "When user has unseen private messages"
    (is (= {:set-dock-badge-label 3}
           (chat/update-dock-badge-label {:db {:chats {"0x0"    {:is-active         true
                                                                 :public?           false
                                                                 :unviewed-messages #{1 2 3}}
                                                       "status" {:is-active         true
                                                                 :public?           true
                                                                 :unviewed-messages #{1 2}}}}}))))
  (testing "When user has unseen public messages and no unseen private messages"
    (is (= {:set-dock-badge-label "•"}
           (chat/update-dock-badge-label {:db {:chats {"0x0"    {:is-active         true
                                                                 :public?           false
                                                                 :unviewed-messages #{}}
                                                       "status" {:is-active         true
                                                                 :public?           true
                                                                 :unviewed-messages #{1 2}}}}}))))
  (testing "When user has no unseen messages"
    (is (= {:set-dock-badge-label nil}
           (chat/update-dock-badge-label {:db {:chats {"0x0"    {:is-active         true
                                                                 :public?           false
                                                                 :unviewed-messages #{}}
                                                       "status" {:is-active         true
                                                                 :public?           true
                                                                 :unviewed-messages #{}}}}})))))
