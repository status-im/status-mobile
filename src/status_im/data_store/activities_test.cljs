(ns status-im.data-store.activities-test
  (:require [cljs.test :refer [deftest is testing]]
            [quo.design-system.colors :as colors]
            [status-im.constants :as constants]
            [status-im.data-store.activities :as store]))

(def chat-id
  "0x04c66155")

(def chat-name
  "0x04c661")

(def raw-notification
  {:chatId                    chat-id
   :contactVerificationStatus constants/contact-verification-state-pending
   :lastMessage               {}
   :name                      chat-name
   :replyMessage              {}})

(deftest <-rpc-test
  (testing "assocs random chat color"
    (is (contains? (set colors/chat-colors) (:color (store/<-rpc raw-notification)))))

  (testing "renames keys"
    (is (= {:name                        chat-name
            :chat-id                     chat-id
            :contact-verification-status constants/contact-verification-state-pending}
           (-> raw-notification
               store/<-rpc
               (dissoc :color :last-message :message :reply-message)))))

  (testing "transforms messages from RPC response"
    (is (= {:last-message  {:quoted-message     nil
                            :outgoing-status    nil
                            :command-parameters nil
                            :content            {:sticker     nil
                                                 :rtl?        nil
                                                 :ens-name    nil
                                                 :parsed-text nil
                                                 :response-to nil
                                                 :chat-id     nil
                                                 :image       nil
                                                 :line-count  nil
                                                 :links       nil
                                                 :text        nil}
                            :outgoing           false}
            :message       nil
            :reply-message {:quoted-message     nil
                            :outgoing-status    nil
                            :command-parameters nil
                            :content            {:sticker     nil
                                                 :rtl?        nil
                                                 :ens-name    nil
                                                 :parsed-text nil
                                                 :response-to nil
                                                 :chat-id     nil
                                                 :image       nil
                                                 :line-count  nil
                                                 :links       nil
                                                 :text        nil}
                            :outgoing           false}}
           (-> raw-notification
               store/<-rpc
               (select-keys [:last-message :message :reply-message])))))

  (testing "augments notification based on its type"
    (is (= {:chat-name chat-name
            :chat-type constants/private-group-chat-type
            :name      chat-name}
           (-> raw-notification
               (assoc :type constants/activity-center-notification-type-reply)
               store/<-rpc
               (select-keys [:name :chat-type :chat-name :public? :group-chat]))))

    (is (= {:chat-name chat-name
            :chat-type constants/private-group-chat-type
            :name      chat-name}
           (-> raw-notification
               (assoc :type constants/activity-center-notification-type-mention)
               store/<-rpc
               (select-keys [:name :chat-type :chat-name :public? :group-chat]))))

    (is (= {:chat-name  chat-name
            :chat-type  constants/private-group-chat-type
            :group-chat true
            :name       chat-name
            :public?    false}
           (-> raw-notification
               (assoc :type constants/activity-center-notification-type-private-group-chat)
               store/<-rpc
               (select-keys [:name :chat-type :chat-name :public? :group-chat]))))

    (is (= {:chat-name  chat-name
            :chat-type  constants/one-to-one-chat-type
            :group-chat false
            :name       chat-name
            :public?    false}
           (-> raw-notification
               (assoc :type constants/activity-center-notification-type-one-to-one-chat)
               store/<-rpc
               (select-keys [:name :chat-type :chat-name :public? :group-chat]))))))
