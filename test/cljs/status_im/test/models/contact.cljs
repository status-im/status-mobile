(ns status-im.test.models.contact
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.contact.core :as model]))

(def public-key "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")
(def address "71adb0644e2b590e37dafdfea8bd58f0c7668c7f")

(deftest handle-contact-update-test
  (with-redefs [json-rpc/call (constantly nil)]
    (testing "the contact is not in contacts"
      (let [actual (model/handle-contact-update
                    {:db {}}
                    public-key
                    1
                    {:name "name"
                     :profile-image "image"
                     :address "address"})
            contact (get-in actual [:db :contacts/contacts public-key])]
        (testing "it adds a new contact"
          (is (=  {:public-key       public-key
                   :photo-path       "image"
                   :name             "name"
                   :last-updated     1000
                   :system-tags      #{:contact/request-received}
                   :address          "address"}
                  contact)))))
    (testing "the contact is already in contacts"
      (testing "timestamp is greater than last-updated"
        (let [actual (model/handle-contact-update
                      {:db {:contacts/contacts
                            {public-key {:public-key       public-key
                                         :photo-path       "old-image"
                                         :name             "old-name"
                                         :last-updated     0
                                         :system-tags      #{:contact/added}
                                         :address          "old-address"}}}}
                      public-key
                      1
                      {:name "new-name"
                       :profile-image "new-image"
                       :address "new-address"})
              contact (get-in actual [:db :contacts/contacts public-key])]
          (testing "it updates the contact and adds contact/request-received to system tags"
            (is (=  {:public-key       public-key
                     :photo-path       "new-image"
                     :name             "new-name"
                     :last-updated     1000

                     :system-tags      #{:contact/added :contact/request-received}
                     :address          "new-address"}
                    contact)))))
      (testing "timestamp is equal to last-updated"
        (let [actual (model/handle-contact-update
                      {:db {:contacts/contacts
                            {public-key {:public-key       public-key
                                         :photo-path       "old-image"
                                         :name             "old-name"
                                         :last-updated     1000
                                         :system-tags      #{:contact/added}
                                         :address          "old-address"}}}}
                      public-key
                      1
                      {:name "new-name"
                       :profile-image "new-image"
                       :address "new-address"})
              contact (get-in actual [:db :contacts/contacts public-key])]
          (testing "it does nothing"
            (is (nil? actual)))))
      (testing "timestamp is less than last-updated"
        (let [actual (model/handle-contact-update
                      {:db {:contacts/contacts
                            {public-key {:public-key       public-key
                                         :photo-path       "old-image"
                                         :name             "old-name"
                                         :last-updated     1000
                                         :system-tags      #{:contact/added :contact/request-received}
                                         :address          "old-address"}}}}
                      public-key
                      0
                      {:name "new-name"
                       :profile-image "new-image"
                       :address "new-address"})
              contact (get-in actual [:db :contacts/contacts public-key])]
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
                   :system-tags      #{:contact/added :contact/request-received}
                   :address          address} contact)))))
    (testing "the message is coming from us"
      (testing "it does not update contacts"
        (is (nil? (model/handle-contact-update {:db {:multiaccount {:public-key "me"}}} "me" 1 {})))))))

(deftest add-ens-names-test
  (with-redefs [gfycat/generate-gfy (constantly "generated")
                identicon/identicon (constantly "generated")]
    (testing "adding ens names"
      (let [pk1 "048e57d37615380705cedf2eacc3543e7597eaed38c0bd0ff5b8c759406c657a29b4d6f4018ae323479dafa6bf1c821a422f2478a6759689afbca5e48fba720332"
            pk2 "04318d20a2ca5fd0022579005ed24802e07d4ec610bede808dd13d3318af439e16d55be1a59af007a11120bd1c205861e5f53fe7b000a25e2b0d4eee7f0c5ebf7e"
            expected {(str "0x" pk1) {:alias "generated"
                                      :identicon "generated"
                                      :name "name-1"
                                      :address "6dd28d3d14c6ded091ed38a6735350ce92fe1956"
                                      :ens-verified true
                                      :ens-verified-at 1
                                      :public-key (str "0x" pk1)
                                      :system-tags #{}}
                      (str "0x" pk2) {:alias "generated"
                                      :name  "name-2"
                                      :identicon "generated"
                                      :address "7ab91a68f65c1365d8071302a71599273acb68a2"
                                      :ens-verified false
                                      :ens-verified-at 2
                                      :public-key (str "0x" pk2)
                                      :system-tags #{}}}]
        (is (= expected (model/add-ens-names {} {pk1 {:verified true
                                                      :name "name-1"
                                                      :verifiedAt 1}
                                                 pk2 {:verified false
                                                      :name "name-2"
                                                      :verifiedAt 2}})))))))
