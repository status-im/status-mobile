(ns status-im.test.chat.models.input
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.constants :as constants]
            [status-im.utils.config :as config]
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
