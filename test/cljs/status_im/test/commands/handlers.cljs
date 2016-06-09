(ns status-im.test.commands.handlers
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.commands.handlers :as h]))

(deftest test-validate-hash
  (let [file "some-js"
        db   (-> {}
                 (assoc-in [:chats :user :dapp-hash] -731917028)
                 (assoc-in [:chats :user2 :dapp-hash] 123))]
    (is (::h/valid-hash (h/validate-hash db [:user file])))
    (is (not (::h/valid-hash (h/validate-hash db [:user2 file]))))
    (is (not (::h/valid-hash (h/validate-hash db [:user3 file]))))))

(deftest test-add-commands
  (let [obj {:commands  {:test {:name        "name"
                                :description "desc"}}
             :responses {:test-r {:name        "r"
                                  :description "desc-r"}}}
        db (h/add-commands {} [:user nil obj])]
    (is (= obj (select-keys (get-in db [:chats :user]) [:commands :responses])))))
