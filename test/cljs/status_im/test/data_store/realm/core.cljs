(ns status-im.test.data-store.realm.core
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im.data-store.realm.core :as core]))

(deftest serilization
  (is (nil? (core/deserialize "")))
  (is (nil? (core/deserialize "giberrish")))
  (is (nil? (core/deserialize nil)))
  (is (nil? (core/deserialize (core/serialize nil)))))
