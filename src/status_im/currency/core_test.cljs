(ns status-im.currency.core-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.currency.core :as models]))

(deftest get-currency
  (is (= :usd (models/get-currency {:multiaccount {:currency :usd}})))
  (is (= :usd (models/get-currency {:multiaccount {:not-empty "would throw an error if was empty"}})))
  (is (= :aud (models/get-currency {:multiaccount {:currency :aud}}))))

(deftest set-currency
  (let [cofx (models/set-currency {:db {:multiaccount {:not-empty "would throw an error if was empty"}}} :usd)]
    (is (= :usd (get-in cofx [:db :multiaccount :currency]))))
  (is (= :jpy (get-in (models/set-currency {:db {:multiaccount {:not-empty "would throw an error if was empty"}}} :jpy)
                      [:db :multiaccount :currency]))))
