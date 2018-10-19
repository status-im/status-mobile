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
    (testing "Suggestions for parameters are injected with correct selection events"
      (is (= [:chat.ui/set-command-parameter false 0 "first-value"]
             ((get-in fx [:db :id->command
                          (core/command-id TestCommandInstance) :params
                          0 :suggestions])
              "first-value")))
      (is (= [:chat.ui/set-command-parameter true 2 "last-value"]
             ((get-in fx [:db :id->command
                          (core/command-id TestCommandInstance) :params
                          2 :suggestions])
              "last-value"))))
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
