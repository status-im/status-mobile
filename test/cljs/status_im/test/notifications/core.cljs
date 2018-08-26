(ns status-im.test.notifications.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.notifications.core :as notifications]))

(deftest test-handle-push-notification
  (testing "user's signed in"
    (is (= {:db       {:current-public-key        "0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"
                       :push-notifications/stored {}}
            :dispatch [:navigate-to-chat "0x045db1fdb16c4721ddf32e892c5156d9c7a7445482b84ccd41131eb7970f9d623629d86763c5c2a542ae372815125c27eb73535d583d3285bdbfa16ba37f42e2de"]}
           (notifications/handle-push-notification {:db {:push-notifications/stored {}
                                                         :current-public-key        "0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"}}
                                                   [:push-notification-opened {:from "0x045db1fdb16c4721ddf32e892c5156d9c7a7445482b84ccd41131eb7970f9d623629d86763c5c2a542ae372815125c27eb73535d583d3285bdbfa16ba37f42e2de"
                                                                               :to   "0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"}]))))
  (testing "user's signed in into another account"
    (is (= {}
           (notifications/handle-push-notification {:db {:current-public-key "0x04bc8bf4a91ab726bd98f2c54b3036caacaeea527867945ab839e9ad4e62696856d7f7fa485f68304de357e38a1553eac5592706a16fcf71fd821bbd6c796f9ab3"}}
                                                   [:push-notification-opened {:from "0x045db1fdb16c4721ddf32e892c5156d9c7a7445482b84ccd41131eb7970f9d623629d86763c5c2a542ae372815125c27eb73535d583d3285bdbfa16ba37f42e2de"
                                                                               :to   "0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"}]))))
  (testing "user's not signed in"
    (is (= {:db       {:accounts/accounts           {"bd36cd64e2621b054a3b7464ff1b3c4c304880e7" {:address    "bd36cd64e2621b054a3b7464ff1b3c4c304880e7"
                                                                                                 :photo-path "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAMAAAC7IEhfAAADAFBMVEX"
                                                                                                 :name       "Bob"
                                                                                                 :public-key "0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"}}
                       :current-public-key          nil
                       :push-notifications/initial? true
                       :push-notifications/stored   {"0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"
                                                     "0x045db1fdb16c4721ddf32e892c5156d9c7a7445482b84ccd41131eb7970f9d623629d86763c5c2a542ae372815125c27eb73535d583d3285bdbfa16ba37f42e2de"}}
            :dispatch [:open-login "bd36cd64e2621b054a3b7464ff1b3c4c304880e7" "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAMAAAC7IEhfAAADAFBMVEX" "Bob"]}
           (notifications/handle-push-notification {:db {:accounts/accounts         {"bd36cd64e2621b054a3b7464ff1b3c4c304880e7" {:address    "bd36cd64e2621b054a3b7464ff1b3c4c304880e7"
                                                                                                                                 :photo-path "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAMAAAC7IEhfAAADAFBMVEX"
                                                                                                                                 :name       "Bob"
                                                                                                                                 :public-key "0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"}}
                                                         :current-public-key        nil
                                                         :push-notifications/stored {}}}
                                                   [:push-notification-opened {:from     "0x045db1fdb16c4721ddf32e892c5156d9c7a7445482b84ccd41131eb7970f9d623629d86763c5c2a542ae372815125c27eb73535d583d3285bdbfa16ba37f42e2de"
                                                                               :to       "0x04d2e59a7501a7bc5bc8bf973a0ab95d06225e2b0f53d5f6be719d857c579bdc1b809bfbe0e8393343f9a5b63a9a90a1a58329c6d1c286d6929f01aaa5472ca9c2"
                                                                               :initial? true}])))))
