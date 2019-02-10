(ns status-im.test.contact-recovery.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.config :as config]
            [status-im.contact-recovery.core :as contact-recovery]))

(deftest show-contact-recovery-fx
  (let [public-key "pk"]
    (testing "no contact-recovery in place"
      (let [cofx {:now "now"
                  :db {:contact-recovery/pop-up #{}
                       :account/account {:settings {:pfs? true}}}}
            actual (contact-recovery/handle-contact-recovery-fx cofx public-key)]
        (testing "it sets the pop up as displayed"
          (is (get-in actual [:db :contact-recovery/pop-up public-key])))
        (testing "it adds an fx for fetching the contact"
          (is (= ["now" public-key] (:contact-recovery/handle-recovery actual))))))
    (testing "contact recovery is in place"
      (let [actual (contact-recovery/handle-contact-recovery-fx {:db {:contact-recovery/pop-up #{public-key}}} public-key)]
        (testing "it does nothing"
          (is (not (:db actual)))
          (is (not (:contact-recovery/show-contact-recovery-message actual))))))))

(deftest show-contact-recovery-message
  (let [public-key "pk"]
    (with-redefs [config/show-contact-recovery-pop-up? true]
      (let [cofx {:db {}}
            actual (contact-recovery/handle-recovery cofx public-key)]
        (testing "it shows a pop up"
          (is (:ui/show-confirmation actual)))))))
