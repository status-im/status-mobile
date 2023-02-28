(ns status-im.chat.models.mentions-test
  (:require [cljs.test :as test]
            [clojure.string :as string]
            [status-im.chat.models.mentions :as mentions]))

(def ->info-input
  [[:text "H."]
   [:mention
    "@helpinghand.eth"]
   [:text
    " "]])

(def ->info-expected
  {:at-sign-idx   2
   :mention-end   19
   :new-text      " "
   :previous-text ""
   :start         18
   :end           18
   :at-idxs       [{:mention? true
                    :from     2
                    :to       17
                    :checked? true}]})

(test/deftest test->info
  (test/testing "->info base case"
    (test/is (= ->info-expected (mentions/->info ->info-input)))))

;; No mention
(def mention-text-1 "parse-text")
(def mention-text-result-1 [[:text "parse-text"]])

;; Mention in the middle
(def mention-text-2
  "hey @0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073 he")
(def mention-text-result-2
  [[:text "hey "]
   [:mention
    "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073"]
   [:text " he"]])

;; Mention at the beginning
(def mention-text-3
  "@0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073 he")
(def mention-text-result-3
  [[:mention
    "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073"]
   [:text " he"]])

;; Mention at the end
(def mention-text-4
  "hey @0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")
(def mention-text-result-4
  [[:text "hey "]
   [:mention
    "0x04fbce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073"]])

;; Invalid mention
(def mention-text-5
  "invalid @0x04fBce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073")
(def mention-text-result-5
  [[:text
    "invalid @0x04fBce10971e1cd7253b98c7b7e54de3729ca57ce41a2bfb0d1c4e0a26f72c4b6913c3487fa1b4bb86125770f1743fb4459da05c1cbe31d938814cfaf36e252073"]])

(test/deftest test-to-input
  (test/testing "only text"
    (test/is (= mention-text-result-1 (mentions/->input-field mention-text-1))))
  (test/testing "in the middle"
    (test/is (= mention-text-result-2 (mentions/->input-field mention-text-2))))
  (test/testing "at the beginning"
    (test/is (= mention-text-result-3 (mentions/->input-field mention-text-3))))
  (test/testing "at the end"
    (test/is (= mention-text-result-4 (mentions/->input-field mention-text-4))))
  (test/testing "invalid"
    (test/is (= mention-text-result-5 (mentions/->input-field mention-text-5)))))

(test/deftest test-replace-mentions
  (let [users {"User Number One"
               {:primary-name "User Number One"
                :public-key   "0xpk1"}
               "User Number Two"
               {:primary-name   "user2"
                :secondary-name "User Number Two"
                :public-key     "0xpk2"}
               "User Number Three"
               {:primary-name   "user3"
                :secondary-name "User Number Three"
                :public-key     "0xpk3"}}]
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
        (test/is (= result "@0xpk1 foo") (pr-str text))))

    (test/testing "starts with some text, then mention"
      (let [text   "text @User Number One"
            result (mentions/replace-mentions text users)]
        (test/is (= result "text @0xpk1") (pr-str text))))

    (test/testing "starts with some text, then mention, then more text"
      (let [text   "text @User Number One foo"
            result (mentions/replace-mentions text users)]
        (test/is (= result "text @0xpk1 foo") (pr-str text))))

    (test/testing "no space before mention"
      (let [text   "text@User Number One"
            result (mentions/replace-mentions text users)]
        (test/is (= result "text@0xpk1") (pr-str text))))

    (test/testing "two different mentions"
      (let [text   "@User Number One @User Number two"
            result (mentions/replace-mentions text users)]
        (test/is (= result "@0xpk1 @0xpk2") (pr-str text))))

    (test/testing "two different mentions, separated with comma"
      (let [text   "@User Number One,@User Number two"
            result (mentions/replace-mentions text users)]
        (test/is (= result "@0xpk1,@0xpk2") (pr-str text))))

    (test/testing "two different mentions inside text"
      (let [text   "foo@User Number One bar @User Number two baz"
            result (mentions/replace-mentions text users)]
        (test/is (= result "foo@0xpk1 bar @0xpk2 baz") (pr-str text))))

    (test/testing "ens mention"
      (let [text   "@user2"
            result (mentions/replace-mentions text users)]
        (test/is (= result "@0xpk2") (pr-str text))))

    (test/testing "multiple mentions"
      (let [text             (string/join
                              " "
                              (repeat 1000 "@User Number One @User Number two"))
            result           (mentions/replace-mentions text users)
            exprected-result (string/join
                              " "
                              (repeat 1000 "@0xpk1 @0xpk2"))]
        (test/is (= exprected-result result))))
    (test/testing "markdown"
      (test/testing "single * case 1"
        (let [text   "*@user2*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 2"
        (let [text   "*@user2 *"
              result (mentions/replace-mentions text users)]
          (test/is (= result "*@0xpk2 *") (pr-str text))))

      (test/testing "single * case 3"
        (let [text   "a*@user2*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 4"
        (let [text   "*@user2 foo*foo"
              result (mentions/replace-mentions text users)]
          (test/is (= result "*@0xpk2 foo*foo") (pr-str text))))

      (test/testing "single * case 5"
        (let [text   "a *@user2*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 6"
        (let [text   "*@user2 foo*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 7"
        (let [text   "@user2 *@user2 foo* @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "@0xpk2 *@user2 foo* @0xpk2") (pr-str text))))

      (test/testing "single * case 8"
        (let [text   "*@user2 foo**@user2 foo*"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "single * case 9"
        (let [text   "*@user2 foo***@user2 foo* @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "*@user2 foo***@user2 foo* @0xpk2") (pr-str text))))

      (test/testing "double * case 1"
        (let [text   "**@user2**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 2"
        (let [text   "**@user2 **"
              result (mentions/replace-mentions text users)]
          (test/is (= result "**@0xpk2 **") (pr-str text))))

      (test/testing "double * case 3"
        (let [text   "a**@user2**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 4"
        (let [text   "**@user2 foo**foo"
              result (mentions/replace-mentions text users)]
          (test/is (= result "**@user2 foo**foo") (pr-str text))))

      (test/testing "double * case 5"
        (let [text   "a **@user2**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 6"
        (let [text   "**@user2 foo**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 7"
        (let [text   "@user2 **@user2 foo** @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "@0xpk2 **@user2 foo** @0xpk2") (pr-str text))))

      (test/testing "double * case 8"
        (let [text   "**@user2 foo****@user2 foo**"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "double * case 9"
        (let [text   "**@user2 foo*****@user2 foo** @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "**@user2 foo*****@user2 foo** @0xpk2") (pr-str text))))

      (test/testing "tripple * case 1"
        (let [text   "***@user2 foo***@user2 foo*"
              result (mentions/replace-mentions text users)]
          (test/is (= result "***@user2 foo***@0xpk2 foo*") (pr-str text))))

      (test/testing "tripple ~ case 1"
        (let [text   "~~~@user2 foo~~~@user2 foo~"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 1"
        (let [text   ">@user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 2"
        (let [text   "\n>@user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 3"
        (let [text   "\n> @user2 \n   \n @user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result "\n> @user2 \n   \n @0xpk2") (pr-str text))))

      (test/testing "quote case 4"
        (let [text   ">@user2\n\n>@user2"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "quote case 5"
        (let [text   "***hey\n\n>@user2\n\n@user2 foo***"
              result (mentions/replace-mentions text users)]
          (test/is (= result "***hey\n\n>@user2\n\n@0xpk2 foo***")
                   (pr-str text))))

      (test/testing "code case 1"
        (let [text   "` @user2 `"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "code case 2"
        (let [text   "` @user2 `"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "code case 3"
        (let [text   "``` @user2 ```"
              result (mentions/replace-mentions text users)]
          (test/is (= result text) (pr-str text))))

      (test/testing "code case 4"
        (let [text   "` ` @user2 ``"
              result (mentions/replace-mentions text users)]
          (test/is (= result "` ` @0xpk2 ``") (pr-str text)))))))
