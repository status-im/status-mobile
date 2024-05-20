(ns status-im.contexts.settings.wallet.events-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    matcher-combinators.test
    [status-im.contexts.settings.wallet.events :as sut]))

(def key-uid "0xfef454bb492ee4677594f8e05921c84f336fa811deb99b8d922477cc87a38b98")

(deftest rename-keypair-test
  (let [new-keypair-name "key pair new"
        cofx             {:db {}}
        expected         {:fx [[:json-rpc/call
                                [{:method     "accounts_updateKeypairName"
                                  :params     [key-uid new-keypair-name]
                                  :on-success [:wallet/rename-keypair-success key-uid new-keypair-name]
                                  :on-error   fn?}]]]}]
    (is (match? expected
                (sut/rename-keypair cofx
                                    [{:key-uid      key-uid
                                      :keypair-name new-keypair-name}])))))
