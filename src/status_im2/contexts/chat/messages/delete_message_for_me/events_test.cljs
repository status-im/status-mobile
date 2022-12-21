(ns status-im2.contexts.chat.messages.delete-message-for-me.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [utils.datetime :as datetime]
            [status-im2.contexts.chat.messages.message.delete-message-for-me.events :as
             delete-message-for-me]))

(def mid "message-id")
(def cid "chat-id")

(deftest delete-for-me
  (with-redefs [datetime/timestamp (constantly 1)]
    (let [db      {:messages {cid {mid {:id mid :whisper-timestamp 1}}}}
          message {:message-id mid :chat-id cid}]
      (testing "delete for me"
        (testing "dispatch right db fx"
          (let [result-message (get-in (delete-message-for-me/delete {:db db} message 1000)
                                       [:db :messages cid mid])]
            (is (= (:id result-message) mid))
            (is (true? (:deleted-for-me? result-message)))
            (is (= (:deleted-for-me-undoable-till result-message) 1001))))
        (testing "return nil if message not in db"
          (is (= (delete-message-for-me/delete {:db {:messages []}} message 1000)
                 nil)))))))

(deftest undo-delete-for-me
  (let [db      {:messages {cid {mid {:id mid :whisper-timestamp 1}}}}
        message {:message-id mid :chat-id cid}]
    (testing "undo delete for me"
      (testing "in time"
        (let [db             (update-in db
                                        [:messages cid mid]
                                        assoc
                                        :deleted-for-me?              true
                                        :deleted-for-me-undoable-till
                                        (+ (datetime/timestamp) 1000))
              result-message (get-in (delete-message-for-me/undo {:db db} message)
                                     [:db :messages cid mid])]
          (is (= (:id result-message) mid))
          (is (nil? (:deleted-for-me? result-message)))
          (is (nil? (:deleted-for-me-undoable-till result-message)))))

      (testing "remain deleted for me when undo after timelimit"
        (let [db             (update-in db
                                        [:messages cid mid]
                                        assoc
                                        :deleted-for-me?              true
                                        :deleted-for-me-undoable-till (- (datetime/timestamp) 1000))
              result-message (get-in (delete-message-for-me/undo {:db db} message)
                                     [:db :messages cid mid])]
          (is (= (:id result-message) mid))
          (is (nil? (:deleted-for-me-undoable-till result-message)))
          (is (true? (:deleted-for-me? result-message)))))

      (testing "return nil if message not in db"
        (is (= (delete-message-for-me/undo {:db {:messages []}} message)
               nil))))))

(deftest delete-for-me-and-sync
  (let [db      {:messages {cid {mid {:id mid}}}}
        message {:message-id mid :chat-id cid}]
    (testing "delete for me and sync"
      (testing "dispatch right rpc call"
        (let [expected-db {:messages {cid {mid {:id mid}}}}
              effects     (delete-message-for-me/delete-and-sync {:db db} message)
              result-db   (:db effects)
              rpc-calls   (:json-rpc/call effects)]
          (is (= result-db expected-db))
          (is (= (count rpc-calls) 1))
          (is (= (-> rpc-calls
                     first
                     :method)
                 "wakuext_deleteMessageForMeAndSync"))
          (is (= (-> rpc-calls
                     first
                     :params
                     count)
                 2))
          (is (= (-> rpc-calls
                     first
                     :params
                     first)
                 cid))
          (is (= (-> rpc-calls
                     first
                     :params
                     second)
                 mid))))
      (testing "clean undo timer"
        (let [expected-db {:messages {cid {mid {:id mid}}}}
              effects     (delete-message-for-me/delete-and-sync
                           {:db (update-in db
                                           [:messages cid mid
                                            :deleted-for-me-undoable-till]
                                           (constantly (datetime/timestamp)))}
                           message)
              result-db   (:db effects)]
          (is (= result-db expected-db))))
      (testing "return nil if message not in db"
        (is (= (delete-message-for-me/delete-and-sync {:db {:messages []}}
                                                      message)
               nil))))))
