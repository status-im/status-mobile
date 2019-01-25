(ns status-im.test.chat.commands.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.commands.core :as core]
            [status-im.chat.commands.protocol :as protocol]))

(defn- fake-suggestion
  [selected-event-creator value]
  (selected-event-creator value))

(def test-command-parameters
  [{:id   :first-param
    :type :text
    ;; pass function as mock-up for suggestions component, so we can
    ;; just test the correct injection of `:set-command-parameter` event
    :suggestions fake-suggestion}
   {:id   :second-param
    :type :text}
   {:id   :last-param
    :type :text
    :suggestions fake-suggestion}])

(deftype TestCommand []
  protocol/Command
  (id [_] "test-command")
  (scope [_] #{:personal-chats :group-chats :public-chats})
  (description [_] "Another test command")
  (parameters [_] test-command-parameters)
  (validate [_ parameters _]
    (when-not (every? (comp string? second) parameters)
      "Not all parameters are filled and of the correct type"))
  (on-send [_ _ _])
  (on-receive [_ _ _])
  (short-preview [_ command-message]
    [:text (str "Test-command, first-param: "
                (get-in command-message [:content :params :first-param]))])
  (preview [_ command-message]
    [:text (str "Test-command, params: "
                (apply str (map [:first-param :second-param :last-param]
                                (get-in command-message [:content :params]))))]))

(def another-test-command-parameters
  [{:id   :first-param
    :type :text}])

(deftype AnotherTestCommand []
  protocol/Command
  (id [_] "another-test-command")
  (scope [_] #{:public-chats})
  (description [_] "Another test command")
  (parameters [_] another-test-command-parameters)
  (validate [_ parameters _]
    (when-not (every? (comp string? second) parameters)
      "Not all parameters are filled and of the correct type"))
  (on-send [_ _ _])
  (on-receive [_ _ _])
  (short-preview [_ command-message]
    [:text (str "Test-command, first-param: "
                (get-in command-message [:content :params :first-param]))])
  (preview [_ command-message]
    [:text (str "Test-command, params: "
                (apply str (map [:first-param]
                                (get-in command-message [:content :params]))))]))

(def TestCommandInstance (TestCommand.))
(def AnotherTestCommandInstance (AnotherTestCommand.))

(deftest load-commands-test
  (let [fx (core/load-commands {:db {}} #{TestCommandInstance AnotherTestCommandInstance})]
    (testing "Primary composite key index for command is correctly created"
      (is (= TestCommandInstance
             (get-in fx [:db :id->command
                         (core/command-id TestCommandInstance) :type]))))
    (testing "Access scope indexes are correctly created"
      (is (contains? (get-in fx [:db :access-scope->command-id #{:personal-chats}])
                     (core/command-id TestCommandInstance)))
      (is (not (contains? (get-in fx [:db :access-scope->command-id #{:personal-chats}])
                          (core/command-id AnotherTestCommandInstance))))
      (is (contains? (get-in fx [:db :access-scope->command-id #{:group-chats}])
                     (core/command-id TestCommandInstance)))
      (is (contains? (get-in fx [:db :access-scope->command-id #{:public-chats}])
                     (core/command-id TestCommandInstance)))
      (is (contains? (get-in fx [:db :access-scope->command-id #{:public-chats}])
                     (core/command-id AnotherTestCommandInstance))))))

(deftest chat-commands-test
  (let [fx (core/load-commands {:db {}} #{TestCommandInstance AnotherTestCommandInstance})]
    (testing "That relevant commands are looked up for chat"
      (is (= #{TestCommandInstance AnotherTestCommandInstance}
             (into #{}
                   (map (comp :type second))
                   (core/chat-commands (get-in fx [:db :id->command])
                                       (get-in fx [:db :access-scope->command-id])
                                       {:chat-id    "topic"
                                        :group-chat true
                                        :public?    true}))))
      (is (= #{TestCommandInstance}
             (into #{}
                   (map (comp :type second))
                   (core/chat-commands (get-in fx [:db :id->command])
                                       (get-in fx [:db :access-scope->command-id])
                                       {:chat-id    "group"
                                        :group-chat true}))))
      (is (= #{TestCommandInstance}
             (into #{}
                   (map (comp :type second))
                   (core/chat-commands (get-in fx [:db :id->command])
                                       (get-in fx [:db :access-scope->command-id])
                                       {:chat-id "contact"})))))))

(def contacts #{"0x0471b2be1e8b971f75b571ba047baa58e2f40f67dad38f6381b2382df43f7176b1813bf372af4cd8451ed9063213029378b9fbc7db792d496e1a6161c42d999edf"
                "0x04b790f2c3f4079f35a1fa396465ceb243cc446c9af211d0a1774f869eb9632a67a6e664e24075ec5c5a8a95a509a2a8173dbfeb88af372e784a37fecc1b5c0ba5"
                "0x04cc3cec3f88dc1a39e224388f0304023fc78c2a7d05e4ebd61638192cc592d2c13d8f081b5d9995dbfcbe45a4ca7eb80d5c505eee660e8fee0df2da222f047287"})

(def contacts_addresses '("0x5adf1b9e1fa4bd4889fecd598b45079045d98f0e"
                          "0x21631d18d9681d4ffdd460fc45fa52159fcd95c8"
                          "0x5541e3be81b76d76cdbf968516caa5a5b773763b"))

(def contacts-list
  (let [pairs (zipmap contacts contacts_addresses)]
    (reduce (fn [acc [pub addr]]
              (assoc acc pub {:address addr})) {} pairs)))

(deftest enrich-command-message-for-events-test-public
  (let [db {:chats {"1" {:contacts nil :public? true :group-chat false}}}
        msg {:chat-id "1"}
        enriched-msg (core/enrich-command-message-for-events db msg)]
    (testing "command-message correctly (not) enriched - public chat"
      (is (= enriched-msg
             (assoc msg :public? true :group-chat false))))))

(deftest enrich-command-message-for-events-test-groupchat
  (let [db {:contacts/contacts contacts-list
            :chats {"1" {:contacts contacts :public? false :group-chat true}}}
        msg {:chat-id "1"}
        enriched-msg (core/enrich-command-message-for-events db msg)]
    (testing "command-message correctly enriched - group chat"
      (is (= enriched-msg
             (assoc msg :public? false :group-chat true :contacts contacts_addresses))))))

(deftest enrich-command-message-for-events-test-1on1-chat
  (let [db {:contacts/contacts contacts-list
            :chats {"1" {:contacts contacts :public? false :group-chat false}}}
        msg {:chat-id "1"}
        enriched-msg (core/enrich-command-message-for-events db msg)]
    (testing "command-message correctly enriched - 1on1 chat"
      (is (= enriched-msg
             (assoc msg :public? false :group-chat false :contact (first contacts_addresses)))))))
