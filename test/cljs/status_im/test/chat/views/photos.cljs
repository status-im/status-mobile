(ns status-im.test.chat.views.photos
  (:require [cljs.test :refer [deftest testing is]]
            [status-im.react-native.resources :as resources]
            [status-im.chat.views.photos :as photos]))

(deftest photos-test
  (testing "a normal string"
    (let [actual (photos/source "some-string")]
      (is (= {:uri "some-string"} actual))))
  (testing "a contact string"
    (with-redefs [resources/contacts {:test "something"}]
      (let [actual (photos/source "contacts://test")]
        (is (= "something" actual))))))
