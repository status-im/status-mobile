(ns status-im.test.ui.screens.add-new.models
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.ui.screens.add-new.models :as models]))

(def cofx {:db {:multiaccount {:public-key "0x04e1433c1a8ad71280e6d4b1814aa3958ba6eb451da47ea1d4a4bfc4a04969c445548f3bd9d40fa7e4356aa62075b4d7615179ef1332f1d6a7c59b96c4ab8e04c1"}}})

(deftest test-handle-qr-code
  (testing "handle contact code"
    (is (= :profile
           (get-in (models/handle-qr-code  cofx "0x04405dfcf94380f9159a1bf8d7dbfce19dd2a3552695bf5e6dd96fb8c3c016c62adaf036b387e7f68621c366186f59dae2561374752996aa13ffc57aad9e6e7202") [:db :view-id]))))
  (testing "handle own contact code"
    (is (= :my-profile
           (get-in (models/handle-qr-code  cofx "0x04e1433c1a8ad71280e6d4b1814aa3958ba6eb451da47ea1d4a4bfc4a04969c445548f3bd9d40fa7e4356aa62075b4d7615179ef1332f1d6a7c59b96c4ab8e04c1") [:db :view-id]))))
  (testing "handle universal link"
    (is (= (:browser/show-browser-selection (models/handle-qr-code  cofx "status-im://browse/www.cryptokitties.co"))
           "www.cryptokitties.co")))
  (testing "handle invalid qr code"
    (is (:utils/show-popup (models/handle-qr-code cofx "a random string")))))
