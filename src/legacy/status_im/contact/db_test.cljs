(ns legacy.status-im.contact.db-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [legacy.status-im.contact.db :as contact.db]))

(deftest contacts-subs
  (testing "get-all-contacts-in-group-chat"
    (let
      [chat-contact-ids
       #{"0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917"
         "0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"
         "0x048a2f8b80c60f89a91b4c1316e56f75b087f446e7b8701ceca06a40142d8efe1f5aa36bd0fee9e248060a8d5207b43ae98bef4617c18c71e66f920f324869c09f"}
       admins
       #{"0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917"}

       contacts
       {"0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"
        {:last-updated 0
         :name "User B"
         :primary-name "User B"
         :last-online 0
         :public-key
         "0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"}}
       current-multiaccount
       {:last-updated 0
        :signed-up? true
        :sharing-usage-data? false
        :primary-name "User A"
        :name "User A"
        :public-key
        "0x048a2f8b80c60f89a91b4c1316e56f75b087f446e7b8701ceca06a40142d8efe1f5aa36bd0fee9e248060a8d5207b43ae98bef4617c18c71e66f920f324869c09f"}]
      (is
       (=
        (contact.db/get-all-contacts-in-group-chat chat-contact-ids
                                                   admins
                                                   contacts
                                                   current-multiaccount)
        [{:admin? true
          :public-key
          "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917"}
         {:alias "User A"
          :primary-name "User A"
          :public-key
          "0x048a2f8b80c60f89a91b4c1316e56f75b087f446e7b8701ceca06a40142d8efe1f5aa36bd0fee9e248060a8d5207b43ae98bef4617c18c71e66f920f324869c09f"}
         {:last-updated 0
          :name "User B"
          :primary-name "User B"
          :last-online 0
          :public-key
          "0x04985040682b77a32bb4bb58268a0719bd24ca4d07c255153fe1eb2ccd5883669627bd1a092d7cc76e8e4b9104327667b19dcda3ac469f572efabe588c38c1985f"}])))))
