(ns status-im.test.data-store.realm.core
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [status-im.utils.utils :as utils]
            [status-im.data-store.realm.core :as core]))

(def showed-popup? (atom nil))
(def resetted-realm? (atom nil))
(def migrated-realm? (atom nil))

(defn fixtures [f]
  (reset! showed-popup? nil)
  (reset! resetted-realm? nil)
  (reset! migrated-realm? nil)
  (f))

(use-fixtures :each fixtures)

(deftest migrate-realm
  (with-redefs [core/reset-realm #(reset! resetted-realm? true)
                utils/show-popup #(reset! showed-popup? true)
                core/open-realm  #(reset! migrated-realm? true)]
    (testing "the database does not exists"
      (with-redefs [core/encrypted-realm-version (constantly -1)]
        (core/migrate-realm "test-filename" [] "encryption-key")
        (testing "it does not reset realm"
          (is (not @resetted-realm?)))
        (testing "it does not show a popup"
          (is (not @showed-popup?)))
        (testing "it migrates the db"
          (is @migrated-realm?))))
    (testing "the database exists"
      (with-redefs [core/encrypted-realm-version (constantly 2)]
        (core/migrate-realm "test-filename" [] "encryption-key")
        (testing "it does not reset realm"
          (is (not @resetted-realm?)))
        (testing "it does not show a popup"
          (is (not @showed-popup?)))
        (testing "it migrates the db"
          (is @migrated-realm?))))
    (testing "the database exists, but is unencrypted"
      (with-redefs [core/encrypted-realm-version #(if @resetted-realm?
                                                    -1
                                                    nil)
                    core/unencrypted-realm?      (constantly true)]
        (core/migrate-realm "test-filename" [] "encryption-key")
        (testing "it resets realm"
          (is @resetted-realm?))
        (testing "it shows a popup"
          (is @showed-popup?))
        (testing "it migrates the db"
          (is @migrated-realm?))))))

(deftest serialization
  (is (nil? (core/deserialize "")))
  (is (nil? (core/deserialize "giberrish")))
  (is (nil? (core/deserialize nil)))
  (is (nil? (core/deserialize (core/serialize nil)))))
