(ns status-im.test.chat.views.photos
  (:require [cljs.test :refer [deftest testing is]]
            [status-im.utils.image :as utils.image]))

(deftest photos-test
  (testing "a normal string"
    (let [actual (utils.image/source "some-string")]
      (is (= {:uri "some-string"} actual)))))
