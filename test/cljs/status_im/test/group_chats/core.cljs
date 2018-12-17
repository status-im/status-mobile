(ns status-im.test.group-chats.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.clocks :as utils.clocks]
            [status-im.utils.config :as config]
            [status-im.group-chats.core :as group-chats]))

(def random-id "685a9351-417e-587c-8bc1-191ac2a57ef8")
(def chat-name "chat-name")

(def member-1 "member-1")
(def member-2 "member-2")
(def member-3 "member-3")
(def member-4 "member-4")

(def admin member-1)

(def chat-id (str random-id admin))

(def initial-message {:chat-id chat-id
                      :membership-updates [{:from admin
                                            :events [{:type "chat-created"
                                                      :name "chat-name"
                                                      :clock-value 1}
                                                     {:type "members-added"
                                                      :clock-value 3
                                                      :members [member-2 member-3]}]}]})

(deftest get-last-clock-value-test
  (is (= 3 (group-chats/get-last-clock-value {:db {:chats {chat-id {:last-clock-value 3}}}} chat-id))))

(deftest handle-group-membership-update-test
  (with-redefs [config/group-chats-enabled? true]
    (testing "a brand new chat"
      (let [actual   (->
                      (group-chats/handle-membership-update {:now 0 :db {}} initial-message "payload" admin)
                      :db
                      :chats
                      (get chat-id))]
        (testing "it creates a new chat"
          (is actual))
        (testing "it sets the right chat-name"
          (is (= "chat-name"
                 (:name actual))))
        (testing "it sets the right chat-id"
          (is (= chat-id
                 (:chat-id actual))))
        (testing "it sets the right participants"
          (is (= #{member-1 member-2 member-3}
                 (:contacts actual))))
        (testing "it sets the updates"
          (is (= (:membership-updates initial-message)
                 (:membership-updates actual))))
        (testing "it sets the right admins"
          (is (= #{admin}
                 (:admins actual))))
        (testing "it adds a system message"
          (is (= 3 (count (:messages actual)))))
        (testing "it adds the right text"
          (is (= ["group-chat-created"
                  "group-chat-member-added"
                  "group-chat-member-added"]
                 (map (comp :text :content) (sort-by :clock-value (vals (:messages actual)))))))))
    (testing "a chat with the wrong id"
      (let [bad-chat-id (str random-id member-2)
            actual      (->
                         (group-chats/handle-membership-update
                          {:now 0 :db {}}
                          (assoc initial-message :chat-id bad-chat-id)
                          "payload"
                          admin)
                         :db
                         :chats
                         (get bad-chat-id))]
        (testing "it does not create a chat"
          (is (not actual)))))
    (testing "an already existing chat"
      (let [cofx (assoc
                  (group-chats/handle-membership-update {:now 0 :db {}} initial-message "payload" admin)
                  :now 0)]
        (testing "the message has already been received"
          (let [actual (group-chats/handle-membership-update cofx initial-message "payload" admin)]
            (testing "it noops"
              (is (=
                   (get-in cofx [:db :chats chat-id])
                   (get-in actual [:db :chats chat-id]))))))
        (testing "a new message comes in"
          (let [actual (group-chats/handle-membership-update cofx
                                                             {:chat-id chat-id
                                                              :membership-updates [{:from member-1
                                                                                    :events [{:type "chat-created"
                                                                                              :clock-value 1
                                                                                              :name "group-name"}
                                                                                             {:type "admins-added"
                                                                                              :clock-value 10
                                                                                              :members [member-2]}
                                                                                             {:type "admin-removed"
                                                                                              :clock-value 11
                                                                                              :member member-1}]}
                                                                                   {:from member-2
                                                                                    :events [{:type "member-removed"
                                                                                              :clock-value 12
                                                                                              :member member-3}
                                                                                             {:type "members-added"
                                                                                              :clock-value 12
                                                                                              :members [member-4]}
                                                                                             {:type "name-changed"
                                                                                              :clock-value 13
                                                                                              :name "new-name"}]}]}
                                                             "payload"
                                                             member-3)
                actual-chat (get-in actual [:db :chats chat-id])]
            (testing "the chat is updated"
              (is actual-chat))
            (testing "admins are updated"
              (is (= #{member-2} (:admins actual-chat))))
            (testing "members are updated"
              (is (= #{member-1 member-2 member-4} (:contacts actual-chat))))
            (testing "the name is updated"
              (is (= "new-name" (:name actual-chat))))
            (testing "it adds a system message"
              (is (= 7 (count (:messages actual-chat)))))
            (testing "it sets the right text"
              (is (= ["group-chat-created"
                      "group-chat-member-added"
                      "group-chat-member-added"
                      "group-chat-admin-added"
                      "group-chat-member-added"
                      "group-chat-member-removed"
                      "group-chat-name-changed"]
                     (map (comp :text :content) (sort-by :clock-value (vals (:messages actual-chat)))))))))))))

(deftest build-group-test
  (testing "only adds"
    (let [events [{:type    "chat-created"
                   :clock-value 0
                   :name    "chat-name"
                   :from    "1"}
                  {:type    "members-added"
                   :clock-value 1
                   :from    "1"
                   :members  ["2"]}
                  {:type    "admins-added"
                   :clock-value 2
                   :from    "1"
                   :members  ["2"]}
                  {:type    "members-added"
                   :clock-value 3
                   :from    "2"
                   :members  ["3"]}
                  {:type    "member-joined"
                   :clock-value 4
                   :from    "3"
                   :member  "3"}]
          expected {:name   "chat-name"
                    :created-at 0
                    "2" {:added 1
                         :admin-added 2}
                    "3" {:added 3
                         :joined 4}
                    :admins #{"1" "2"}
                    :members-joined #{"1" "3"}
                    :contacts #{"1" "2" "3"}}]
      (is (= expected (group-chats/build-group events)))))
  (testing "adds and removes"
    (let [events [{:type    "chat-created"
                   :clock-value 0
                   :name  "chat-name"
                   :from    "1"}
                  {:type    "members-added"
                   :clock-value 1
                   :from    "1"
                   :members  ["2"]}
                  {:type    "member-joined"
                   :clock-value 3
                   :from    "2"
                   :member  "2"}
                  {:type    "admins-added"
                   :clock-value 4
                   :from    "1"
                   :members  ["2"]}
                  {:type    "admin-removed"
                   :clock-value 5
                   :from    "2"
                   :member  "2"}
                  {:type    "member-removed"
                   :clock-value 6
                   :from   "2"
                   :member "2"}]
          expected {:name "chat-name"
                    :created-at 0
                    "2" {:added 1
                         :joined 3
                         :admin-added 4
                         :admin-removed 5
                         :removed 6}
                    :admins #{"1"}
                    :members-joined #{"1"}
                    :contacts #{"1"}}]
      (is (= expected (group-chats/build-group events)))))
  (testing "an admin removing themselves"
    (let [events [{:type    "chat-created"
                   :clock-value 0
                   :name  "chat-name"
                   :from    "1"}
                  {:type    "members-added"
                   :clock-value 1
                   :from    "1"
                   :members  ["2"]}
                  {:type    "admins-added"
                   :clock-value 2
                   :from    "1"
                   :members  ["2"]}
                  {:type    "member-removed"
                   :clock-value 3
                   :from   "2"
                   :member "2"}]
          expected {:name "chat-name"
                    :created-at 0
                    :members-joined #{"1"}
                    "2" {:added 1
                         :admin-added 2
                         :removed 3}
                    :admins #{"1"}
                    :contacts #{"1"}}]
      (is (= expected (group-chats/build-group events)))))
  (testing "name changed"
    (let [events [{:type    "chat-created"
                   :clock-value 0
                   :name  "chat-name"
                   :from    "1"}
                  {:type    "members-added"
                   :clock-value 1
                   :from    "1"
                   :members  ["2"]}
                  {:type    "admins-added"
                   :clock-value 2
                   :from    "1"
                   :members  ["2"]}
                  {:type    "name-changed"
                   :clock-value 3
                   :from    "2"
                   :name  "new-name"}]
          expected {:name "new-name"
                    :created-at 0
                    :members-joined #{"1"}
                    :name-changed-by "2"
                    :name-changed-at 3
                    "2" {:added 1
                         :admin-added 2}
                    :admins #{"1" "2"}
                    :contacts #{"1" "2"}}]
      (is (= expected (group-chats/build-group events)))))
  (testing "invalid events"
    (let [events [{:type    "chat-created"
                   :name "chat-name"
                   :clock-value 0
                   :from    "1"}
                  {:type    "admins-added" ; can't make an admin a user not in the group
                   :clock-value 1
                   :from    "1"
                   :members  ["non-existing"]}
                  {:type    "members-added"
                   :clock-value 2
                   :from    "1"
                   :members  ["2"]}
                  {:type    "member-joined" ; non-invited user joining
                   :clock-value 2
                   :from    "non-invited"
                   :member  "non-invited"}
                  {:type    "admins-added"
                   :clock-value 3
                   :from    "1"
                   :members  ["2"]}
                  {:type    "members-added"
                   :clock-value 4
                   :from    "2"
                   :members  ["3"]}
                  {:type    "admin-removed" ; can't remove an admin from admins unless it's the same user
                   :clock-value 5
                   :from    "1"
                   :member  "2"}
                  {:type    "member-joined"
                   :clock-value 5
                   :from    "2"
                   :member  "2"}
                  {:type    "member-removed" ; can't remove an admin from the group
                   :clock-value 6
                   :from    "1"
                   :member  "2"}
                  {:type    "members-added"
                   :clock-value 7
                   :from    "2"
                   :members  ["4"]}
                  {:type    "member-joined"
                   :clock-value 8
                   :from    "4"
                   :member  "4"}
                  {:type    "member-removed"
                   :clock-value 9
                   :from    "1"
                   :member  "4"}
                  {:type    "member-joined" ; join after being removed
                   :clock-value 10
                   :from    "4"
                   :member  "4"}]
          expected {:name "chat-name"
                    :members-joined #{"1" "2"}
                    :created-at 0
                    "2" {:added 2
                         :admin-added 3
                         :joined 5}
                    "3" {:added 4}
                    "4" {:added 7
                         :joined 8
                         :removed 9}
                    :admins #{"1" "2"}
                    :contacts #{"1" "2" "3"}}]
      (is (= expected (group-chats/build-group events)))))
  (testing "out of order-events"
    (let [events [{:type    "chat-created"
                   :name    "chat-name"
                   :clock-value 0
                   :from    "1"}
                  {:type    "admins-added"
                   :clock-value 2
                   :from    "1"
                   :members  ["2"]}
                  {:type    "members-added"
                   :clock-value 1
                   :from    "1"
                   :members  ["2"]}
                  {:type    "members-added"
                   :clock-value 3
                   :from    "2"
                   :members  ["3"]}]
          expected {:name "chat-name"
                    :created-at 0
                    :members-joined #{"1"}
                    "2" {:added 1
                         :admin-added 2}
                    "3" {:added 3}
                    :admins #{"1" "2"}
                    :contacts #{"1" "2" "3"}}]
      (is (= expected (group-chats/build-group events))))))

(deftest valid-event-test
  (let [multi-admin-group {:admins #{"1" "2"}
                           :contacts #{"1" "2" "3"}}
        single-admin-group {:admins #{"1"}
                            :contacts #{"1" "2" "3"}}]
    (testing "members-added"
      (testing "admins can add members"
        (is (group-chats/valid-event? multi-admin-group
                                      {:type "members-added" :clock-value 6 :from "1" :members ["4"]})))
      (testing "non-admin members cannot add members"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "members-added" :clock-value 6 :from "3" :members ["4"]})))))
    (testing "admins-added"
      (testing "admins can make other member admins"
        (is (group-chats/valid-event? multi-admin-group
                                      {:type "admins-added" :clock-value 6 :from "1" :members ["3"]})))
      (testing "non-admins can't make other member admins"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "admins-added" :clock-value 6 :from "3" :members ["3"]}))))
      (testing "non-existing users can't be made admin"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "admins-added" :clock-value 6 :from "1" :members ["not-existing"]})))))
    (testing "member-removed"
      (testing "admins can remove non-admin members"
        (is (group-chats/valid-event? multi-admin-group
                                      {:type "member-removed" :clock-value 6 :from "1" :member "3"})))
      (testing "admins can remove themselves"
        (is (group-chats/valid-event? multi-admin-group
                                      {:type "member-removed" :clock-value 6 :from "1" :member "1"})))
      (testing "participants non-admin can remove themselves"
        (is (group-chats/valid-event? multi-admin-group
                                      {:type "member-removed" :clock-value 6 :from "3" :member "3"})))
      (testing "non-admin can't remove other members"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "member-removed" :clock-value 6 :from "3" :member "1"})))))
    (testing "admin-removed"
      (testing "admins can remove themselves"
        (is (group-chats/valid-event? multi-admin-group
                                      {:type "admin-removed" :clock-value 6 :from "1" :member "1"})))
      (testing "admins can't remove other admins"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "admin-removed" :clock-value 6 :from "1" :member "2"}))))
      (testing "participants non-admin can't remove other admins"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "admin-removed" :clock-value 6 :from "3" :member "1"}))))
      (testing "the last admin can be removed"
        (is (group-chats/valid-event? single-admin-group
                                      {:type "admin-removed" :clock-value 6 :from "1" :member "1"})))
      (testing "name-changed"
        (testing "a change from an admin"
          (is (group-chats/valid-event? multi-admin-group
                                        {:type "name-changed" :clock-value 6 :from "1" :name "new-name"}))))
      (testing "a change from an non-admin"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "name-changed" :clock-value 6 :from "3" :name "new-name"}))))
      (testing "an empty name"
        (is (not (group-chats/valid-event? multi-admin-group
                                           {:type "name-changed" :clock-value 6 :from "1" :name "   "})))))))

(deftest create-test
  (testing "create a new chat"
    (with-redefs [utils.clocks/send inc]
      (let [cofx {:random-guid-generator (constantly "random")
                  :db {:account/account {:public-key "me"}
                       :group/selected-contacts #{"1" "2"}}}]
        (is (= {:chat-id "randomme"
                :from    "me"
                :events [{:type "chat-created"
                          :clock-value 1
                          :name "group-name"}
                         {:type "members-added"
                          :clock-value 2
                          :members #{"1" "2"}}]}
               (:group-chats/sign-membership (group-chats/create cofx "group-name"))))))))

(deftest signature-pairs-test
  (let [event-1 {:from "1"
                 :signature "signature-1"
                 :events [{:type "a" :name "a" :clock-value 1}
                          {:type "b" :name "b" :clock-value 2}]}
        event-2 {:from "2"
                 :signature "signature-2"
                 :events [{:type "c" :name "c" :clock-value 1}
                          {:type "d" :name "d" :clock-value 2}]}
        message {:chat-id "randomme"

                 :membership-updates [event-1
                                      event-2]}
        expected (js/JSON.stringify
                  (clj->js [[(group-chats/signature-material "randomme" (:events event-1))
                             "signature-1"]
                            [(group-chats/signature-material "randomme" (:events event-2))
                             "signature-2"]]))]

    (is (= expected (group-chats/signature-pairs message)))))

(deftest signature-material-test
  (is (= (js/JSON.stringify (clj->js [[[["a" "a-value"]
                                        ["b" "b-value"]
                                        ["c" "c-value"]]
                                       [["a" "a-value"]
                                        ["e" "e-value"]]] "chat-id"]))
         (group-chats/signature-material "chat-id" [{:b "b-value"
                                                     :a "a-value"
                                                     :c "c-value"}
                                                    {:e "e-value"
                                                     :a "a-value"}]))))

(deftest remove-group-chat-test
  (with-redefs [utils.clocks/send inc]
    (let [cofx {:db {:chats {chat-id {:admins #{member-1 member-2}
                                      :name "chat-name"
                                      :chat-id chat-id
                                      :last-clock-value 3
                                      :is-active true
                                      :group-chat true
                                      :contacts #{member-1 member-2 member-3}
                                      :membership-updates (:membership-updates initial-message)}}}}]
      (testing "removing a member"
        (is (= {:from member-3
                :chat-id chat-id
                :events [{:type "member-removed" :member member-3 :clock-value 4}]}
               (:group-chats/sign-membership
                (group-chats/remove
                 (assoc-in cofx [:db :account/account :public-key] member-3)
                 chat-id)))))
      (testing "removing an admin"
        (is (= {:from member-1
                :chat-id chat-id
                :events [{:type "member-removed" :member member-1 :clock-value 4}]}
               (:group-chats/sign-membership
                (group-chats/remove
                 (assoc-in cofx [:db :account/account :public-key] member-1)
                 chat-id))))))))

(deftest add-members-test
  (with-redefs [utils.clocks/send inc]
    (testing "add-members"
      (let [cofx {:db {:current-chat-id chat-id
                       :selected-participants ["new-member"]
                       :account/account {:public-key "me"}
                       :chats {chat-id {:last-clock-value   1
                                        :membership-updates [{:events [{:clock-value 1}]}]}}}}]
        (is (= {:chat-id chat-id
                :from "me"
                :events [{:type "members-added"
                          :clock-value 2
                          :members ["new-member"]}]}
               (:group-chats/sign-membership (group-chats/add-members cofx))))))))

(deftest remove-member-test
  (with-redefs [utils.clocks/send inc]
    (testing "remove-member"
      (let [cofx {:db {:account/account {:public-key "me"}
                       :chats {chat-id {:admins #{"me"}
                                        :last-clock-value 1
                                        :contacts #{"member"}
                                        :membership-updates [{:events [{:clock-value 1}]}]}}}}]
        (is (= {:chat-id chat-id
                :from "me"
                :events [{:type "member-removed"
                          :clock-value 2
                          :member "member"}]}
               (:group-chats/sign-membership (group-chats/remove-member cofx chat-id "member"))))))))

(deftest make-admin-test
  (with-redefs [utils.clocks/send inc]
    (testing "make-admin"
      (let [cofx {:db {:account/account {:public-key "me"}
                       :chats {chat-id {:admins #{"me"}
                                        :last-clock-value 1
                                        :contacts #{"member"}
                                        :membership-updates [{:events [{:clock-value 1}]}]}}}}]
        (is (= {:chat-id chat-id
                :from "me"
                :events [{:type "admins-added"
                          :clock-value 2
                          :members ["member"]}]}
               (:group-chats/sign-membership (group-chats/make-admin cofx chat-id "member"))))))))
