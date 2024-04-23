(ns status-im.subs.chats-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    [status-im.constants :as constants]
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def public-key "0xpk")
(def multiaccount {:public-key public-key})
(def chat-id "1")
(def chat {:chat-id chat-id})
(def community-id "community-1")
(def community {:community-id community-id})

(def private-group-chat
  (assoc
   chat
   :members    #{{:id public-key}}
   :group-chat true
   :chat-type  constants/private-group-chat-type))

(def community-chat
  (assoc
   chat
   :group-chat   true
   :community-id community-id
   :chat-type    constants/community-chat-type))

(def one-to-one-chat
  (assoc
   chat
   :chat-type
   constants/one-to-one-chat-type))

(h/deftest-sub :chats/current-chat
  [sub-name]
  (testing "private group chat, user is a member"
    (let [chats {chat-id private-group-chat}]
      (swap! rf-db/app-db assoc
        :profile/profile multiaccount
        :current-chat-id chat-id
        :chats           chats)
      (is (true? (:able-to-send-message? (rf/sub [sub-name]))))))
  (testing "private group chat, user is not member"
    (let [chats {chat-id (dissoc private-group-chat :members)}]
      (swap! rf-db/app-db assoc
        :profile/profile multiaccount
        :current-chat-id chat-id
        :chats           chats)
      (is (not (:able-to-send-message? (rf/sub [sub-name]))))))
  (testing "one to one chat, mutual contacts"
    (let [chats {chat-id one-to-one-chat}]
      (swap! rf-db/app-db assoc
        :contacts/contacts {chat-id {:contact-request-state constants/contact-request-state-mutual}}
        :profile/profile   multiaccount
        :current-chat-id   chat-id
        :chats             chats)
      (is (:able-to-send-message? (rf/sub [sub-name])))))
  (testing "one to one chat, not a contact"
    (let [chats {chat-id one-to-one-chat}]
      (swap! rf-db/app-db assoc
        :contacts/contacts {chat-id {:contact-request-state constants/contact-request-state-sent}}
        :profile/profile   multiaccount
        :current-chat-id   chat-id
        :chats             chats)
      (is (not (:able-to-send-message? (rf/sub [sub-name])))))))

(h/deftest-sub :chats/current-chat-message-list-view-context
  [sub-name]
  (testing "reflect :in-pinned-view? in the result"
    (reset! rf-db/app-db {})
    (is (true? (:in-pinned-view? (rf/sub [sub-name :in-pinned-view]))))
    (is (false? (:in-pinned-view? (rf/sub [sub-name])))))
  (testing "reflect current community in community?"
    (let [chats       {chat-id community-chat}
          communities {community-id community}]
      (is (false? (:community? (rf/sub [sub-name]))))
      (swap! rf-db/app-db assoc
        :communities/enabled? true
        :current-chat-id      chat-id
        :chats                chats
        :communities          communities)
      (is (true? (:community? (rf/sub [sub-name]))))))
  (testing "community admin"
    (let [chats       {chat-id community-chat}
          communities {community-id (assoc community
                                           :admin true
                                           :can-delete-message-for-everyone? true
                                           :admin-settings {:pin-message-all-members-enabled? false})}]
      (is (false? (:community? (rf/sub [sub-name]))))
      (swap! rf-db/app-db assoc
        :communities/enabled? true
        :profile/profile      multiaccount
        :current-chat-id      chat-id
        :chats                chats
        :communities          communities)
      (is (= chat-id (:chat-id (rf/sub [sub-name]))))
      (is (= public-key (:current-public-key (rf/sub [sub-name]))))
      (is (true? (:community? (rf/sub [sub-name]))))
      (is (true? (:group-chat (rf/sub [sub-name]))))
      (is (true? (:community-admin? (rf/sub [sub-name]))))
      (is (true? (:can-delete-message-for-everyone? (rf/sub [sub-name]))))
      (is (not (:group-admin? (rf/sub [sub-name]))))
      (is (true? (:message-pin-enabled (rf/sub [sub-name]))))))
  (testing "community member"
    (let [chats       {chat-id community-chat}
          communities {community-id (assoc community
                                           :admin false
                                           :can-delete-message-for-everyone? false
                                           :admin-settings {:pin-message-all-members-enabled?
                                                            false})}]
      (is (false? (:community? (rf/sub [sub-name]))))
      (swap! rf-db/app-db assoc
        :communities/enabled? true
        :profile/profile      multiaccount
        :current-chat-id      chat-id
        :chats                chats
        :communities          communities)
      (is (= chat-id (:chat-id (rf/sub [sub-name]))))
      (is (= public-key (:current-public-key (rf/sub [sub-name]))))
      (is (true? (:community? (rf/sub [sub-name]))))
      (is (true? (:group-chat (rf/sub [sub-name]))))
      (is (not (:community-admin? (rf/sub [sub-name]))))
      (is (not (:can-delete-message-for-everyone? (rf/sub [sub-name]))))
      (is (not (:group-admin? (rf/sub [sub-name]))))
      (is (not (:message-pin-enabled (rf/sub [sub-name]))))))
  (testing "group admin"
    (let [chats {chat-id (assoc private-group-chat
                                :admins
                                #{public-key})}]
      (swap! rf-db/app-db assoc
        :communities/enabled? true
        :profile/profile      multiaccount
        :current-chat-id      chat-id
        :chats                chats)
      (is (= chat-id (:chat-id (rf/sub [sub-name]))))
      (is (= public-key (:current-public-key (rf/sub [sub-name]))))
      (is (not (:public? (rf/sub [sub-name]))))
      (is (not (:community? (rf/sub [sub-name]))))
      (is (true? (:group-chat (rf/sub [sub-name]))))
      (is (not (:community-admin? (rf/sub [sub-name]))))
      (is (not (:can-delete-message-for-everyone? (rf/sub [sub-name]))))
      (is (true? (:group-admin? (rf/sub [sub-name]))))
      (is (true? (:message-pin-enabled (rf/sub [sub-name]))))))
  (testing "group member"
    (let [chats {chat-id (assoc private-group-chat
                                :admins
                                #{})}]
      (swap! rf-db/app-db assoc
        :communities/enabled? true
        :profile/profile      multiaccount
        :current-chat-id      chat-id
        :chats                chats)
      (is (= chat-id (:chat-id (rf/sub [sub-name]))))
      (is (= public-key (:current-public-key (rf/sub [sub-name]))))
      (is (not (:public? (rf/sub [sub-name]))))
      (is (not (:community? (rf/sub [sub-name]))))
      (is (true? (:group-chat (rf/sub [sub-name]))))
      (is (not (:community-admin? (rf/sub [sub-name]))))
      (is (not (:can-delete-message-for-everyone? (rf/sub [sub-name]))))
      (is (not (:group-admin? (rf/sub [sub-name]))))
      (is (not (:message-pin-enabled (rf/sub [sub-name])))))))

(h/deftest-sub :chats/community-channel-ui-details-by-id
  [sub-name]
  (testing "returns specific ui details of a given community channel chat id"
    (let [chats {chat-id (assoc community-chat
                                :color     :army
                                :emoji     "🍑"
                                :chat-name "test")}]
      (swap! rf-db/app-db assoc
        :chats
        chats)
      (let [result (rf/sub [sub-name chat-id])]
        (is (= 3 (count (keys result))))
        (is (= :army (:color result)))
        (is (= "test" (:chat-name result)))
        (is (= "🍑" (:emoji result)))))))

(h/deftest-sub :chats/group-chat-image
  [sub-name]
  (testing "returns picture for group"
    (let [image-data {:uri "data:image/png1234"}
          chats      {chat-id (assoc community-chat
                                     :color     :army
                                     :emoji     "🍑"
                                     :chat-name "test"
                                     :image     image-data)}]
      (swap! rf-db/app-db assoc
        :chats
        chats)
      (let [result (rf/sub [sub-name chat-id])]
        (= image-data result)))))
