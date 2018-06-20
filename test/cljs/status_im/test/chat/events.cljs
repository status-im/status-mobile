(ns status-im.test.chat.events
  (:require [cljs.test :refer [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer [run-test-sync]]
            [status-im.constants :as const]
            [status-im.chat.console :as console-chat]
            [status-im.chat.events :as chat-events]))

(def test-db
  {:current-public-key "me"
   :chats {const/console-chat-id console-chat/chat
           "status" {:public? true
                     :unviewed-messages #{"6" "5" "4" "3" "2" "1"}
                     :message-statuses {"6" {"me" {:message-id "6"
                                                   :chat-id "status"
                                                   :whisper-identity "me"
                                                   :status :received}}
                                        "5" {"me" {:message-id "5"
                                                   :chat-id "status"
                                                   :whisper-identity "me"
                                                   :status :received}}
                                        "4" {"me" {:message-id "4"
                                                   :chat-id "status"
                                                   :whisper-identity "me"
                                                   :status :received}}}}
           "opened" {:unviewed-messages #{}
                     :message-statuses {"1" {"me" {:message-id "1"
                                                   :chat-id "opened"
                                                   :whisper-identity "me"
                                                   :status :seen}}}}
           "1-1"    {:unviewed-messages #{"6" "5" "4" "3" "2" "1"}
                     :message-statuses {"6" {"me" {:message-id "6"
                                                   :chat-id "status"
                                                   :whisper-identity "me"
                                                   :status :received}}
                                        "5" {"me" {:message-id "5"
                                                   :chat-id "status"
                                                   :whisper-identity "me"
                                                   :status :received}}
                                        "4" {"me" {:message-id "4"
                                                   :chat-id "status"
                                                   :whisper-identity "me"
                                                   :status :received}}}}}})

(deftest init-console-chat
  (testing "initialising console if console is already added to chats, should not modify anything"
    (let [fx (chat-events/init-console-chat {:db test-db})]
      (is (not fx))))

  (testing "initialising console without existing account and console chat not initialisated"
    (let [fresh-db {:chats {}}
          {:keys [db dispatch-n]} (chat-events/init-console-chat {:db fresh-db})]
      (is (= (:current-chat-id db)
             (:chat-id console-chat/chat)))
      (is (= (:current-chat-id db)
             const/console-chat-id))))

  (testing "initialising console with existing account and console chat not initialisated"
    (let [fresh-db {:chats {}}
          {:keys [db dispatch-n]} (chat-events/init-console-chat {:db fresh-db})]
      (is (= (:current-chat-id db)
             (:chat-id console-chat/chat)))
      (is (= (:current-chat-id db)
             const/console-chat-id)))))

(deftest mark-messages-seen
  (testing "Marking messages seen correctly marks loaded messages as seen and updates absolute unviewed set"
    (let [fx (chat-events/mark-messages-seen "status" {:db test-db})
          me (:current-public-key test-db)]
      (is (= '(:seen :seen :seen)
             (map (fn [[_ v]]
                    (get-in v [me :status]))
                  (get-in fx [:db :chats "status" :message-statuses]))))
      (is (= 1 (count (:data-store/tx fx))))
      (is (= nil (:shh/post fx))) ;; for public chats, no confirmation is sent out
      (is (= #{"3" "2" "1"} (get-in fx [:db :chats "status" :unviewed-messages])))))

  (testing "With empty unviewed set, no effects are produced"
    (is (= nil (chat-events/mark-messages-seen "opened" {:db test-db}))))

  (testing "For 1-1 chat, we send seen messages confirmation to the recipient as well"
    (is (= #{"4" "5" "6"}
           (set (get-in (chat-events/mark-messages-seen "1-1" {:db test-db})
                        [:shh/post 0 :message :payload :message-ids]))))))
