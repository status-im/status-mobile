(ns status-im.test.chat.models.input
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.constants :as constants]
            [status-im.constants :as global.constants]
            [status-im.utils.config :as config]
            [status-im.chat.models.message :as chat.message]
            [status-im.utils.datetime :as datetime]
            [status-im.chat.models.input :as input]))

(deftest text->emoji
  (is (nil? (input/text->emoji nil)))
  (is (= "" (input/text->emoji "")))
  (is (= "test" (input/text->emoji "test")))
  (is (= "word1 \uD83D\uDC4D word2" (input/text->emoji "word1 :+1: word2"))))

(deftest process-cooldown-fx
  (let [db {:current-chat-id              "chat"
            :chats                        {"chat" {:public? true}}
            :chat/cooldowns               0
            :chat/spam-messages-frequency 0
            :chat/cooldown-enabled?       false}]
    (with-redefs [datetime/timestamp (constantly 1527675198542)]
      (testing "no spamming detected"
        (let [expected {:db (assoc db :chat/last-outgoing-message-sent-at 1527675198542)}
              actual (input/process-cooldown {:db db})]
          (is (= expected actual))))

      (testing "spamming detected in 1-1"
        (let [db (assoc db
                        :chats {"chat" {:public? false}}
                        :chat/spam-messages-frequency constants/spam-message-frequency-threshold
                        :chat/last-outgoing-message-sent-at (- 1527675198542 900))
              expected nil
              actual (input/process-cooldown {:db db})]
          (is (= expected actual))))

      (testing "spamming detected"
        (let [db (assoc db
                        :chat/last-outgoing-message-sent-at (- 1527675198542 900)
                        :chat/spam-messages-frequency constants/spam-message-frequency-threshold)
              expected {:db                    (assoc db
                                                      :chat/last-outgoing-message-sent-at 1527675198542
                                                      :chat/cooldowns 1
                                                      :chat/spam-messages-frequency 0
                                                      :chat/cooldown-enabled? true)
                        :show-cooldown-warning nil
                        :dispatch-later        [{:dispatch [:chat/disable-cooldown]
                                                 :ms       (constants/cooldown-periods-ms 1)}]}
              actual (input/process-cooldown {:db db})]
          (is (= expected actual))))

      (testing "spamming detected twice"
        (let [db (assoc db
                        :chat/cooldowns 1
                        :chat/last-outgoing-message-sent-at (- 1527675198542 900)
                        :chat/spam-messages-frequency constants/spam-message-frequency-threshold)
              expected {:db                    (assoc db
                                                      :chat/last-outgoing-message-sent-at 1527675198542
                                                      :chat/cooldowns 2
                                                      :chat/spam-messages-frequency 0
                                                      :chat/cooldown-enabled? true)
                        :show-cooldown-warning nil
                        :dispatch-later        [{:dispatch [:chat/disable-cooldown]
                                                 :ms       (constants/cooldown-periods-ms 2)}]}
              actual (input/process-cooldown {:db db})]
          (is (= expected actual))))

      (testing "spamming reaching cooldown threshold"
        (let [db (assoc db
                        :chat/cooldowns (dec constants/cooldown-reset-threshold)
                        :chat/last-outgoing-message-sent-at (- 1527675198542 900)
                        :chat/spam-messages-frequency constants/spam-message-frequency-threshold)
              expected {:db                    (assoc db
                                                      :chat/last-outgoing-message-sent-at 1527675198542
                                                      :chat/cooldowns 0
                                                      :chat/spam-messages-frequency 0
                                                      :chat/cooldown-enabled? true)
                        :show-cooldown-warning nil
                        :dispatch-later        [{:dispatch [:chat/disable-cooldown]
                                                 :ms       (constants/cooldown-periods-ms 3)}]}
              actual (input/process-cooldown {:db db})]
          (is (= expected actual)))))))

(deftest contact-request-message-fx-test
  (let [actual (:dispatch
                (input/contact-request-message-fx "input-text" "chat-id" {:now 1
                                                                          :db {}}))]

    (testing "it dispatches the contact request message"
      (is actual))
    (testing "it sets the content-type"
      (is (= global.constants/content-type-contact-request
             (-> actual
                 (nth 3)
                 :content-type))))))

(deftest send-current-message-test
  (with-redefs [chat.message/send-message (fn [{:keys [content-type]}]
                                            (fn [{:keys [db]}]
                                              {:db (assoc db :message-sent content-type)}))]
    (testing "pfs is enabled"
      (with-redefs [config/pfs-encryption-enabled? (constantly true)]
        (testing "is a group chat"
          (testing "does not send a contact request"
            (is
             (= global.constants/content-type-text
                (->
                 (input/send-current-message
                  {:now 0
                   :db {:current-chat-id "chat-id"
                        :chats {"chat-id" {:input-text "input-text"
                                           :group-chat true}}}})
                 :db
                 :message-sent)))))
        (testing "is a 1-to-1 chat"
          (testing "there no contact code"
            (testing "it sends a contact request"
              (is
               (= global.constants/content-type-contact-request
                  (->
                   (input/send-current-message
                    {:now 0
                     :db {:current-chat-id "chat-id"
                          :chats {"chat-id" {:input-text "input-text"}}}})
                   :db
                   :message-sent)))))
          (testing "there's a contact code"
            (testing "it does not send a contact request"
              (is
               (= global.constants/content-type-text
                  (->
                   (input/send-current-message
                    {:now 0
                     :db {:contact-codes/contact-codes {"chat-id" true}
                          :current-chat-id "chat-id"
                          :chats {"chat-id" {:input-text "input-text"}}}})
                   :db
                   :message-sent))))))))
    (testing "pfs is not enabled"
      (with-redefs [config/pfs-encryption-enabled? (constantly false)]
        (testing "is a 1-to-1 chat"
          (testing "there's no contact-code"
            (testing "it does not send a contact-request"
              (is
               (= global.constants/content-type-text
                  (->
                   (input/send-current-message
                    {:now 0
                     :db {:current-chat-id "chat-id"
                          :chats {"chat-id" {:input-text "input-text"}}}})
                   :db
                   :message-sent))))))))))
