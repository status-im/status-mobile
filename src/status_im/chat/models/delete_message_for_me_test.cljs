(ns status-im.chat.models.delete-message-for-me-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.models.delete-message-for-me :as
             delete-message-for-me]
            [status-im.utils.datetime :as datetime]))

(defonce mid "message-id")
(defonce cid "chat-id")

(deftest delete-for-me
  (let [db      {:messages {cid {mid {:id mid}}}}
        message {:message-id mid :chat-id cid}]
    (testing "delete for me"
      (let [expected {:db {:messages {"chat-id" {"message-id"
                                                 {:id "message-id"
                                                  :deleted-for-me? true
                                                  :deleted-for-me-undoable-till
                                                  (+ (datetime/timestamp)
                                                     1000)}}}}
                      :utils/dispatch-later
                      [{:dispatch [:chat.ui/delete-message-for-me-and-sync
                                   {:chat-id "chat-id" :message-id "message-id"}]
                        :ms       1000}]}]
        (is (= (delete-message-for-me/delete {:db db} message 1000) expected))))
    (testing "should return nil if message in db"
      (is (= (delete-message-for-me/delete {:db {:messages []}} message 1000)
             nil)))))

(deftest undo-delete-for-me
  (let [db      {:messages {cid {mid {:id mid}}}}
        message {:message-id mid :chat-id cid}]
    (testing "undo delete for me in time"
      (let [db       (update-in db
                                [:messages cid mid]
                                assoc
                                :deleted-for-me? true
                                :deleted-for-me-undoable-till
                                (+ (datetime/timestamp) 1000))

            expected {:db {:messages {"chat-id" {"message-id"
                                                 {:id "message-id"}}}}}]
        (is (= (delete-message-for-me/undo {:db db} message) expected))))
    (testing "remain deleted for me when undo delete for me late"
      (let [db       (update-in db
                                [:messages cid mid]
                                assoc
                                :deleted-for-me? true
                                :deleted-for-me-undoable-till
                                (- (datetime/timestamp) 1000))

            expected {:db {:messages {"chat-id" {"message-id" {:id "message-id"
                                                               :deleted-for-me?
                                                               true}}}}}]
        (is (= (delete-message-for-me/undo {:db db} message) expected))))
    (testing "remain deleted for me when undo delete for me late"
      (let [db       (update-in db
                                [:messages cid mid]
                                assoc
                                :deleted-for-me? true
                                :deleted-for-me-undoable-till
                                (- (datetime/timestamp) 1000))

            expected {:db {:messages {"chat-id" {"message-id" {:id "message-id"
                                                               :deleted-for-me?
                                                               true}}}}}]
        (is (= (delete-message-for-me/undo {:db db} message) expected))))
    (testing "should return nil if message in db"
      (is (= (delete-message-for-me/undo {:db {:messages []}} message)
             nil)))))

(deftest delete-for-me-and-sync
  (let [db      {:messages {cid {mid {:id mid}}}}
        message {:message-id mid :chat-id cid}]
    (testing "delete for me and sync"
      (let [expected-db {:messages {"chat-id" {"message-id" {:id "message-id"}}}}
            effects     (delete-message-for-me/delete-and-sync {:db db} message)
            result-db   (:db effects)
            rpc-calls   (:status-im.ethereum.json-rpc/call effects)]
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
    (testing "delete for me and sync, should clean undo timer"
      (let [expected-db {:messages {"chat-id" {"message-id" {:id "message-id"}}}}
            effects     (delete-message-for-me/delete-and-sync
                         {:db (update-in db
                                         [:messages cid mid
                                          :deleted-for-me-undoable-till]
                                         (constantly (datetime/timestamp)))}
                         message)
            result-db   (:db effects)]
        (is (= result-db expected-db))))
    (testing "should return nil if message in db"
      (is (= (delete-message-for-me/delete-and-sync {:db {:messages []}}
                                                    message)
             nil)))))
