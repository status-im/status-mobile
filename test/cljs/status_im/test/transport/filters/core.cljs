(ns status-im.test.transport.filters.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.fx :as fx]
            [status-im.mailserver.topics :as mailserver.topics]
            [status-im.transport.filters.core :as transport.filters]))

(def me "me")
(def member-1 "member-1")
(def member-2 "member-2")
(def chat-id "chat-id")

(deftest stop-listening
  (testing "the user is in our contacts"
    (testing "it does not remove filters"
      (is (not (transport.filters/stop-listening
                {:db {:contacts/contacts
                      {chat-id {:system-tags #{:contact/added}}}}}
                chat-id)))))
  (testing "the user is not in our contacts"
    (testing "the user is not in any group chats or 1-to1-"
      (testing "it removes the filters"
        (let [fx (transport.filters/stop-listening {:db {:filter/filters
                                                         {"a" {:chat-id chat-id :filter-id "a"}
                                                          "b" {:chat-id chat-id :negotiated? true}}}}
                                                   chat-id)]
          (is fx)
          (is (= fx {:filters/remove-filters [{:chat-id chat-id :filter-id "a"}]})))))
    (testing "the user is still in some group chats"
      (testing "we joined, and group chat is active it does not remove filters"
        (let [fx (transport.filters/stop-listening {:db {:multiaccount {:public-key me}
                                                         :chats
                                                         {chat-id {:is-active true
                                                                   :members-joined #{me}
                                                                   :members #{member-1}}}
                                                         :filter/filters
                                                         {member-1 {}}}}
                                                   member-1)]
          (is (not fx))))
      (testing "we didn't join, it removes transport"
        (let [fx (transport.filters/stop-listening {:db {:multiaccount {:public-key me}
                                                         :chats
                                                         {chat-id {:is-active true
                                                                   :members-joined #{member-1}
                                                                   :members #{member-1}}}
                                                         :filter/filters
                                                         {member-1 {:chat-id member-1 :filter-id "a"}}}}
                                                   member-1)]
          (is fx)
          (is (= fx {:filters/remove-filters [{:chat-id member-1 :filter-id "a"}]})))))
    (testing "we have a 1-to-1 chat with the user"
      (testing "it does not remove filter"
        (let [fx (transport.filters/stop-listening {:db {:chats
                                                         {member-1 {:is-active true}}}}
                                                   member-1)]
          (is (not fx)))))))

(deftest chats->filter-requests
  (testing "a single one to one chat"
    (is (= [{:ChatID "0xchat-id"
             :OneToOne true
             :Identity "chat-id"}]
           (transport.filters/chats->filter-requests [{:is-active true
                                                       :group-chat false
                                                       :chat-id "0xchat-id"}]))))
  (testing "a single public chat"
    (is (= [{:ChatID "chat-id"
             :OneToOne false}]
           (transport.filters/chats->filter-requests [{:is-active true
                                                       :group-chat true
                                                       :public? true
                                                       :chat-id "chat-id"}]))))
  (testing "a single group chat"
    (is (= [{:ChatID "0xchat-id-2"
             :OneToOne true
             :Identity "chat-id-2"}
            {:ChatID "0xchat-id-1"
             :OneToOne true
             :Identity "chat-id-1"}]
           (transport.filters/chats->filter-requests [{:is-active true
                                                       :group-chat true
                                                       :members-joined #{"0xchat-id-1" "0xchat-id-2"}
                                                       :chat-id "chat-id"}])))))

(deftest contacts->filters
  (testing "converting contacts to filters"
    (is (= [{:ChatID "0xchat-id-2"
             :OneToOne true
             :Identity "chat-id-2"}]
           (transport.filters/contacts->filter-requests [{:system-tags #{}
                                                          :public-key "0xchat-id-1"}
                                                         {:system-tags #{:contact/added}
                                                          :public-key "0xchat-id-2"}])))))

(deftest load-member
  (testing "it returns fx for a member"
    (is (= {:filters/load-filters [{:ChatID "0xchat-id-2"
                                    :OneToOne true
                                    :Identity "chat-id-2"}]}
           (transport.filters/load-member {:db {}} "0xchat-id-2"))))
  (testing "merging fx"
    (is (=
         {:db {}
          :filters/load-filters [{:ChatID "0xchat-id-1"
                                  :OneToOne true
                                  :Identity "chat-id-1"}
                                 {:ChatID "0xchat-id-2"
                                  :OneToOne true
                                  :Identity "chat-id-2"}]}
         (apply fx/merge {:db {}}
                (map transport.filters/load-member ["0xchat-id-1" "0xchat-id-2"]))))))

(deftest add-filter-to-db
  (with-redefs [mailserver.topics/upsert (fn [{:keys [db]} r] {:db (assoc db :upsert r)})]
    (let [expected {:db {:filter/chat-ids #{"chat-id"}
                         :filter/filters {"filter-id" {:filter-id "filter-id"
                                                       :discovery? false
                                                       :chat-id "chat-id"
                                                       :negotiated? false
                                                       :topic "topic"}}
                         :upsert {:topic "topic"
                                  :negotiated? false
                                  :discovery? false
                                  :chat-ids #{"chat-id"}
                                  :filter-ids #{"filter-id"}}}}]
      (is (= expected
             (transport.filters/add-filter-to-db {:db {}} {:filter-id "filter-id"
                                                           :discovery? false
                                                           :chat-id "chat-id"
                                                           :negotiated? false
                                                           :topic "topic"}))))))

(deftest new-filters?
  (testing "new-filters?"
    (let [db {:filter/filters {"a" {}
                               "b" {}
                               "c" {}}}]
      (is (not (transport.filters/new-filters? db [{:filter-id "a"}
                                                   {:filter-id "b"}
                                                   {:filter-id "c"}])))
      (is (not (transport.filters/new-filters? db [{:filter-id "a"}])))
      (is (transport.filters/new-filters? db [{:filter-id "d"}]))
      (is (transport.filters/new-filters? db [{:filter-id "a"}
                                              {:filter-id "d"}])))))
