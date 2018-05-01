(ns status-im.test.data-store.realm.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.data-store.realm.core :as core]))

(deftest serialization
  (is (nil? (core/deserialize "")))
  (is (nil? (core/deserialize "giberrish")))
  (is (nil? (core/deserialize nil)))
  (is (nil? (core/deserialize (core/serialize nil)))))
