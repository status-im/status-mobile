(ns status-im.chat.models.mentions-test
  (:require [status-im.chat.models.mentions :as mentions]
            [clojure.string :as string]
            [cljs.test :as test :include-macros true]))

(test/deftest test-replace-mentions
  (let [users (fn []
                {"User Number One"
                 {:name       "User Number One"
                  :alias      "User Number One"
                  :public-key "0xpk1"}
                 "User Number Two"
                 {:name       "user2"
                  :alias      "User Number Two"
                  :public-key "0xpk2"}
                 "User Number Three"
                 {:name       "user3"
                  :alias      "User Number Three"
                  :public-key "0xpk3"}})]
    (test/testing "no mentions"
      (let [text   "foo bar @buzz kek @foo"
            result (mentions/replace-mentions text users)]
        (test/is (= result text) (pr-str text))))

    (test/testing "starts with mention"
      (let [text   "@User Number One"
            result (mentions/replace-mentions text users)]
        (test/is (= result "@0xpk1") (pr-str text))))

    (test/testing "starts with mention but no space after"
      (let [text   "@User Number Onefoo"
            result (mentions/replace-mentions text users)]
        (test/is (= result text) (pr-str text))))

    (test/testing "starts with mention, some text after mention"
      (let [text   "@User Number One foo"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "@0xpk1 foo") (pr-str text))))

    (test/testing "starts with some text, then mention"
      (let [text   "text @User Number One"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "text @0xpk1") (pr-str text))))

    (test/testing "starts with some text, then mention, then more text"
      (let [text   "text @User Number One foo"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "text @0xpk1 foo") (pr-str text))))

    (test/testing "no space before mention"
      (let [text   "text@User Number One"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "text@0xpk1") (pr-str text))))

    (test/testing "two different mentions"
      (let [text   "@User Number One @User Number two"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "@0xpk1 @0xpk2") (pr-str text))))

    (test/testing "two different mentions inside text"
      (let [text   "foo@User Number One bar @User Number two baz"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "foo@0xpk1 bar @0xpk2 baz") (pr-str text))))

    (test/testing "ens mention"
      (let [text   "@user2"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "@0xpk2") (pr-str text))))

    (test/testing "multiple mentions"
      (let [text (string/join
                  " "
                  (repeat 1000 "@User Number One @User Number two"))
            result (mentions/replace-mentions text users)
            exprected-result (string/join
                              " "
                              (repeat 1000 "@0xpk1 @0xpk2"))]
        (test/is (= exprected-result result))))))
