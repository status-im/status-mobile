(ns status-im.test.ui.screens.currency-settings.models
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.chat.commands.impl.transactions :as txs]
            [status-im.ui.screens.currency-settings.models :as models]))

(deftest get-currency
  (is (= :usd (txs/get-currency {:account/account {:settings {:wallet {:currency :usd}}}})))
  (is (= :usd (txs/get-currency {:account/account {:settings {:wallet {:currency nil}}}})))
  (is (= :usd (txs/get-currency {:account/account {:settings {:wallet {}}}})))
  (is (= :aud (txs/get-currency {:account/account {:settings {:wallet {:currency :aud}}}}))))

(deftest set-currency
  (let [cofx (models/set-currency {:db {:account/account {:settings {:wallet {}}}}} :usd)]
    (is (= [:db :get-balance :get-tokens-balance :get-prices :data-store/base-tx] (keys cofx)))
    (is (= :usd (get-in cofx [:db :account/account :settings :wallet :currency]))))
  (is (= :jpy (get-in (models/set-currency {:db {:account/account {:settings {:wallet {}}}}} :jpy)
                      [:db :account/account :settings :wallet :currency]))))
