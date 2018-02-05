(ns status-im.test.chat.models.input
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.chat.models.input :as input]))

(def fake-db
  {:access-scope->commands-responses {#{:global :personal-chats :anonymous :dapps} {:command {"global-command1" ["0x1" :command 0 "global-command1"]}}
                                      #{"0x1" :personal-chats :anonymous :dapps} {:command {"command2" ["0x1" :command 2 "command2"]}}
                                      #{"0x1" :group-chats :anonymous :dapps} {:command {"command2" ["0x1" :command 4 "command2"]}}
                                      #{"0x2" :personal-chats :anonymous :dapps} {:command {"command3" ["0x2" :command 2 "command3"]}}
                                      #{"0x2" :group-chats :anonymous :dapps} {:response {"response1" ["0x2" :response 4 "response1"]}}}
   :chats                            {"test1" {:contacts      [{:identity "0x1"}]
                                               :requests      nil
                                               :seq-arguments ["arg1" "arg2"]}
                                      "test2" {:contacts   [{:identity "0x1"}
                                                            {:identity "0x2"}]
                                               :group-chat true
                                               :requests   {"id1" {:message-id "id1"
                                                                   :response   "response1"}}}
                                      "test3" {:contacts [{:identity "0x1"}]
                                               :requests {"id1" {:message-id "id1"
                                                                 :response   "request1"}}}
                                      "test4" {:contacts       [{:identity "0x1"}
                                                                {:identity "0x2"}]
                                               :group-chat     true
                                               :requests       {"id2" {:message-id "id2"
                                                                       :response   "response1"}}
                                               :input-metadata {:meta-k "meta-v"}}}
   :contacts/contacts                {"0x1" {:dapp?    true
                                             :command  {0 {"global-command1" {:name "global-command1"
                                                                              :ref ["0x1" :command 0 "global-command1"]}}
                                                        2 {"command2" {:name "command2"
                                                                       :ref ["0x1" :command 2 "command2"]}}
                                                        4 {"command2" {:name "command2"
                                                                       :ref ["0x1" :command 4 "command2"]}}}
                                             :response {}}
                                      "0x2" {:dapp?    true
                                             :command  {2 {"command3" {:name "command3"
                                                                       :ref ["0x2" :command 2 "command3"]}}}
                                             :response {4 {"response1" {:name "response1"
                                                                        :ref ["0x2" :response 4 "response1"]}}}}}})

(deftest text->emoji
  (is (nil? (input/text->emoji nil)))
  (is (= "" (input/text->emoji "")))
  (is (= "test" (input/text->emoji "test")))
  (is (= "word1 \uD83D\uDC4D word2" (input/text->emoji "word1 :+1: word2"))))

(deftest starts-as-command?
  (is (not (input/starts-as-command? nil)))
  (is (not (input/text-ends-with-space? "")))
  (is (not (input/text-ends-with-space? "word1 word2 word3")))
  (is (input/text-ends-with-space? "word1 word2 ")))

(deftest split-command-args
  (is (nil? (input/split-command-args nil)))
  (is (= [""] (input/split-command-args "")))
  (is (= ["@browse" "google.com"] (input/split-command-args "@browse google.com")))
  (is (= ["@browse" "google.com"] (input/split-command-args "  @browse   google.com  ")))
  (is (= ["/send" "1.0" "John Doe"] (input/split-command-args "/send 1.0 \"John Doe\"")))
  (is (= ["/send" "1.0" "John Doe"] (input/split-command-args "/send     1.0     \"John     Doe\"   "))))

(deftest join-command-args
  (is (nil? (input/join-command-args nil)))
  (is (= "" (input/join-command-args [""])))
  (is (= "/send 1.0 \"John Doe\"" (input/join-command-args ["/send" "1.0" "John Doe"]))))

(deftest selected-chat-command
  (is (= (input/selected-chat-command (-> fake-db
                                          (assoc :current-chat-id "test1")
                                          (assoc-in [:chats "test1" :input-text] "/global-command1")))
         {:command {:name "global-command1"
                    :ref ["0x1" :command 0 "global-command1"]}
          :metadata nil
          :args ["arg1" "arg2"]}))
  (is (= (input/selected-chat-command (-> fake-db
                                          (assoc :current-chat-id "test2")
                                          (assoc-in [:chats "test2" :input-text] "/command2")))
         {:command {:name "command2"
                    :ref ["0x1" :command 4 "command2"]}
          :metadata nil
          :args []}))
  (is (nil? (input/selected-chat-command (-> fake-db
                                             (assoc :current-chat-id "test1")
                                             (assoc-in [:chats "test1" :input-text] "/command3")))))
  (is (= (input/selected-chat-command (-> fake-db
                                          (assoc :current-chat-id "test1")
                                          (assoc-in [:chats "test1" :input-text] "/command2")))
         {:command {:name "command2"
                    :ref ["0x1" :command 2 "command2"]}
          :metadata nil
          :args ["arg1" "arg2"]}))
  (is (= (input/selected-chat-command (-> fake-db
                                          (assoc :current-chat-id "test2")
                                          (assoc-in [:chats "test2" :input-text] "/response1 arg1")))
         {:command {:name "response1"
                    :ref ["0x2" :response 4 "response1"]}
          :metadata nil
          :args ["arg1"]}))
  (is (= (input/selected-chat-command (-> fake-db
                                          (assoc :current-chat-id "test4")
                                          (assoc-in [:chats "test4" :input-text] "/command2 arg1")))
         {:command {:name "command2"
                    :ref ["0x1" :command 4 "command2"]}
          :metadata {:meta-k "meta-v"}
          :args ["arg1"]})))

(deftest current-chat-argument-position
  (is (= (input/current-chat-argument-position
          {:name "command1"} "/command1 arg1 arg2 " 0 nil) -1))
  (is (= (input/current-chat-argument-position
          {:name "command1"} "/command1 argument1 arg2 " 9 nil) -1))
  (is (= (input/current-chat-argument-position
          {:name "command1"} "/command1 argument1 arg2 " 10 nil) 0))
  (is (= (input/current-chat-argument-position
          {:name "command1"} "/command1 argument1 arg2 " 19 nil) 0))
  (is (= (input/current-chat-argument-position
          {:name "command1"} "/command1 argument1 arg2 " 20 nil) 1))
  (is (= (input/current-chat-argument-position
          {:name "command2"} "/command2 \"a r g u m e n t 1\" argument2" 30 nil) 1))
  (is (= (input/current-chat-argument-position
          {:name "command3" :command {:sequential-params true}} "/command3" 0 ["test1" "test2"]) 2)))

(deftest argument-position
  "Doesn't require a separate test because it simply calls `current-chat-argument-position")

(deftest command-completion
  (is (= (input/command-completion {:args    ["p1" "p2"]
                                    :command {:params [{:optional false} {:optional false}]}})
         :complete))
  (is (= (input/command-completion {:args    ["p1"]
                                    :command {:params [{:optional false} {:optional false}]}})
         :less-than-needed))
  (is (= (input/command-completion {:args    ["p1" "p2" "p3"]
                                    :command {:params [{:optional false} {:optional false}]}})
         :more-than-needed))
  (is (= (input/command-completion {:args    ["p1" "p2"]
                                    :command {:params [{:optional false} {:optional false} {:optional true}]}})
         :complete))
  (is (= (input/command-completion {:args    ["p1" "p2" "p3"]
                                    :command {:params [{:optional false} {:optional false} {:optional true}]}})
         :complete))
  (is (= (input/command-completion {:args    ["p1" "p2" "p3" "p4"]
                                    :command {:params [{:optional false} {:optional false} {:optional true}]}})
         :more-than-needed))
  (is (= (input/command-completion {:command {:params [{:optional false}]}})
         :less-than-needed))
  (is (= (input/command-completion {:command {}})
         :complete))
  (is (= (input/command-completion nil)
         :no-command)))

(deftest args->params
  (is (= {} (input/args->params nil)))
  (is (= {} (input/args->params {})))
  (is (= {} (input/args->params {:args ["1.0"]})))
  (is (= {:amount "1.0"}
         (input/args->params {:command {:params [{:name "amount"}]}
                              :args    ["1.0"]})))
  (is (= {:amount "1.0"}
         (input/args->params {:command {:params [{:name "amount"}]}
                              :args    ["1.0" "2.0" "3.0"]})))
  (is (= {:amount "1.0"}
         (input/args->params {:command {:params [{:name "amount"} {:name "recipient"}]}
                              :args    ["1.0"]})))
  (is (= {:amount "1.0" :recipient "John Doe"}
         (input/args->params {:command {:params [{:name "amount"} {:name "recipient"}]}
                              :args    ["1.0" "John Doe"]}))))

(deftest modified-db-after-change
  "Just a combination of db modifications. Can be skipped now")
