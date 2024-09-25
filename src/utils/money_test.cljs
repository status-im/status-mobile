(ns utils.money-test
  (:require
    [cljs.test :refer-macros [deftest testing is are]]
    [utils.money :as money]))

(deftest comparable-test
  (testing "sorts bignumbers (and nils)"
    (is (= [nil
            nil
            (money/bignumber -4)
            (money/bignumber 0)
            (money/bignumber 1)
            (money/bignumber 1.1)
            (money/bignumber 2.1)]
           (sort [(money/bignumber 0)
                  nil
                  (money/bignumber 2.1)
                  (money/bignumber -4)
                  (money/bignumber 1.1)
                  (money/bignumber nil)
                  (money/bignumber 1)]))))

  (testing "throws when comparing non-bignumbers with bignumbers"
    (is (thrown? js/Error (sort [(money/bignumber 42) ""])))))

(deftest equivalence-test
  (testing "equivalence with numbers and strings"
    (is (= (money/bignumber 42) 42))
    (is (= (money/bignumber 42) 42.0))
    (is (= (money/bignumber 42) "42"))
    (is (= (money/bignumber 42) "42.0"))
    (is (not= (money/bignumber 42) :42))
    (is (not= (money/bignumber 42) {:x 42})))

  (testing "bignumbers are never equivalent to nil"
    ;; The native `.eq` function throws with nil values, but we gracefully handle this.
    (is (false? (= (money/bignumber 0) nil)))
    (is (false? (= (money/bignumber 10) (money/bignumber nil)))))

  (testing "equivalence of actual bignumbers"
    (is (= (money/bignumber 0) (money/bignumber 0)))
    (is (= (money/bignumber -1) (money/bignumber -1)))
    (is (not= (money/bignumber 10) (money/bignumber -10)))
    (is (match? {:a {:b {:c (money/bignumber 42)}}}
                {:a {:b {:c (money/bignumber 42)}}}))))

(deftest wei->ether-test
  (testing "Numeric input, 15 significant digits"
    (is (= (str (money/wei->ether 111122223333444000))
           "0.111122223333444")))
  (testing "String input, 18 significant digits"
    (is (= (str (money/wei->ether "111122223333441239"))
           "0.111122223333441239"))))

(deftest valid?-test
  (is (not (true? (money/valid? nil))))
  (is (true? (money/valid? (money/bignumber 0))))
  (is (true? (money/valid? (money/bignumber 1))))
  (is (not (true? (money/valid? (money/bignumber -1))))))

(deftest normalize-test
  (is (= nil (money/normalize nil)))
  (is (= "1" (money/normalize "  1 ")))
  (is (= "1.1" (money/normalize "1.1")))
  (is (= "1.1" (money/normalize "1,1"))))

(deftest format-amount-test
  (are [amount expected]
   (= expected (money/format-amount amount))
   nil       nil
   0         "0"
   1         "1"
   10        "10"
   100       "100"
   999       "999"
   1000      "1K"
   1001      "1K"
   10000     "10K"
   100000    "100K"
   999999    "1000K"
   1000000   "1M"
   1000000   "1M"
   1000001   "1M"
   10000000  "10M"
   100000000 "100M"))

(deftest from-hex-test
  (is (= (money/bignumber "2840425681744351250") (money/from-hex "276b381bbb44d012")))
  (is (= (money/bignumber "0") (money/from-hex "0")))
  (is (= (money/bignumber "255") (money/from-hex "ff")))
  (is (= (money/bignumber "4294967295") (money/from-hex "ffffffff")))
  (is (= (money/bignumber "12345678901234567890") (money/from-hex "ab54a98ceb1f0ad2"))))
