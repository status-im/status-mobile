(ns status-im.test.contacts.db
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.utils.identicon :as identicon]
            [status-im.contact.db :as contact.db]))

(deftest contacts-subs
  (testing "get-all-contacts-in-group-chat"
    (with-redefs [identicon/identicon (constantly "generated")]
      (let [chat-contact-ids #{"0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917"
                               "0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"
                               "0x048a2f8b80c60f89a91b4c1316e56f75b087f446e7b8701ceca06a40142d8efe1f5aa36bd0fee9e248060a8d5207b43ae98bef4617c18c71e66f920f324869c09f"}
            admins           #{"0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917"}

            contacts         {"0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"
                              {:description   nil,
                               :last-updated  0,
                               :hide-contact? false,
                               :address       "eca8218b5ebeb2c47ba94c1b6e0a779d78fff7bc",
                               :name          "User B",
                               :fcm-token     nil,
                               :photo-path    "photo1",
                               :status        nil,
                               :blocked?      false,
                               :pending?      true,
                               :last-online   0,
                               :public-key
                               "0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"}}
            current-account  {:last-updated        0,
                              :address             "f23d28f538fd8cd4a90c2d96ca89f5bccca5383f",
                              :signed-up?          true,
                              :sharing-usage-data? false,
                              :name                "User A",
                              :photo-path          "photo2",
                              :public-key
                              "0x048a2f8b80c60f89a91b4c1316e56f75b087f446e7b8701ceca06a40142d8efe1f5aa36bd0fee9e248060a8d5207b43ae98bef4617c18c71e66f920f324869c09f"}]
        (is (= (contact.db/get-all-contacts-in-group-chat chat-contact-ids
                                                          admins
                                                          contacts
                                                          current-account)
               [{:name             "Snappy Impressive Leonberger"
                 :photo-path       "generated"
                 :admin?           true
                 :public-key "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917"}
                {:name             "User A"
                 :photo-path       "photo2"
                 :public-key "0x048a2f8b80c60f89a91b4c1316e56f75b087f446e7b8701ceca06a40142d8efe1f5aa36bd0fee9e248060a8d5207b43ae98bef4617c18c71e66f920f324869c09f"}
                {:description      nil
                 :last-updated     0
                 :hide-contact?    false
                 :address          "eca8218b5ebeb2c47ba94c1b6e0a779d78fff7bc"
                 :name             "User B"
                 :fcm-token        nil
                 :photo-path       "photo1"
                 :status           nil
                 :blocked?         false
                 :pending?         true
                 :last-online      0
                 :public-key       "0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"}]))))))
