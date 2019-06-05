(ns status-im.test.mailserver.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.transport.utils :as utils]
            [status-im.mailserver.core :as mailserver]
            [status-im.mailserver.constants :as constants]
            [status-im.utils.random :as rand]))

(def enode "enode://08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404")
(def enode2 "enode://12d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404")

(deftest test-extract-enode-id
  (testing "Get enode id from enode uri"
    (is (= "08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b"
           (utils/extract-enode-id enode))))
  (testing "Get enode id from mailformed enode uri"
    (is (= ""
           (utils/extract-enode-id "08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404"))))
  (testing "Test empty string"
    (is (= ""
           (utils/extract-enode-id ""))))
  (testing "Test nil"
    (is (= ""
           (utils/extract-enode-id nil)))))

(def peers
  [{:id "08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b"
    :enode "enode://08d8eb6177b187049f6c97ed3f6c74fbbefb94c7ad10bafcaf4b65ce89c314dcfee0a8bc4e7a5b824dfa08b45b360cc78f34f0aff981f8386caa07652d2e601b@163.172.177.138:40404"
    :name "StatusIM/v0.9.9-unstable/linux-amd64/go1.9.2"}
   {:id "0f7c65277f916ff4379fe520b875082a56e587eb3ce1c1567d9ff94206bdb05ba167c52272f20f634cd1ebdec5d9dfeb393018bfde1595d8e64a717c8b46692f"
    :enode "enode://0f7c65277f916ff4379fe520b875082a56e587eb3ce1c1567d9ff94206bdb05ba167c52272f20f634cd1ebdec5d9dfeb393018bfde1595d8e64a717c8b46692f@203.136.241.111:40404"
    :name "Geth/v1.7.2-stable/linux-amd64/go1.9.1"}])

(deftest change-mailserver
  (testing "we are offline"
    (testing "it does not change mailserver"
      (is (not (mailserver/change-mailserver {:db {:peers-count 0}})))))
  (testing "we are online"
    (testing "there's a preferred mailserver"
      (testing "it shows the popup"
        (is (:ui/show-confirmation (mailserver/change-mailserver
                                    {:db {:account/account {:settings
                                                            {:fleet :beta
                                                             :mailserver {:beta "id"}}}
                                          :peers-count 1}})))))
    (testing "there's not a preferred mailserver"
      (testing "it changes the mailserver"
        (is (= :a
               (get-in
                (mailserver/change-mailserver
                 {:db {:mailserver/mailservers {:beta {:a "b"}}
                       :account/account {:settings
                                         {:fleet :beta}}
                       :peers-count 1}})
                [:db :mailserver/current-id]))))
      (testing "it does not show the popup"
        (is (not (:ui/show-confirmation (mailserver/change-mailserver
                                         {:db {:peers-count 1}}))))))))

(deftest test-registered-peer?
  (testing "Peer is registered"
    (is (mailserver/registered-peer? peers enode)))
  (testing "Peer is not peers list"
    (is (not (mailserver/registered-peer? peers enode2))))
  (testing "Empty peers"
    (is (not (mailserver/registered-peer? [] enode))))
  (testing "Empty peer"
    (is (not (mailserver/registered-peer? peers ""))))
  (testing "Nil peer"
    (is (not (mailserver/registered-peer? peers nil)))))

(def enode-id "1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40")
(def password "password")
(def host "167.99.209.61:30504")

(def valid-enode-address (str "enode://" enode-id "@" host))
(def valid-enode-url (str "enode://" enode-id ":" password "@" host))

(deftest valid-enode-address-test
  (testing "url without password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"]
      (is (mailserver/valid-enode-address? address))))
  (testing "url with password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepasswordwith@and:@@167.99.209.61:30504"]
      (is (not (mailserver/valid-enode-address? address)))))
  (testing "invalid url"
    (is (not (mailserver/valid-enode-address? "something not valid")))))

(deftest valid-enode-url-test
  (testing "url without password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"]
      (is (not (mailserver/valid-enode-url? address)))))
  (testing "url with password"
    (let [address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40:somepasswordwith@and:@@167.99.209.61:30504"]
      (is (mailserver/valid-enode-url? address))))
  (testing "invalid url"
    (is (not (mailserver/valid-enode-url? "something not valid")))))

(deftest address->mailserver
  (testing "with password"
    (let [address "enode://some-id:the-password@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :password "the-password"
              :user-defined true}
             (mailserver/address->mailserver address)))))
  (testing "without password"
    (let [address "enode://some-id@206.189.56.154:30504"]
      (is (= {:address "enode://some-id@206.189.56.154:30504"
              :user-defined true}
             (mailserver/address->mailserver address))))))

(deftest set-input
  (testing "it validates names"
    (testing "correct name"
      (is (= {:db {:mailserver.edit/mailserver {:name {:value "value"
                                                       :error false}}}}
             (mailserver/set-input {:db {}} :name "value"))))
    (testing "blank name"
      (is (= {:db {:mailserver.edit/mailserver {:name {:value ""
                                                       :error true}}}}
             (mailserver/set-input {:db {}} :name "")))))
  (testing "it validates enodes url"
    (testing "correct url"
      (is (= {:db {:mailserver.edit/mailserver {:url {:value valid-enode-url
                                                      :error false}}}}
             (mailserver/set-input {:db {}} :url valid-enode-url))))
    (testing "broken url"
      (is (= {:db {:mailserver.edit/mailserver {:url {:value "broken"
                                                      :error true}}}}
             (mailserver/set-input {:db {}} :url "broken"))))))

(deftest edit-mailserver
  (let [db {:mailserver/mailservers
            {:eth.beta {"a" {:id      "a"
                             :address valid-enode-address
                             :password password
                             :name    "name"}}}}
        cofx {:db db}]
    (testing "when no id is given"
      (let [actual (mailserver/edit cofx nil)]
        (testing "it resets :mailserver/manage"
          (is (= {:id   {:value nil
                         :error false}
                  :url  {:value ""
                         :error true}
                  :name {:value ""
                         :error true}}
                 (-> actual :db :mailserver.edit/mailserver))))
        (testing "it navigates to edit-mailserver view"
          (is (= [:edit-mailserver nil]
                 (:status-im.ui.screens.navigation/navigate-to actual))))))
    (testing "when an id is given"
      (testing "when the mailserver is in the list"
        (let [actual (mailserver/edit cofx "a")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value "a"
                           :error false}
                    :url  {:value valid-enode-url
                           :error false}
                    :name {:value "name"
                           :error false}}
                   (-> actual :db :mailserver.edit/mailserver))))
          (testing "it navigates to edit-mailserver view"
            (is (= [:edit-mailserver nil]
                   (:status-im.ui.screens.navigation/navigate-to actual))))))
      (testing "when the mailserver is not in the list"
        (let [actual (mailserver/edit cofx "not-existing")]
          (testing "it populates the fields with the correct values"
            (is (= {:id   {:value nil
                           :error false}
                    :url  {:value ""
                           :error true}
                    :name {:value ""
                           :error true}}
                   (-> actual :db :mailserver.edit/mailserver))))
          (testing "it navigates to edit-mailserver view"
            (is (= [:edit-mailserver nil]
                   (:status-im.ui.screens.navigation/navigate-to actual)))))))))

(deftest connected-mailserver
  (testing "it returns true when set in mailserver/current-id"
    (let [cofx {:db {:mailserver/current-id "a"}}]
      (is (mailserver/connected? cofx "a"))))
  (testing "it returns false otherwise"
    (is (not (mailserver/connected? {:db {}} "a")))))

(deftest fetch-mailserver
  (testing "it fetches the mailserver from the db"
    (let [cofx {:db {:mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}]
      (is (mailserver/fetch cofx "a")))))

(deftest fetch-current-mailserver
  (testing "it fetches the mailserver from the db with corresponding id"
    (let [cofx {:db {:mailserver/current-id "a"
                     :mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}]
      (is (mailserver/fetch-current cofx)))))

(deftest set-current-mailserver
  (with-redefs [rand-nth (comp last sort)]
    (let [cofx {:db {:mailserver/mailservers {:eth.beta {"a" {}
                                                         "b" {}
                                                         "c" {}
                                                         "d" {}}}}}]
      (testing "the user has already a preference"
        (let [cofx (assoc-in cofx
                             [:db :account/account :settings]
                             {:mailserver {:eth.beta "a"}})]
          (testing "the mailserver exists"
            (testing "it sets the preferred mailserver"
              (is (= "a" (-> (mailserver/set-current-mailserver cofx)
                             :db
                             :mailserver/current-id)))))
          (testing "the mailserver does not exists"
            (let [cofx (update-in cofx [:db :mailserver/mailservers :eth.beta] dissoc "a")]
              (testing "sets a random mailserver"
                (is (= "d" (-> (mailserver/set-current-mailserver cofx)
                               :db
                               :mailserver/current-id))))))))
      (testing "the user has not set an explicit preference"
        (testing "current-id is not set"
          (testing "it sets a random mailserver"
            (is (= "d" (-> (mailserver/set-current-mailserver cofx)
                           :db
                           :mailserver/current-id)))))
        (testing "current-id is set"
          (testing "it sets the next mailserver"
            (is (= "c" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :mailserver/current-id]
                                                               "b"))
                           :db
                           :mailserver/current-id)))
            (is (= "a" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :mailserver/current-id]
                                                               "d"))
                           :db
                           :mailserver/current-id)))
            (is (= "a" (-> (mailserver/set-current-mailserver (assoc-in
                                                               cofx
                                                               [:db :mailserver/current-id]
                                                               "non-existing"))
                           :db
                           :mailserver/current-id)))))))))

(deftest delete-mailserver
  (testing "the user is not connected to the mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver/mailservers {:eth.beta {"a" {:id           "a"
                                                              :name         "old-name"
                                                              :user-defined true
                                                              :address      "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it removes the mailserver from the list"
        (is (not (mailserver/fetch actual "a"))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "the mailserver is not user-defined"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it does not delete the mailserver"
        (is (= {:dispatch [:navigate-back]} actual)))))
  (testing "the user is connected to the mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver/mailservers {:eth.beta {"a" {:id      "a"
                                                              :name    "old-name"
                                                              :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/delete cofx "a")]
      (testing "it does not remove the mailserver from the list"
        (is (= {:dispatch [:navigate-back]} actual))))))

(deftest upsert-mailserver
  (testing "new mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver.edit/mailserver {:name {:value "test-name"}
                                                  :url  {:value "enode://test-id:test-password@url:port"}}

                     :mailserver/mailservers {}}}
          actual (mailserver/upsert cofx)]

      (testing "it adds the enode to mailserver/mailservers"
        (is (= {:eth.beta {:randomid {:password "test-password"
                                      :address "enode://test-id@url:port"
                                      :name "test-name"
                                      :id :randomid
                                      :user-defined true}}}
               (get-in actual [:db :mailserver/mailservers]))))
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))))
  (testing "existing mailserver"
    (let [cofx {:random-id-generator (constantly "random-id")
                :db {:mailserver.edit/mailserver {:id   {:value  :a}
                                                  :name {:value "new-name"}
                                                  :url  {:value "enode://new-id:new-password@url:port"}}

                     :mailserver/mailservers {:eth.beta {:a {:id      :a
                                                             :name    "old-name"
                                                             :address "enode://old-id:old-password@url:port"}}}}}
          actual (mailserver/upsert cofx)]
      (testing "it navigates back"
        (is (= [:navigate-back]
               (:dispatch actual))))
      (testing "it updates the enode to mailserver/mailservers"
        (is (= {:eth.beta {:a {:password "new-password"
                               :address "enode://new-id@url:port"
                               :name "new-name"
                               :id :a
                               :user-defined true}}}
               (get-in actual [:db :mailserver/mailservers]))))
      (testing "it stores it in the db"
        (is (= 1 (count (:data-store/tx actual)))))
      (testing "it logs the user out if connected to the current mailserver"
        (let [actual (mailserver/upsert (assoc-in cofx
                                                  [:db :mailserver/current-id] :a))]
          (is (= [:accounts.logout.ui/logout-confirmed]
                 (-> actual :data-store/tx first :success-event))))))))

(defn cofx-fixtures [sym-key registered-peer?]
  {:db {:mailserver/state :connected
        :peers-summary (if registered-peer?
                         [{:id "mailserver-id" :enode "enode://mailserver-id@ip"}]
                         [])
        :account/account {:settings {:fleet :eth.beta}}
        :mailserver/current-id "mailserver-a"
        :mailserver/mailservers {:eth.beta {"mailserver-a" {:sym-key-id sym-key
                                                            :address "enode://mailserver-id@ip"}}}}})

(defn peers-summary-change-result [sym-key registered-peer? registered-peer-before?]
  (mailserver/peers-summary-change (cofx-fixtures sym-key
                                                  registered-peer?)
                                   (if registered-peer-before?
                                     [{:id "mailserver-id" :enode "enode://mailserver-id@ip"}]
                                     [])))

(deftest test-resend-request
  (testing "there's no current request"
    (is (not (mailserver/resend-request {:db {}} {}))))
  (testing "there's a current request"
    (testing "it reached the maximum number of attempts"
      (testing "it changes mailserver"
        (is (= :connecting
               (get-in (mailserver/resend-request
                        {:db {:mailserver/current-request
                              {:attempts constants/maximum-number-of-attempts}}}
                        {})
                       [:db :mailserver/state])))))
    (testing "it did not reach the maximum number of attempts"
      (testing "it reached the maximum number of attempts"
        (testing "it decrease the limit")
        (is (= {:mailserver/decrease-limit []} (mailserver/resend-request {:db {:mailserver/current-request
                                                                                {}}}
                                                                          {})))))))

(deftest test-resend-request-request-id
  (testing "request-id passed is nil"
    (testing "it resends the request"
      (is (mailserver/resend-request {:db {:mailserver/current-request {}}} {}))))
  (testing "request-id is nil in db"
    (testing "it resends the request"
      (is (mailserver/resend-request {:db {:mailserver/current-request {}}}
                                     {:request-id "a"}))))
  (testing "request id matches"
    (testing "it resends the request"
      (is (mailserver/resend-request {:db {:mailserver/current-request {:request-id "a"}}}
                                     {:request-id "a"}))))
  (testing "request id does not match"
    (testing "it does not resend the request"
      (is (not (mailserver/resend-request {:db {:mailserver/current-request {:request-id "a"}}}
                                          {:request-id "b"}))))))

(def cofx-no-pub-topic
  {:db
   {:account/account {:public-key "me"}
    :chats
    {"chat-id-1" {:is-active                      true
                  :messages                       {}
                  :might-have-join-time-messages? true
                  :group-chat                     true
                  :public?                        true
                  :chat-id                        "chat-id-1"}
     "chat-id-2" {:is-active  true
                  :messages   {}
                  :group-chat true
                  :public?    true
                  :chat-id    "chat-id-2"}}}})

(def cofx-single-pub-topic
  {:db
   {:account/account {:public-key "me"}
    :chats
    {"chat-id-1" {:is-active                      true
                  :messages                       {}
                  :join-time-mail-request-id      "a"
                  :might-have-join-time-messages? true
                  :group-chat                     true
                  :public?                        true
                  :chat-id                        "chat-id-1"}
     "chat-id-2" {:is-active  true
                  :messages   {}
                  :group-chat true
                  :public?    true
                  :chat-id    "chat-id-2"}}}})

(def cofx-multiple-pub-topic
  {:db
   {:account/account {:public-key "me"}
    :chats
    {"chat-id-1" {:is-active                      true
                  :messages                       {}
                  :join-time-mail-request-id      "a"
                  :might-have-join-time-messages? true
                  :group-chat                     true
                  :public?                        true
                  :chat-id                        "chat-id-1"}
     "chat-id-2" {:is-active                      true
                  :messages                       {}
                  :join-time-mail-request-id      "a"
                  :might-have-join-time-messages? true
                  :group-chat                     true
                  :public?                        true
                  :chat-id                        "chat-id-2"}
     "chat-id-3" {:is-active  true
                  :messages   {}
                  :group-chat true
                  :public?    true
                  :chat-id    "chat-id-3"}
     "chat-id-4" {:is-active                      true
                  :messages                       {}
                  :join-time-mail-request-id      "a"
                  :might-have-join-time-messages? true
                  :group-chat                     true
                  :public?                        true
                  :chat-id                        "chat-id-4"}}}})

(def mailserver-completed-event
  {:requestID "a"
   :lastEnvelopeHash "0xC0FFEE"
   :cursor ""
   :errorMessage ""})

(def mailserver-completed-event-zero-for-envelope
  {:requestID "a"
   :lastEnvelopeHash "0x0000000000000000000000000000000000000000000000000000000000000000"
   :cursor ""
   :errorMessage ""})

(deftest test-public-chat-related-handling-of-request-completed
  (testing "Request does not include any public chat topic"
    (testing "It does not dispatch any event"
      (is (not (or (contains?
                    (mailserver/handle-request-completed cofx-no-pub-topic mailserver-completed-event)
                    :dispatch-n)
                   (contains?
                    (mailserver/handle-request-completed cofx-no-pub-topic mailserver-completed-event)
                    :dispatch-later)))))
    (testing "It has :mailserver/increase-limit effect"
      (is (contains? (mailserver/handle-request-completed cofx-no-pub-topic mailserver-completed-event)
                     :mailserver/increase-limit))))
  (testing "Request includes one public chat topic"
    (testing "Event has non-zero envelope"
      (let [handeled-effects (mailserver/handle-request-completed
                              cofx-single-pub-topic
                              mailserver-completed-event)]
        (testing "It has no :dispatch-n event"
          (is (not (contains?
                    handeled-effects
                    :dispatch-n))))
        (testing "It has one :dispatch-later event"
          (is (= 1 (count (get
                           handeled-effects
                           :dispatch-later)))))
        (testing "The :dispatch-later event is :chat.ui/join-time-messages-checked"
          (is (= :chat.ui/join-time-messages-checked
                 (-> (get
                      handeled-effects
                      :dispatch-later)
                     first
                     :dispatch
                     first))))
        (testing "The :dispatch-later event argument is the chat-id/topic that the request included"
          (is (= "chat-id-1"
                 (-> (get
                      handeled-effects
                      :dispatch-later)
                     first
                     :dispatch
                     second))))
        (testing "It has :mailserver/increase-limit effect"
          (is (contains? handeled-effects
                         :mailserver/increase-limit)))))
    (testing "Event has zero-valued envelope"
      (let [handeled-effects (mailserver/handle-request-completed
                              cofx-single-pub-topic
                              mailserver-completed-event-zero-for-envelope)]
        (testing "It has one :dispatch-n event"
          (is (= 1 (count (get
                           handeled-effects
                           :dispatch-n)))))
        (testing "It has no :dispatch-later event"
          (is (not (contains?
                    handeled-effects
                    :dispatch-later))))
        (testing "The :dispatch-n event is :chat.ui/join-time-messages-checked"
          (is (= :chat.ui/join-time-messages-checked
                 (-> (get
                      handeled-effects
                      :dispatch-n)
                     first
                     first))))
        (testing "The :dispatch-n event argument is the chat-id/topic that the request included"
          (is (= "chat-id-1"
                 (-> (get
                      handeled-effects
                      :dispatch-n)
                     first
                     second))))
        (testing "It has :mailserver/increase-limit effect"
          (is (contains? handeled-effects
                         :mailserver/increase-limit))))))
  (testing "Request includes multiple public chat topics (3)"
    (testing "Event has non-zero envelope"
      (let [handeled-effects (mailserver/handle-request-completed
                              cofx-multiple-pub-topic
                              mailserver-completed-event)]
        (testing "It has no :dispatch-n event"
          (is (not (contains?
                    handeled-effects
                    :dispatch-n))))
        (testing "It has one :dispatch-later event"
          (is (= 3 (count (get
                           handeled-effects
                           :dispatch-later)))))
        (testing "It has :mailserver/increase-limit effect"
          (is (contains? handeled-effects
                         :mailserver/increase-limit)))))
    (testing "Event has zero-valued envelope"
      (let [handeled-effects (mailserver/handle-request-completed
                              cofx-multiple-pub-topic
                              mailserver-completed-event-zero-for-envelope)]
        (testing "It has one :dispatch-n event"
          (is (= 3 (count (get
                           handeled-effects
                           :dispatch-n)))))
        (testing "It has no :dispatch-later event"
          (is (not (contains?
                    handeled-effects
                    :dispatch-later))))
        (testing "It has :mailserver/increase-limit effect"
          (is (contains? handeled-effects
                         :mailserver/increase-limit)))))))

(deftest peers-summary-change
  (testing "Mailserver added, sym-key doesn't exist"
    (let [result (peers-summary-change-result false true false)]
      (is (= (into #{} (keys result))
             #{:mailserver/mark-trusted-peer :shh/generate-sym-key-from-password :db}))))
  (testing "Mailserver disconnected, sym-key exists"
    (let [result (peers-summary-change-result true false true)]
      (is (= (into #{} (keys result))
             #{:db :mailserver/add-peer :utils/dispatch-later :mailserver/update-mailservers}))
      (is (= (get-in result [:db :mailserver/state])
             :connecting))))
  (testing "Mailserver disconnected, sym-key doesn't exists (unlikely situation in practice)"
    (let [result (peers-summary-change-result false false true)]
      (is (= (into #{} (keys result))
             #{:db :mailserver/add-peer :utils/dispatch-later  :shh/generate-sym-key-from-password :mailserver/update-mailservers}))
      (is (= (get-in result [:db :mailserver/state])
             :connecting))))
  (testing "Mailserver isn't concerned by peer summary changes"
    (is (= (into #{} (keys (peers-summary-change-result true true true)))
           #{}))
    (is (= (into #{} (keys (peers-summary-change-result true false false)))
           #{}))))

(deftest unpin-test
  (testing "it removes the preference"
    (let [db {:mailserver/current-id "mailserverid"
              :mailserver/mailservers
              {:eth.beta {"mailserverid" {:address  "mailserver-address"
                                          :password "mailserver-password"}}}
              :account/account
              {:settings {:fleet :eth.beta
                          :mailserver {:eth.beta "mailserverid"}}}}]
      (is (not (get-in (mailserver/unpin {:db db})
                       [:db :account/account :settings :mailserver :eth.beta]))))))

(deftest pin-test
  (testing "it removes the preference"
    (let [db {:mailserver/current-id "mailserverid"
              :mailserver/mailservers
              {:eth.beta {"mailserverid" {:address  "mailserver-address"
                                          :password "mailserver-password"}}}
              :account/account
              {:settings {:fleet :eth.beta
                          :mailserver {}}}}]
      (is (= "mailserverid" (get-in (mailserver/pin {:db db})
                                    [:db :account/account :settings :mailserver :eth.beta]))))))

(deftest connect-to-mailserver
  (let [db {:mailserver/current-id "mailserverid"
            :mailserver/mailservers
            {:eth.beta {"mailserverid" {:address  "mailserver-address"
                                        :password "mailserver-password"}}}
            :account/account
            {:settings {:fleet :eth.beta
                        :mailserver {:eth.beta "mailserverid"}}}}]
    (testing "it adds the peer"
      (is (= "mailserver-address"
             (:mailserver/add-peer (mailserver/connect-to-mailserver {:db db})))))
    (testing "it generates a sym key if hasn't been generated before"
      (is (= "mailserver-password"
             (-> (mailserver/connect-to-mailserver {:db db})
                 :shh/generate-sym-key-from-password
                 first
                 :password))))
    (let [mailserver-with-sym-key-db (assoc-in db
                                               [:mailserver/mailservers :eth.beta "mailserverid" :sym-key-id]
                                               "somesymkeyid")]
      (testing "it does not generate a sym key if already present"
        (is (not (-> (mailserver/connect-to-mailserver {:db mailserver-with-sym-key-db})
                     :shh/generate-sym-key-from-password
                     first)))))))

(deftest check-existing-gaps
  (let []
    (testing "no gaps"
      (is (= {}
             (mailserver/check-existing-gaps
              :chat-id
              nil
              {:from 1
               :to   2}))))
    (testing "request before gaps"
      (is (= {}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 1
               :to   2}))))
    (testing "request between gaps"
      (is (= {}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 22
               :to   28}))))
    (testing "request between gaps"
      (is (= {}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 22
               :to   28}))))
    (testing "request after gaps"
      (is (= {}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 70
               :to   80}))))
    (testing "request covers all gaps"
      (is (= {:deleted-gaps [:g3 :g2 :g1]}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 10
               :to   60}))))
    (testing "request splits gap in two"
      (is (= {:deleted-gaps [:g1]
              :new-gaps     [{:chat-id :chat-id :from 10 :to 12}
                             {:chat-id :chat-id :from 18 :to 20}]}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 12
               :to   18}))))
    (testing "request partially covers one gap #1"
      (is (= {:updated-gaps {:g1 {:from 15
                                  :to   20
                                  :id   :g1}}}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 8
               :to   15}))))
    (testing "request partially covers one gap #2"
      (is (= {:updated-gaps {:g1 {:from 10
                                  :to   15
                                  :id   :g1}}}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 15
               :to   25}))))
    (testing "request partially covers two gaps #2"
      (is (= {:updated-gaps {:g1 {:from 10
                                  :to   15
                                  :id   :g1}
                             :g2 {:from 35
                                  :to   40
                                  :id   :g2}}}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 15
               :to   35}))))
    (testing "request covers one gap and two other partially"
      (is (= {:updated-gaps {:g1 {:from 10
                                  :to   15
                                  :id   :g1}
                             :g3 {:from 55
                                  :to   60
                                  :id   :g3}}
              :deleted-gaps [:g2]}
             (mailserver/check-existing-gaps
              :chat-id
              {:g1 {:from 10
                    :to   20
                    :id   :g1}
               :g2 {:from 30
                    :to   40
                    :id   :g2}
               :g3 {:from 50
                    :to   60
                    :id   :g3}}
              {:from 15
               :to   55}))))))

(defn rand-guid []
  (let [gap-id (atom 0)]
    (fn []
      (swap! gap-id inc)
      (str "gap" @gap-id))))

(deftest prepare-new-gaps
  (testing "prepare-new-gaps"
    (with-redefs [rand/guid (rand-guid)]
      (is (= {"chat1" {"gap1" {:id      "gap1"
                               :chat-id "chat1"
                               :from    20
                               :to      30}}}
             (mailserver/prepare-new-gaps
              nil
              {"chat1"
               {:chat-id             "chat1"
                :lowest-request-from 10
                :highest-request-to  20}}
              {:from 30
               :to   50}
              #{"chat1"})))))
  (testing "prepare-new-gaps request after known range"
    (with-redefs [rand/guid (rand-guid)]
      (is (= {"chat1" {"gap1" {:id      "gap1"
                               :chat-id "chat1"
                               :from    12
                               :to      14}
                       "gap2" {:chat-id "chat1"
                               :from    20
                               :to      30
                               :id      "gap2"}}}
             (mailserver/prepare-new-gaps
              {"chat1" [{:chat-id "chat1"
                         :from    12
                         :to      14}]}
              {"chat1"
               {:chat-id             "chat1"
                :lowest-request-from 10
                :highest-request-to  20}}
              {:from 30
               :to   50}
              #{"chat1"})))))
  (testing "prepare-new-gaps request before known range"
    (with-redefs [rand/guid (rand-guid)]
      (is (= {"chat1" {"gap1" {:chat-id "chat1"
                               :from    12
                               :to      14
                               :id      "gap1"}
                       "gap2" {:chat-id "chat1"
                               :from    8
                               :to      10
                               :id      "gap2"}}}
             (mailserver/prepare-new-gaps
              {"chat1" [{:chat-id "chat1"
                         :from    12
                         :to      14}]}
              {"chat1"
               {:chat-id             "chat1"
                :lowest-request-from 10
                :highest-request-to  20}}
              {:from 2
               :to   8}
              #{"chat1"}))))))
