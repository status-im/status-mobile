(ns status-im2.subs.contact-test
  (:require [cljs.test :refer [is testing]]
            [re-frame.db :as rf-db]
            [status-im.test-helpers :as h]
            status-im2.subs.contact
            [utils.re-frame :as rf]))
(def ^:private contacts-sample-data
  {:selected-contacts-count 1
   :contacts/contacts       {"0xtest"  {:last-updated          1672582629695
                                        :mutual?               true
                                        :contactRequestClock   0
                                        :images                {}
                                        :added                 true
                                        :name                  "slim.shady"
                                        :Removed               false
                                        :trustStatus           0
                                        :alias                 "Real Slim Shady"
                                        :bio                   ""
                                        :IsSyncing             false
                                        :display-name          ""
                                        :ens-verified          true
                                        :socialLinks           nil
                                        :blocked               false
                                        :verificationStatus    0
                                        :lastUpdatedLocally    1672582563204
                                        :public-key            "0xtest"
                                        :has-added-us          true
                                        :contact-request-state 1}
                             "0xtest2" {:last-updated          1672582629695
                                        :mutual?               true
                                        :contactRequestClock   0
                                        :images                {}
                                        :added                 true
                                        :name                  "slim.shady"
                                        :Removed               false
                                        :trustStatus           0
                                        :alias                 "Fake Slim Shady"
                                        :bio                   ""
                                        :IsSyncing             false
                                        :display-name          ""
                                        :ens-verified          true
                                        :socialLinks           nil
                                        :blocked               false
                                        :verificationStatus    0
                                        :lastUpdatedLocally    1672582563204
                                        :public-key            "0xtest"
                                        :has-added-us          true
                                        :contact-request-state 1}
                             "0xtest3" {:last-updated          1672582629695
                                        :mutual?               true
                                        :contactRequestClock   0
                                        :images                {}
                                        :added                 true
                                        :name                  "slim.shady"
                                        :Removed               false
                                        :trustStatus           0
                                        :alias                 "Instant noodles"
                                        :bio                   ""
                                        :IsSyncing             false
                                        :display-name          ""
                                        :ens-verified          true
                                        :socialLinks           nil
                                        :blocked               false
                                        :verificationStatus    0
                                        :lastUpdatedLocally    1672582563204
                                        :public-key            "0xtest"
                                        :has-added-us          true
                                        :contact-request-state 1}}})

(def ^:private expected-sorted-contacts
  '({:title "F"
     :data  ({:active?               true
              :last-updated          1672582629695
              :mutual?               true
              :blocked?              false
              :contactRequestClock   0
              :images                {}
              :added                 true
              :name                  "slim.shady"
              :added?                true
              :Removed               false
              :trustStatus           0
              :alias                 "Fake Slim Shady"
              :bio                   ""
              :IsSyncing             false
              :display-name          ""
              :ens-verified          true
              :socialLinks           nil
              :blocked               false
              :allow-new-users?      true
              :verificationStatus    0
              :lastUpdatedLocally    1672582563204
              :public-key            "0xtest"
              :names                 {:nickname         nil
                                      :display-name     ""
                                      :three-words-name "Fake Slim Shady"
                                      :ens-name         "@slim.shady"}
              :has-added-us          true
              :contact-request-state 1})}
    {:title "I"
     :data  ({:active?               true
              :last-updated          1672582629695
              :mutual?               true
              :blocked?              false
              :contactRequestClock   0
              :images                {}
              :added                 true
              :name                  "slim.shady"
              :added?                true
              :Removed               false
              :trustStatus           0
              :alias                 "Instant noodles"
              :bio                   ""
              :IsSyncing             false
              :display-name          ""
              :ens-verified          true
              :socialLinks           nil
              :blocked               false
              :allow-new-users?      true
              :verificationStatus    0
              :lastUpdatedLocally    1672582563204
              :public-key            "0xtest"
              :names                 {:nickname         nil
                                      :display-name     ""
                                      :three-words-name "Instant noodles"
                                      :ens-name         "@slim.shady"}
              :has-added-us          true
              :contact-request-state 1})}
    {:title "R"
     :data  ({:active?               true
              :last-updated          1672582629695
              :mutual?               true
              :blocked?              false
              :contactRequestClock   0
              :images                {}
              :added                 true
              :name                  "slim.shady"
              :added?                true
              :Removed               false
              :trustStatus           0
              :alias                 "Real Slim Shady"
              :bio                   ""
              :IsSyncing             false
              :display-name          ""
              :ens-verified          true
              :socialLinks           nil
              :blocked               false
              :allow-new-users?      true
              :verificationStatus    0
              :lastUpdatedLocally    1672582563204
              :public-key            "0xtest"
              :names                 {:nickname         nil
                                      :display-name     ""
                                      :three-words-name "Real Slim Shady"
                                      :ens-name         "@slim.shady"}
              :has-added-us          true
              :contact-request-state 1})}))

(h/deftest-sub :contacts/sorted-and-grouped-by-first-letter
  [sub-name]
  (testing "Returning empty sequence when no contacts"
    (swap! rf-db/app-db merge (assoc contacts-sample-data :contacts/contacts {}))
    (is (empty? (rf/sub [sub-name]))))
  (testing "Returning empty sequence when no mutual contacts"
    (let [remove-contact-as-mutual #(-> %
                                        (assoc-in ["0xtest" :mutual?] false)
                                        (assoc-in ["0xtest2" :mutual?] false)
                                        (assoc-in ["0xtest3" :mutual?] false))]
<<<<<<< HEAD
<<<<<<< HEAD
=======
      ;; TODO(@ibrkhalil,2023-01-01): Replace with update-vals when we update the ClojureScript API
>>>>>>> 852db7a58 (Typo)
=======
>>>>>>> 140ec965e (Comments)
      (swap! rf-db/app-db merge
        (update contacts-sample-data :contacts/contacts remove-contact-as-mutual))
      (is (empty? (rf/sub [sub-name])))))
  (testing "Returning sorted contacts"
    (swap! rf-db/app-db merge contacts-sample-data)
    (let [contact-list-without-identicons (map (fn [contact-group]
                                                 (update contact-group
                                                         :data
                                                         #(map (fn [contact]
                                                                 (dissoc contact :identicon))
                                                               %)))
                                               (rf/sub [sub-name]))]

      (is (= expected-sorted-contacts contact-list-without-identicons)))))
