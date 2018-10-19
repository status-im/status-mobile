(ns status-im.test.chat.models.loading
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.models.loading :as loading]))

(deftest group-chat-messages
  (let [cofx         {:db {:chats {"chat-id" {:messages {0 {:message-id  0
                                                            :content     "a"
                                                            :clock-value 0
                                                            :timestamp   0}
                                                         1 {:message-id  1
                                                            :content     "b"
                                                            :clock-value 1
                                                            :timestamp   1}
                                                         2 {:message-id  2
                                                            :content     "c"
                                                            :clock-value 2
                                                            :timestamp   2}
                                                         3 {:message-id  3
                                                            :content     "d"
                                                            :clock-value 3
                                                            :timestamp   3}}}}}}
        new-messages [{:message-id  1
                       :content     "b"
                       :clock-value 1
                       :timestamp   1
                       :show?       false}
                      {:message-id  2
                       :content     "c"
                       :clock-value 2
                       :timestamp   2
                       :show?       true}
                      {:message-id  3
                       :content     "d"
                       :clock-value 3
                       :timestamp   3
                       :show?       true}]]
    (testing "New messages are grouped/sorted correctly, hidden messages are not grouped"
      (is (= '(2 3)
             (map :message-id
                  (-> (get-in (loading/group-chat-messages cofx "chat-id" new-messages)
                              [:db :chats "chat-id" :message-groups])
                      first
                      second)))))))
