(ns status-im.contact.core-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.contact.core :as model]
            [status-im.ethereum.json-rpc :as json-rpc]))

(def public-key "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")

(deftest handle-contact-update-test
  (with-redefs [json-rpc/call (constantly nil)]
    (testing "the contact is not in contacts"
      (let [actual (model/handle-contact-update
                    {:db {}}
                    public-key
                    1
                    {:name "name"
                     :profile-image "image"})
            contact (get-in actual [:db :contacts/contacts public-key])]
        (testing "it adds a new contact"
          (is (=  {:public-key       public-key
                   :photo-path       "image"
                   :name             "name"
                   :last-updated     1000
                   :system-tags      #{:contact/request-received}}
                  contact)))))
    (testing "the contact is already in contacts"
      (testing "timestamp is greater than last-updated"
        (let [actual (model/handle-contact-update
                      {:db {:contacts/contacts
                            {public-key {:public-key       public-key
                                         :photo-path       "old-image"
                                         :name             "old-name"
                                         :last-updated     0
                                         :system-tags      #{:contact/added}}}}}
                      public-key
                      1
                      {:name "new-name"
                       :profile-image "new-image"})
              contact (get-in actual [:db :contacts/contacts public-key])]
          (testing "it updates the contact and adds contact/request-received to system tags"
            (is (=  {:public-key       public-key
                     :photo-path       "new-image"
                     :name             "new-name"
                     :last-updated     1000

                     :system-tags      #{:contact/added :contact/request-received}}
                    contact)))))
      (testing "timestamp is equal to last-updated"
        (let [actual (model/handle-contact-update
                      {:db {:contacts/contacts
                            {public-key {:public-key       public-key
                                         :photo-path       "old-image"
                                         :name             "old-name"
                                         :last-updated     1000
                                         :system-tags      #{:contact/added}}}}}
                      public-key
                      1
                      {:name "new-name"
                       :profile-image "new-image"})]
          (testing "it does nothing"
            (is (nil? actual)))))
      (testing "timestamp is less than last-updated"
        (let [actual (model/handle-contact-update
                      {:db {:contacts/contacts
                            {public-key {:public-key       public-key
                                         :photo-path       "old-image"
                                         :name             "old-name"
                                         :last-updated     1000
                                         :system-tags      #{:contact/added :contact/request-received}}}}}
                      public-key
                      0
                      {:name "new-name"
                       :profile-image "new-image"})]
          (testing "it does nothing"
            (is (nil? actual))))))
    (testing "backward compatibility"
      (let [actual (model/handle-contact-update
                    {:db {:contacts/contacts
                          {public-key {:public-key       public-key
                                       :photo-path       "old-image"

                                       :name             "old-name"
                                       :last-updated     0
                                       :system-tags      #{:contact/added}}}}}
                    public-key
                    1
                    {:name "new-name"
                     :profile-image "new-image"})
            contact (get-in actual [:db :contacts/contacts public-key])]
        (testing "it updates the contact"
          (is (=  {:public-key       public-key
                   :photo-path       "new-image"
                   :name             "new-name"

                   :last-updated     1000
                   :system-tags      #{:contact/added :contact/request-received}} contact)))))
    (testing "the message is coming from us"
      (testing "it does not update contacts"
        (is (nil? (model/handle-contact-update {:db {:multiaccount {:public-key "me"}}} "me" 1 {})))))))
