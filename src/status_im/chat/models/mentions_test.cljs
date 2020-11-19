(ns status-im.chat.models.mentions-test
  (:require [status-im.chat.models.mentions :as mentions]
            [clojure.string :as string]
            [cljs.test :as test :include-macros true]))

(test/deftest test-replace-mentions
  (let [users {"User Number One"
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
                :public-key "0xpk3"}}]
    (test/testing "empty string"
      (let [text   ""
            result (mentions/replace-mentions text users)]
        (test/is (= result text) (pr-str text))))

    (test/testing "no text"
      (let [text   nil
            result (mentions/replace-mentions text users)]
        (test/is (= result text) (pr-str text))))

    (test/testing "incomlepte mention 1"
      (let [text   "@"
            result (mentions/replace-mentions text users)]
        (test/is (= result text) (pr-str text))))

    (test/testing "incomplete mention 2"
      (let [text   "@r"
            result (mentions/replace-mentions text users)]
        (test/is (= result text) (pr-str text))))

    (test/testing "no mentions"
      (let [text   "foo bar @buzz kek @foo"
            result (mentions/replace-mentions text users)]
        (test/is (= result text) (pr-str text))))

    (test/testing "starts with mention"
      (let [text   "@User Number One"
            result (mentions/replace-mentions text users)]
        (test/is (= result "@0xpk1") (pr-str text))))

    (test/testing "starts with mention, comma after mention"
      (let [text   "@User Number One,"
            result (mentions/replace-mentions text users)]
        (test/is (= result "@0xpk1,") (pr-str text))))

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

    (test/testing "two different mentions, separated with comma"
      (let [text   "@User Number One,@User Number two"
            result (mentions/replace-mentions text users)]
        (test/is (= result  "@0xpk1,@0xpk2") (pr-str text))))

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
        (test/is (= exprected-result result))))
    (test/testing "markdown"
      (test/testing "single * case 1"
        (let [text "*@user2*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 2"
        (let [text "*@user2 *"
              result (mentions/replace-mentions text users)]
          (test/is (= result  "*@0xpk2 *") (pr-str text))))

      (test/testing "single * case 3"
        (let [text "a*@user2*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 4"
        (let [text "*@user2 foo*foo"
              result (mentions/replace-mentions text users)]
          (test/is (= result "*@0xpk2 foo*foo") (pr-str text))))

      (test/testing "single * case 5"
        (let [text "a *@user2*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 6"
        (let [text "*@user2 foo*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 7"
        (let [text "@user2 *@user2 foo* @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "@0xpk2 *@user2 foo* @0xpk2") (pr-str text))))

      (test/testing "single * case 8"
        (let [text "*@user2 foo**@user2 foo*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 9"
        (let [text "*@user2 foo***@user2 foo* @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "*@user2 foo***@user2 foo* @0xpk2") (pr-str text))))

      (test/testing "double * case 1"
        (let [text "**@user2**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 2"
        (let [text "**@user2 **"
              result (mentions/replace-mentions text users)]
          (test/is (= result  "**@0xpk2 **") (pr-str text))))

      (test/testing "double * case 3"
        (let [text "a**@user2**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 4"
        (let [text "**@user2 foo**foo"
              result (mentions/replace-mentions text users)]
          (test/is (= result "**@user2 foo**foo") (pr-str text))))

      (test/testing "double * case 5"
        (let [text "a **@user2**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 6"
        (let [text "**@user2 foo**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 7"
        (let [text "@user2 **@user2 foo** @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "@0xpk2 **@user2 foo** @0xpk2") (pr-str text))))

      (test/testing "double * case 8"
        (let [text "**@user2 foo****@user2 foo**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 9"
        (let [text "**@user2 foo*****@user2 foo** @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "**@user2 foo*****@user2 foo** @0xpk2") (pr-str text))))

      (test/testing "tripple * case 1"
        (let [text "***@user2 foo***@user2 foo*"
              result (mentions/replace-mentions text users)]
          (test/is (= result "***@user2 foo***@0xpk2 foo*") (pr-str text))))

      (test/testing "tripple ~ case 1"
        (let [text "~~~@user2 foo~~~@user2 foo~"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 1"
        (let [text ">@user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 2"
        (let [text "\n>@user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 3"
        (let [text "\n> @user2 \n   \n @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "\n> @user2 \n   \n @0xpk2") (pr-str text))))

      (test/testing "quote case 4"
        (let [text ">@user2\n\n>@user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 5"
        (let [text "***hey\n\n>@user2\n\n@user2 foo***"
              result (mentions/replace-mentions text users)]
          (test/is (= result "***hey\n\n>@user2\n\n@0xpk2 foo***")
                   (pr-str text))))

      (test/testing "code case 1"
        (let [text "` @user2 `"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "code case 2"
        (let [text "` @user2 `"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "code case 3"
        (let [text "``` @user2 ```"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "code case 4"
        (let [text "` ` @user2 ``"
              result (mentions/replace-mentions text users)]
          (test/is (= result  "` ` @0xpk2 ``") (pr-str text)))))))
