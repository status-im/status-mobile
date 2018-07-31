(ns status-im.test.ui.screens.wallet.db
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.wallet.db :as wallet.db]
            [status-im.utils.money :as money]
            [status-im.i18n :as i18n]))

(deftest test-too-precise-amount?
  (testing "try both decimal and scientific or hex format"
    (is (= false (wallet.db/too-precise-amount? "100" 2)))
    (is (= false (wallet.db/too-precise-amount? "100" 0)))
    (is (= true (wallet.db/too-precise-amount? "100.1" 0)))
    (is (= false (wallet.db/too-precise-amount? "100.23" 2)))
    (is (= true (wallet.db/too-precise-amount? "100.233" 2)))
    (is (= true (wallet.db/too-precise-amount? "100.0000000000000000001" 18)))
    (is (= false (wallet.db/too-precise-amount? "100.000000000000000001" 18)))
    (is (= false (wallet.db/too-precise-amount? "1e-18" 18)))
    (is (= true (wallet.db/too-precise-amount? "1e-19" 18)))
    (is (= true (wallet.db/too-precise-amount? "0xa.a" 2)))  ;; 0xa.a is 10.625
    (is (= false (wallet.db/too-precise-amount? "0xa.a" 3)))
    (is (= false (wallet.db/too-precise-amount? "1000" 3)))))

(defn- equal-results? [a b]
  (and (= (:error a) (:error b))
       (or (and (nil? (:amount a))
                (nil? (:amount b)))
           (.eq (:amount a) (:amount b)))))

(deftest test-parse-amount
  (testing "test amount parsing"
    (is (equal-results? {:value (money/bignumber "100")} (wallet.db/parse-amount "100" 2)))
    (is (equal-results? {:value (money/bignumber "100")} (wallet.db/parse-amount "100" 0)))
    (is (equal-results? {:error (i18n/label :t/validation-amount-is-too-precise {:decimals 0})
                         :value (money/bignumber "100.1")} (wallet.db/parse-amount "100.1" 0)))
    (is (equal-results? {:value (money/bignumber "100.23")} (wallet.db/parse-amount "100.23" 2)))
    (is (equal-results? {:error (i18n/label :t/validation-amount-is-too-precise {:decimals 2})
                         :value (money/bignumber "100.233")} (wallet.db/parse-amount "100.233" 2)))
    (is (equal-results? {:error (i18n/label :t/validation-amount-is-too-precise {:decimals 18})
                         :value (money/bignumber "100.0000000000000000001")}
                        (wallet.db/parse-amount "100.0000000000000000001" 18)))
    (is (equal-results? {:value (money/bignumber "100.000000000000000001")}
                        (wallet.db/parse-amount "100.000000000000000001" 18)))
    (is (equal-results? {:value (money/bignumber "1e-18")} (wallet.db/parse-amount "1e-18" 18)))
    (is (equal-results? {:error (i18n/label :t/validation-amount-is-too-precise {:decimals 18})
                         :value (money/bignumber "1e-19")} (wallet.db/parse-amount "1e-19" 18)))
    (is (equal-results? {:error (i18n/label :t/validation-amount-is-too-precise {:decimals 2})
                         :value (money/bignumber "10.625")} (wallet.db/parse-amount "0xa.a" 2)))
    (is (equal-results? {:value (money/bignumber "10.625")} (wallet.db/parse-amount "0xa.a" 3)))
    (is (equal-results? {:error (i18n/label :t/validation-amount-invalid-number)
                         :value nil} (wallet.db/parse-amount "SOMETHING" 5)))
    (is (nil? (wallet.db/parse-amount nil 5)))
    (is (nil? (wallet.db/parse-amount "" 5)))
    (is (equal-results? {:value (money/bignumber "1000")} (wallet.db/parse-amount "1000" 3)))))
