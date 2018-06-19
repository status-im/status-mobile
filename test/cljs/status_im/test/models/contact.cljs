(ns status-im.test.models.contact
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.models.contact :as model]))

(def public-key "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")
(def address "71adb0644e2b590e37dafdfea8bd58f0c7668c7f")

(deftest can-add-to-contact-test
  (testing "a user is already in contacts"
    (is (not (model/can-add-to-contacts? {:pending? false}))))
  (testing "a user is pending"
    (testing "a normal user"
      (is (model/can-add-to-contacts? {:pending? true})))
    (testing "a dapp"
      (is (not (model/can-add-to-contacts? {:pending? true
                                            :dapp?    true})))))
  (testing "the user is not in the contacts"
    (testing "a normal user"
      (is (model/can-add-to-contacts? {})))
    (testing "a dapp"
      (is (not (model/can-add-to-contacts? {:dapp? true}))))))

(deftest handle-contact-update-test
  (testing "the contact is not in contacts"
    (let [actual (model/handle-contact-update
                  public-key
                  1
                  {:name "name"
                   :profile-image "image"
                   :address "address"
                   :fcm-token "token"}
                  {})
          contact (get-in actual [:db :contacts/contacts public-key])]
      (testing "it stores the contact in the database"
        (is (:data-store/tx actual)))
      (testing "it adds a new contact with pending? true"
        (is (=  {:whisper-identity public-key
                 :public-key       public-key
                 :photo-path       "image"
                 :name             "name"
                 :last-updated     1000
                 :pending?         true
                 :fcm-token        "token"
                 :address          "address"} contact)))))
  (testing "the contact is already in contacts"
    (testing "timestamp is greather than last-updated"
      (let [actual (model/handle-contact-update
                    public-key
                    1
                    {:name "new-name"
                     :profile-image "new-image"
                     :address "new-address"
                     :fcm-token "new-token"}
                    {:db {:contacts/contacts
                          {public-key {:whisper-identity public-key
                                       :public-key       public-key
                                       :photo-path       "old-image"
                                       :name             "old-name"
                                       :last-updated     0
                                       :pending?         false
                                       :fcm-token        "old-token"
                                       :address          "old-address"}}}})
            contact (get-in actual [:db :contacts/contacts public-key])]
        (testing "it stores the contact in the database"
          (is (:data-store/tx actual)))
        (testing "it updates the contact leaving pending unchanged"
          (is (=  {:whisper-identity public-key
                   :public-key       public-key
                   :photo-path       "new-image"
                   :name             "new-name"
                   :last-updated     1000
                   :pending?         false
                   :fcm-token        "new-token"
                   :address          "new-address"} contact)))))
    (testing "timestamp is equal than last-updated"
      (let [actual (model/handle-contact-update
                    public-key
                    1
                    {:name "new-name"
                     :profile-image "new-image"
                     :address "new-address"
                     :fcm-token "new-token"}
                    {:db {:contacts/contacts
                          {public-key {:whisper-identity public-key
                                       :public-key       public-key
                                       :photo-path       "old-image"
                                       :name             "old-name"
                                       :last-updated     1000
                                       :pending?         false
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
                          {public-key {:whisper-identity public-key
                                       :public-key       public-key
                                       :photo-path       "old-image"
                                       :name             "old-name"
                                       :last-updated     1000
                                       :pending?         false
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
                        {public-key {:whisper-identity public-key
                                     :public-key       public-key
                                     :photo-path       "old-image"
                                     :name             "old-name"
                                     :last-updated     0
                                     :pending?         false}}}})
          contact (get-in actual [:db :contacts/contacts public-key])]
      (testing "it stores the contact in the database"
        (is (:data-store/tx actual)))
      (testing "it updates the contact leaving pending unchanged"
        (is (=  {:whisper-identity public-key
                 :public-key       public-key
                 :photo-path       "new-image"
                 :name             "new-name"
                 :last-updated     1000
                 :pending?         false
                 :address          address} contact)))))
  (testing "the message is coming from us"
    (testing "it does not update contacts"
      (is (nil? (model/handle-contact-update "me" 1 {} {:db {:current-public-key "me"}}))))))
