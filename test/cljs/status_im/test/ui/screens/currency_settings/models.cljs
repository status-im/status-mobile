(ns status-im.test.ui.screens.currency-settings.models
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.currency-settings.models :as models]))

(deftest get-currency
  (is (= :usd (models/get-currency {:multiaccount {:settings {:wallet {:currency :usd}}}})))
  (is (= :usd (models/get-currency {:multiaccount {:settings {:wallet {:currency nil}}}})))
  (is (= :usd (models/get-currency {:multiaccount {:settings {:wallet {}}}})))
  (is (= :aud (models/get-currency {:multiaccount {:settings {:wallet {:currency :aud}}}}))))

(deftest set-currency
  (let [cofx (models/set-currency {:db {:multiaccount {:settings {:wallet {}}}}} :usd)]
    (is (= :usd (get-in cofx [:db :multiaccount :settings :wallet :currency]))))
  (is (= :jpy (get-in (models/set-currency {:db {:multiaccount {:settings {:wallet {}}}}} :jpy)
                      [:db :multiaccount :settings :wallet :currency]))))
