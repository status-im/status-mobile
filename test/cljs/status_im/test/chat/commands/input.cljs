(ns status-im.test.chat.commands.input
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.test.chat.commands.core :as test-core]
            [status-im.chat.commands.core :as core]
            [status-im.chat.commands.input :as input]))

(deftest selected-chat-command-test
  (let [fx       (core/load-commands #{test-core/TestCommandInstance test-core/AnotherTestCommandInstance} {:db {}})
        commands (core/chat-commands (get-in fx [:db :id->command])
                                     (get-in fx [:db :access-scope->command-id])
                                     {:chat-id    "contact"})]
    (testing "Text not beggining with the command special charactes `/` is recognised"
      (is (not (input/selected-chat-command "test-command 1" nil commands))))
    (testing "Command not matching any available commands is not recognised as well"
      (is (not (input/selected-chat-command "/another-test-command" nil commands))))
    (testing "Available correctly entered command is recognised"
      (is (= test-core/TestCommandInstance
             (get (input/selected-chat-command "/test-command" nil commands) :type))))
    (testing "Command completion and param position are determined as well"
      (let [{:keys [current-param-position command-completion]}
            (input/selected-chat-command "/test-command 1 " 17 commands)]
        (is (= 1 current-param-position))
        (is (= :less-then-needed command-completion)))
      (let [{:keys [current-param-position command-completion]}
            (input/selected-chat-command "/test-command 1 2 3" 20 commands)]
        (is (= 2 current-param-position))
        (is (= :complete command-completion))))))

(deftest set-command-parameter-test
  (testing "Setting command parameter correctly updates the text input"
    (let [create-cofx (fn [input-text]
                        {:db {:chats           {"test" {:input-text input-text}}
                              :current-chat-id "test"}})]
      (is (= "/test-command first-value "
             (get-in (input/set-command-parameter
                      false 0 "first-value"
                      (create-cofx "/test-command"))
                     [:db :chats "test" :input-text])))
      (is (= "/test-command first-value second-value \"last value\""
             (get-in (input/set-command-parameter
                      false 1 "second-value"
                      (create-cofx "/test-command first-value edited \"last value\""))
                     [:db :chats "test" :input-text])))
      (is (= "/test-command first-value second-value \"last value\""
             (get-in (input/set-command-parameter
                      false 2 "last value"
                      (create-cofx "/test-command first-value second-value"))
                     [:db :chats "test" :input-text]))))))

(deftest parse-parameters-test
  (testing "testing that parse-parameters work correctly"
    (is (= {:first-param  "1"
            :second-param "2"
            :last-param   "3"}
           (input/parse-parameters test-core/test-command-parameters "/test-command 1 2 3")))
    (is (= {:first-param  "1"
            :second-param "2 2"
            :last-param   "3"}
           (input/parse-parameters test-core/test-command-parameters "/test-command 1 \"2 2\" 3")))
    (is (= {:first-param  "1"
            :second-param "2"}
           (input/parse-parameters test-core/test-command-parameters "/test-command 1 2")))
    (is (= {}
           (input/parse-parameters test-core/test-command-parameters "/test-command ")))))
