(ns status-im.test.chat.views.photos
  (:require [cljs.test :refer [deftest testing is]]
            [status-im.react-native.resources :as resources]
            [status-im.utils.image :as utils.image]))

(deftest photos-test
  (testing "a normal string"
    (let [actual (utils.image/source "some-string")]
      (is (= {:uri "some-string"} actual))))
  (testing "a contact string"
    (with-redefs [resources/contacts {:test "something"}]
      (let [actual (utils.image/source "contacts://test")]
        (is (= "something" actual))))))
