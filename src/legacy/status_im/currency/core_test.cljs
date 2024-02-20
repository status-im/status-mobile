(ns legacy.status-im.currency.core-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [legacy.status-im.currency.core :as models]))

(deftest get-currency
  (is (= :usd (models/get-currency {:profile/profile {:currency :usd}})))
  (is (= :usd (models/get-currency {:profile/profile {:not-empty "would throw an error if was empty"}})))
  (is (= :aud (models/get-currency {:profile/profile {:currency :aud}}))))

(deftest set-currency
  (let [cofx (models/set-currency {:db {:profile/profile {:not-empty
                                                          "would throw an error if was empty"}}}
                                  :usd)]
    (is (= :usd (get-in cofx [:db :profile/profile :currency]))))
  (is
   (= :jpy
      (get-in (models/set-currency {:db {:profile/profile {:not-empty
                                                           "would throw an error if was empty"}}}
                                   :jpy)
              [:db :profile/profile :currency]))))
