(ns status-im.test.data-store.realm.core
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [status-im.utils.utils :as utils]
            [status-im.data-store.realm.core :as core]))

(def migrated-realm? (atom nil))

(defn fixtures [f]
  (reset! migrated-realm? nil)
  (f))

(use-fixtures :each fixtures)

(deftest migrate-realm
  (with-redefs [core/open-realm  #(reset! migrated-realm? true)]
    (testing "the database does not exists"
      (with-redefs [core/encrypted-realm-version (constantly -1)]
        (core/migrate-realm "test-filename" [] "encryption-key")
        (testing "it migrates the db"
          (is @migrated-realm?))))
    (testing "the database exists"
      (with-redefs [core/encrypted-realm-version (constantly 2)]
        (core/migrate-realm "test-filename" [] "encryption-key")
        (testing "it migrates the db"
          (is @migrated-realm?))))))

(deftest serialization
  (is (nil? (core/deserialize "")))
  (is (nil? (core/deserialize "giberrish")))
  (is (nil? (core/deserialize nil)))
  (is (nil? (core/deserialize (core/serialize nil)))))
