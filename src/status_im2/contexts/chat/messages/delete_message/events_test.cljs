(ns status-im2.contexts.chat.messages.delete-message.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im2.contexts.chat.messages.delete-message.events :as delete-message]
            [utils.datetime :as datetime]))

(def mid "message-id")
(def cid "chat-id")

(deftest delete
  (with-redefs [datetime/timestamp (constantly 1)]
    (let [db      {:messages {cid {mid {:id mid :whisper-timestamp 1}}}}
          message {:message-id mid :chat-id cid}]
      (testing "delete"
        (testing "dispatch right db fx"
          (let [result-message (get-in (delete-message/delete {:db db} message 1000)
                                       [:db :messages cid mid])]
            (is (= (:id result-message) mid))
            (is (true? (:deleted? result-message)) "mark message :deleted?")
            (is (= (:deleted-undoable-till result-message) 1001) "set message undo timelimit")))
        (testing "delete with pending deletes"
          (let [db      (-> db
                            (update-in [:messages cid "pending-delete-message"]
                                       assoc
                                       :deleted?              true
                                       :deleted-undoable-till 0
                                       :whisper-timestamp     0)
                            (update-in [:toasts :toasts :delete-message-for-everyone]
                                       assoc
                                       :message-deleted-for-everyone-count 1
                                       :message-deleted-for-everyone-undos [{:message-id
                                                                             "pending-delete-message"
                                                                             :chat-id cid}]))
                effects (delete-message/delete {:db db} message 1000)]
            (is (= (get-in effects [:db :messages cid mid :deleted-undoable-till])
                   (get-in effects [:db :messages cid "pending-delete-message" :deleted-undoable-till])
                   1001)
                "sync all pending delete undo timelimit")
            (let [upsert-toast (-> effects :dispatch-n second)]
              (is (= (-> upsert-toast last :message-deleted-for-everyone-count) 2)
                  "+1 pending deletes")
              (is
               (and
                (-> upsert-toast
                    last
                    :message-deleted-for-everyone-undos
                    first
                    :message-id
                    (= "pending-delete-message"))
                (-> upsert-toast
                    last
                    :message-deleted-for-everyone-undos
                    second
                    :message-id
                    (= mid)))
               "pending deletes are in order"))))
        (testing "return nil if message not in db"
          (is (= (delete-message/delete {:db {:messages []}} message 1000) nil)))))))

(deftest undo-delete
  (let [db      {:messages {cid {mid {:id mid :whisper-timestamp 1}}}}
        message {:message-id mid :chat-id cid}]
    (testing "undo delete"
      (testing "undo in time"
        (let [db             (update-in db
                                        [:messages cid mid]
                                        assoc
                                        :deleted?              true
                                        :deleted-undoable-till (+ (datetime/timestamp) 1000))
              result-message (get-in (delete-message/undo {:db db} message) [:db :messages cid mid])]
          (is (= (:id result-message) mid))
          (is (nil? (:deleted? result-message)))
          (is (nil? (:deleted-undoable-till result-message)))))
      (testing "remain deleted when undo after timelimit"
        (let [db             (update-in db
                                        [:messages cid mid]
                                        assoc
                                        :deleted?              true
                                        :deleted-undoable-till (- (datetime/timestamp) 1000))
              result-message (get-in (delete-message/undo {:db db} message) [:db :messages cid mid])]
          (is (= (:id result-message) mid))
          (is (nil? (:deleted-undoable-till result-message)))
          (is (true? (:deleted? result-message)))))
      (testing "return nil if message not in db"
        (is (= (delete-message/undo {:db {:messages []}} message) nil))))))

(deftest delete-and-send
  (let [db      {:messages {cid {mid {:id mid :deleted? true :deleted-undoable-till 0}}}}
        message {:message-id mid :chat-id cid}]
    (testing "delete and send"
      (testing "dispatch right rpc call fx"
        (let [expected-db {:messages {cid {mid {:id mid :deleted? true}}}}
              effects     (delete-message/delete-and-send {:db db} message false)
              result-db   (:db effects)
              rpc-calls   (:json-rpc/call effects)]
          (is (= result-db expected-db))
          (is (= (count rpc-calls) 1))
          (is (= (-> rpc-calls first :method) "wakuext_deleteMessageAndSend"))
          (is (= (-> rpc-calls first :params count) 1))
          (is (= (-> rpc-calls first :params first) mid))))
      (testing "clean undo timer"
        (let [expected-db {:messages {cid {mid {:id mid :deleted? true}}}}
              effects     (delete-message/delete-and-send
                           {:db (update-in db
                                           [:messages cid mid :deleted-undoable-till]
                                           (constantly (datetime/timestamp)))}
                           message
                           false)
              result-db   (:db effects)]
          (is (= result-db expected-db))))
      (testing "before deleted locally"
        (let [effects (delete-message/delete-and-send
                       {:db (update-in db [:messages cid mid] dissoc :deleted?)}
                       message
                       false)]
          (is (-> effects :db nil?) "not delete and send")))
      (testing "before undo timelimit"
        (with-redefs [datetime/timestamp (constantly 1)]
          (let [effects (delete-message/delete-and-send
                         {:db (update-in db [:messages cid mid] assoc :deleted-undoable-till 2)}
                         message
                         false)]
            (is (-> effects :db nil?)))))
      (testing "return nil if message not in db"
        (is (= (delete-message/delete-and-send {:db {:messages []}} message false)
               nil))))))
