(ns status-im.subs.contact-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.contact
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(def contacts-sample-data
  {:selected-contacts-count 1
   :contacts/contacts       {"0xtest"  {:last-updated          1672582629695
                                        :mutual?               true
                                        :contactRequestClock   0
                                        :images                {}
                                        :added?                true
                                        :name                  "slim.shady"
                                        :primary-name          "rslim.shady"
                                        :Removed               false
                                        :trustStatus           0
                                        :alias                 "Real Slim Shady"
                                        :bio                   ""
                                        :IsSyncing             false
                                        :display-name          ""
                                        :ens-verified          true
                                        :socialLinks           nil
                                        :blocked?              false
                                        :active?               true
                                        :verificationStatus    0
                                        :lastUpdatedLocally    1672582563204
                                        :public-key            "0xtest"
                                        :has-added-us?         true
                                        :contact-request-state 1}
                             "0xtest2" {:last-updated          1672582629695
                                        :mutual?               true
                                        :contactRequestClock   0
                                        :images                {}
                                        :added?                true
                                        :name                  "slim.shady"
                                        :primary-name          "fslim.shady"
                                        :Removed               false
                                        :trustStatus           0
                                        :alias                 "Fake Slim Shady"
                                        :bio                   ""
                                        :IsSyncing             false
                                        :display-name          ""
                                        :ens-verified          true
                                        :socialLinks           nil
                                        :blocked?              false
                                        :active?               true
                                        :verificationStatus    0
                                        :lastUpdatedLocally    1672582563204
                                        :public-key            "0xtest"
                                        :has-added-us?         true
                                        :contact-request-state 1}
                             "0xtest3" {:last-updated          1672582629695
                                        :mutual?               true
                                        :contactRequestClock   0
                                        :images                {}
                                        :added?                true
                                        :name                  "slim.shady"
                                        :primary-name          "islim.shady"
                                        :Removed               false
                                        :trustStatus           0
                                        :alias                 "Instant noodles"
                                        :bio                   ""
                                        :IsSyncing             false
                                        :display-name          ""
                                        :ens-verified          true
                                        :socialLinks           nil
                                        :blocked?              false
                                        :active?               true
                                        :verificationStatus    0
                                        :lastUpdatedLocally    1672582563204
                                        :public-key            "0xtest"
                                        :has-added-us?         true
                                        :contact-request-state 1}}})

(def expected-sorted-contacts-without-images
  [{:title "F"
    :data  [{:active?               true
             :last-updated          1672582629695
             :mutual?               true
             :blocked?              false
             :contactRequestClock   0
             :added?                true
             :name                  "slim.shady"
             :primary-name          "fslim.shady"
             :Removed               false
             :trustStatus           0
             :alias                 "Fake Slim Shady"
             :bio                   ""
             :IsSyncing             false
             :display-name          ""
             :ens-verified          true
             :socialLinks           nil
             :allow-new-users?      true
             :verificationStatus    0
             :lastUpdatedLocally    1672582563204
             :public-key            "0xtest"
             :has-added-us?         true
             :contact-request-state 1}]}
   {:title "I"
    :data  [{:active?               true
             :last-updated          1672582629695
             :mutual?               true
             :blocked?              false
             :contactRequestClock   0
             :added?                true
             :name                  "slim.shady"
             :primary-name          "islim.shady"
             :Removed               false
             :trustStatus           0
             :alias                 "Instant noodles"
             :bio                   ""
             :IsSyncing             false
             :display-name          ""
             :ens-verified          true
             :socialLinks           nil
             :allow-new-users?      true
             :verificationStatus    0
             :lastUpdatedLocally    1672582563204
             :public-key            "0xtest"
             :has-added-us?         true
             :contact-request-state 1}]}
   {:title "R"
    :data  [{:active?               true
             :last-updated          1672582629695
             :mutual?               true
             :blocked?              false
             :contactRequestClock   0
             :added?                true
             :name                  "slim.shady"
             :primary-name          "rslim.shady"
             :Removed               false
             :trustStatus           0
             :alias                 "Real Slim Shady"
             :bio                   ""
             :IsSyncing             false
             :display-name          ""
             :ens-verified          true
             :socialLinks           nil
             :allow-new-users?      true
             :verificationStatus    0
             :lastUpdatedLocally    1672582563204
             :public-key            "0xtest"
             :has-added-us?         true
             :contact-request-state 1}]}])

(defn remove-contact-images
  [{:keys [data] :as contact}]
  (assoc contact :data (mapv #(dissoc % :images) data)))

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
      (swap! rf-db/app-db merge
        (update contacts-sample-data :contacts/contacts remove-contact-as-mutual))
      (is (empty? (rf/sub [sub-name])))))
  (testing "Returning sorted contacts"
    (swap! rf-db/app-db merge contacts-sample-data)
    (let [contact-list-without-identicons (mapv (fn [contact-group]
                                                  (update contact-group
                                                          :data
                                                          #(mapv (fn [contact]
                                                                   (dissoc contact :identicon))
                                                                 %)))
                                                (rf/sub [sub-name]))]
      (is (= expected-sorted-contacts-without-images
             (mapv remove-contact-images contact-list-without-identicons))))))

(h/deftest-sub :contacts/contact-two-names-by-identity
  [sub-name]
  (testing "contact is current profile"
    (let [profile-key "profile-key"]
      (swap! rf-db/app-db assoc
        :profile/profile
        {:public-key     profile-key
         :display-name   "Display Name"
         :name           "Name"
         :preferred-name "Preferred Name"}

        :contacts/contacts
        {profile-key   {:primary-name "Primary Name" :public-key profile-key}
         "contact-key" {:secondary-name "Secondary Name"}})

      (is (= ["Preferred Name" nil] (rf/sub [sub-name profile-key])))

      (swap! rf-db/app-db update :profile/profile dissoc :preferred-name)
      (is (= ["Display Name" nil] (rf/sub [sub-name profile-key])))

      (swap! rf-db/app-db update :profile/profile dissoc :display-name)
      (is (= ["Primary Name" nil] (rf/sub [sub-name profile-key])))

      (swap! rf-db/app-db update-in [:contacts/contacts profile-key] dissoc :primary-name)
      (is (= ["Name" nil] (rf/sub [sub-name profile-key])))))

  (testing "contact is not current profile"
    (swap! rf-db/app-db assoc
      :profile/profile   {:public-key "profile-key"}
      :contacts/contacts {"contact-key" {:preferred-name "Preferred Name"
                                         :secondary-name "Secondary Name"}})

    (is (= ["Preferred Name" "Secondary Name"] (rf/sub [sub-name "contact-key"])))))
