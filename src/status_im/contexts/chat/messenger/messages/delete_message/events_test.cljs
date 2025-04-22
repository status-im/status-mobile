(ns status-im.contexts.chat.messenger.messages.delete-message.events-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [status-im.contexts.chat.messenger.messages.delete-message.events :as delete-message]
    [utils.datetime :as datetime]))

(def mid "message-id")
(def cid "chat-id")

(deftest delete-test
  (with-redefs [datetime/timestamp (constantly 1)]
    (let [pub-key "0x1"
          db      {:profile/profile {:public-key pub-key}
                   :messages        {cid {mid {:id                mid
                                               :from              pub-key
                                               :whisper-timestamp 1}}}}
          message {:message-id mid :chat-id cid}]
      (testing "delete"
        (testing "dispatch right db fx when deleting own message"
          (let [effects           (delete-message/delete {:db db} [message 1000])
                result-messages   (get-in effects [:db :messages] effects)
                expected-messages {cid
                                   {mid
                                    {:id                    mid
                                     :from                  pub-key
                                     :whisper-timestamp     1
                                     :deleted?              true
                                     :deleted-by            nil
                                     :deleted-undoable-till 1001}}}]
            (is (match? result-messages
                        expected-messages))))
        (testing "dispatch right db fx when deleting another user message"
          (let [other-pub-key     "0x2"
                mod-db            (assoc-in db [:messages cid mid :from] other-pub-key)
                effects           (delete-message/delete {:db mod-db} [message 1000])
                result-messages   (get-in effects [:db :messages] effects)
                expected-messages {cid
                                   {mid
                                    {:id                    mid
                                     :from                  other-pub-key
                                     :whisper-timestamp     1
                                     :deleted?              true
                                     :deleted-by            pub-key
                                     :deleted-undoable-till 1001}}}]
            (is (match? result-messages
                        expected-messages))))
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
                effects (delete-message/delete {:db db} [message 1000])]
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
          (is (= (delete-message/delete {:db {:messages []}} [message 1000]) nil)))))))

(deftest undo-delete-test
  (let [db      {:messages {cid {mid {:id mid :whisper-timestamp 1}}}}
        message {:message-id mid :chat-id cid}]
    (testing "undo delete"
      (testing "undo in time"
        (let [db             (update-in db
                                        [:messages cid mid]
                                        assoc
                                        :deleted?              true
                                        :deleted-undoable-till (+ (datetime/timestamp) 1000))
              effects        (delete-message/undo {:db db} [message])
              result-message (get-in effects [:db :messages cid mid])]
          (is (match? result-message
                      {:id                mid
                       :whisper-timestamp 1}))))
      (testing "remain deleted when undo after timelimit"
        (let [db             (update-in db
                                        [:messages cid mid]
                                        assoc
                                        :deleted?              true
                                        :deleted-undoable-till (- (datetime/timestamp) 1000))
              effects        (delete-message/undo {:db db} [message])
              result-message (get-in effects [:db :messages cid mid])]
          (is (match? result-message
                      {:id                mid
                       :whisper-timestamp 1
                       :deleted?          true}))))
      (testing "return nil if message not in db"
        (is (= (delete-message/undo {:db {:messages []}} [message]) nil))))))

(deftest undo-all-test
  (testing "does nothing when there are no pending todos"
    (let [cofx     {:db {:toasts
                         {:toasts
                          {:delete-message-for-everyone
                           {:message-deleted-for-everyone-undos nil}}}}}
          effects  (delete-message/undo-all cofx)
          expected nil]
      (is (match? effects expected))))

  (testing "undo all pending undos"
    (let [pending-undo  {:undo-id 1}
          pending-undos [pending-undo]
          cofx          {:db {:toasts
                              {:toasts
                               {:delete-message-for-everyone
                                {:message-deleted-for-everyone-undos pending-undos}}}}}
          effects       (delete-message/undo-all cofx)
          expected      {:dispatch-n [[:chat.ui/undo-delete-message pending-undo]]}]
      (is (match? effects expected)))))

(deftest delete-and-send-test
  (let [db      {:messages {cid {mid {:id mid :deleted? true :deleted-undoable-till 0}}}}
        message {:message-id mid :chat-id cid}]
    (testing "delete and send"
      (testing "dispatch right rpc call fx"
        (let [effects     (delete-message/delete-and-send {:db db} [message false])
              expected-db {:messages
                           {cid
                            {mid
                             {:id       mid
                              :deleted? true}}}}
              expected-fx [[:json-rpc/call
                            [{:method      "wakuext_deleteMessageAndSend"
                              :params      [mid]
                              :js-response true
                              :on-error    [:chat/delete-and-send-error mid]
                              :on-success  [:sanitize-messages-and-process-response]}]]
                           [:dispatch
                            [:pin-message/send-pin-message
                             {:chat-id      cid
                              :message-id   mid
                              :pinned       false
                              :remote-only? true}]]]]
          (is (match? effects
                      {:db expected-db
                       :fx expected-fx}))))
      (testing "clean undo timer"
        (let [expected-db {:messages {cid {mid {:id mid :deleted? true}}}}
              effects     (delete-message/delete-and-send
                           {:db (update-in db
                                           [:messages cid mid :deleted-undoable-till]
                                           (constantly (datetime/timestamp)))}
                           [message false])
              result-db   (:db effects)]
          (is (= result-db expected-db))))
      (testing "before deleted locally"
        (let [effects (delete-message/delete-and-send
                       {:db (update-in db [:messages cid mid] dissoc :deleted?)}
                       [message false])]
          (is (-> effects :db nil?) "not delete and send")))
      (testing "before undo timelimit"
        (with-redefs [datetime/timestamp (constantly 1)]
          (let [effects (delete-message/delete-and-send
                         {:db (update-in db [:messages cid mid] assoc :deleted-undoable-till 2)}
                         [message false])]
            (is (-> effects :db nil?)))))
      (testing "return nil if message not in db"
        (is (= (delete-message/delete-and-send {:db {:messages []}} [message false])
               nil))))))

(deftest send-all-test
  (testing "sends all delete messages"
    (let [chat-id                 "0x1"
          message-id-1            "1x1"
          message-id-2            "1x2"
          pending-deleted-message {:deleted?              true
                                   :deleted-undoable-till (datetime/timestamp)}
          cofx                    {:db
                                   {:messages
                                    {chat-id
                                     {message-id-1 pending-deleted-message
                                      message-id-2 pending-deleted-message}}}}
          effects                 (delete-message/send-all cofx)
          expected                {:db
                                   {:messages
                                    {chat-id
                                     {message-id-1 {:deleted? true}
                                      message-id-2 {:deleted? true}}}}
                                   :fx [[:json-rpc/call
                                         [{:method      "wakuext_deleteMessageAndSend"
                                           :params      [message-id-1]
                                           :js-response true
                                           :on-error    [:chat/delete-and-send-error message-id-1]
                                           :on-success  [:sanitize-messages-and-process-response]}]]
                                        [:dispatch
                                         [:pin-message/send-pin-message
                                          {:chat-id      chat-id
                                           :message-id   message-id-1
                                           :pinned       false
                                           :remote-only? true}]]
                                        [:json-rpc/call
                                         [{:method      "wakuext_deleteMessageAndSend"
                                           :params      [message-id-2]
                                           :js-response true
                                           :on-error    [:chat/delete-and-send-error message-id-2]
                                           :on-success  [:sanitize-messages-and-process-response]}]]
                                        [:dispatch
                                         [:pin-message/send-pin-message
                                          {:chat-id      chat-id
                                           :message-id   message-id-2
                                           :pinned       false
                                           :remote-only? true}]]]}]
      (is (match? effects expected)))))

(comment
  (cljs.test/run-tests))
