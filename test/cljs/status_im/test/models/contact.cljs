(ns status-im.test.models.contact
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.contact.core :as model]))

(def public-key "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")
(def address "71adb0644e2b590e37dafdfea8bd58f0c7668c7f")

(deftest handle-contact-update-test
  (testing "the contact is not in contacts"
    (let [actual (model/handle-contact-update
                  public-key
                  1
                  {:name "name"
                   :profile-image "image"
                   :address "address"
                   :device-info [{:id "1"
                                  :fcm-token "token-1"}]
                   :fcm-token "token"}
                  {:db {}})
          contact (get-in actual [:db :contacts/contacts public-key])]
      (testing "it stores the contact in the database"
        (is (:data-store/tx actual)))
      (testing "it adds a new contact"
        (is (=  {:public-key       public-key
                 :photo-path       "image"
                 :name             "name"
                 :last-updated     1000
                 :system-tags      #{:contact/request-received}
                 :device-info      {"1" {:id "1"
                                         :timestamp 1
                                         :fcm-token "token-1"}}
                 :fcm-token        "token"
                 :address          "address"}
                contact)))))
  (testing "the contact is already in contacts"
    (testing "timestamp is greater than last-updated"
      (let [actual (model/handle-contact-update
                    public-key
                    1
                    {:name "new-name"
                     :profile-image "new-image"
                     :device-info [{:id "2"
                                    :fcm-token "token-2"}
                                   {:id "3"
                                    :fcm-token "token-3"}]
                     :address "new-address"
                     :fcm-token "new-token"}
                    {:db {:contacts/contacts
                          {public-key {:public-key       public-key
                                       :photo-path       "old-image"
                                       :name             "old-name"
                                       :last-updated     0
                                       :device-info      {"1" {:id "1"
                                                               :timestamp 0
                                                               :fcm-token "token-1"}
                                                          "2" {:id "2"
                                                               :timestamp 0
                                                               :fcm-token "token-2"}}
                                       :system-tags      #{:contact/added}
                                       :fcm-token        "old-token"
                                       :address          "old-address"}}}})
            contact (get-in actual [:db :contacts/contacts public-key])]
        (testing "it stores the contact in the database"
          (is (:data-store/tx actual)))
        (testing "it updates the contact and adds contact/request-received to system tags"
          (is (=  {:public-key       public-key
                   :photo-path       "new-image"
                   :name             "new-name"
                   :last-updated     1000
                   :device-info      {"1" {:id "1"
                                           :fcm-token "token-1"
                                           :timestamp 0}
                                      "2" {:id "2"
                                           :fcm-token "token-2"
                                           :timestamp 1}
                                      "3" {:id "3"
                                           :fcm-token "token-3"
                                           :timestamp 1}}
                   :system-tags      #{:contact/added :contact/request-received}
                   :fcm-token        "new-token"
                   :address          "new-address"}
                  contact)))))
    (testing "timestamp is equal to last-updated"
      (let [actual (model/handle-contact-update
                    public-key
                    1
                    {:name "new-name"
                     :profile-image "new-image"
                     :address "new-address"
                     :fcm-token "new-token"}
                    {:db {:contacts/contacts
                          {public-key {:public-key       public-key
                                       :photo-path       "old-image"
                                       :name             "old-name"
                                       :last-updated     1000
                                       :system-tags      #{:contact/added}
                                       :fcm-token        "old-token"
                                       :address          "old-address"}}}})
            contact (get-in actual [:db :contacts/contacts public-key])]
        (testing "it does nothing"
          (is (nil? actual)))))
    (testing "timestamp is less than last-updated"
      (let [actual (model/handle-contact-update
                    public-key
                    0
                    {:name "new-name"
                     :profile-image "new-image"
                     :address "new-address"
                     :fcm-token "new-token"}
                    {:db {:contacts/contacts
                          {public-key {:public-key       public-key
                                       :photo-path       "old-image"
                                       :name             "old-name"
                                       :last-updated     1000
                                       :system-tags      #{:contact/added :contact/request-received}
                                       :fcm-token        "old-token"
                                       :address          "old-address"}}}})
            contact (get-in actual [:db :contacts/contacts public-key])]
        (testing "it does nothing"
          (is (nil? actual))))))
  (testing "backward compatibility"
    (let [actual (model/handle-contact-update
                  public-key
                  1
                  {:name "new-name"
                   :profile-image "new-image"}
                  {:db {:contacts/contacts
                        {public-key {:public-key       public-key
                                     :photo-path       "old-image"
                                     :device-info      {"1" {:id "1"
                                                             :fcm-token "token-1"}}
                                     :name             "old-name"
                                     :last-updated     0
                                     :system-tags      #{:contact/added}}}}})
          contact (get-in actual [:db :contacts/contacts public-key])]
      (testing "it stores the contact in the database"
        (is (:data-store/tx actual)))
      (testing "it updates the contact"
        (is (=  {:public-key       public-key
                 :photo-path       "new-image"
                 :name             "new-name"
                 :device-info      {"1" {:id "1"
                                         :fcm-token "token-1"}}
                 :last-updated     1000
                 :system-tags      #{:contact/added :contact/request-received}
                 :address          address} contact)))))
  (testing "the message is coming from us"
    (testing "it does not update contacts"
      (is (nil? (model/handle-contact-update "me" 1 {} {:db {:multiaccount {:public-key "me"}}}))))))
