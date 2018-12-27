(ns status-im.test.contact-recovery.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.contact-recovery.core :as contact-recovery]))

(deftest show-contact-recovery-fx
  (let [public-key "pk"]
    (testing "pfs is not enabled"
      (testing "no pop up is displayed"
        (let [actual (contact-recovery/show-contact-recovery-fx {:db {:contact-recovery/pop-up #{}}} public-key)]
          (testing "it does nothing"
            (is (not (:db actual)))
            (is (not (:contact-recovery/show-contact-recovery-message actual)))))))
    (testing "no pop up is displayed"
      (let [cofx {:db {:contact-recovery/pop-up #{}
                       :account/account {:settings {:pfs? true}}}}
            actual (contact-recovery/show-contact-recovery-fx cofx public-key)]
        (testing "it sets the pop up as displayed"
          (is (get-in actual [:db :contact-recovery/pop-up public-key])))
        (testing "it adds an fx for fetching the contact"
          (is (= public-key (:contact-recovery/show-contact-recovery-message actual))))))
    (testing "pop up is already displayed"
      (let [actual (contact-recovery/show-contact-recovery-fx {:db {:contact-recovery/pop-up #{public-key}}} public-key)]
        (testing "it does nothing"
          (is (not (:db actual)))
          (is (not (:contact-recovery/show-contact-recovery-message actual))))))))

(deftest show-contact-recovery-message
  (let [public-key "pk"]
    (testing "pfs is enabled"
      (let [cofx {:db {:account/account {:settings {:pfs? true}}}}
            actual (contact-recovery/show-contact-recovery-message cofx public-key)]
        (testing "it shows a pop up"
          (is (:ui/show-confirmation actual)))))
    (testing "pfs is not enabled"
      (let [cofx {:db {:account/account {:settings {}}}}
            actual (contact-recovery/show-contact-recovery-message cofx public-key)]
        (testing "it shows a pop up"
          (is (not (:ui/show-confirmation actual))))))))
