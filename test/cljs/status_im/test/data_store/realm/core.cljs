(ns status-im.test.data-store.realm.core
  (:require [cljs.test :refer-macros [deftest is testing use-fixtures]]
            [status-im.utils.utils :as utils]
            [clojure.string :as string]
            [status-im.data-store.realm.core :as core]))

(def migrated-realm? (atom nil))

(defn fixtures [f]
  (reset! migrated-realm? nil)
  (f))

(def valid-account-path
  (str "/some/" (string/join (repeat 40 "a"))))

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

(deftest is-account-file-test
  (testing "not an account file"
    (is (not (core/is-account-file? "not-one")))
    (is (not (core/is-account-file? "000000000000000009212102"))))
  (testing "an account file"
    (is (core/is-account-file? valid-account-path))))

(deftest is-realm-file-test
  (testing "not a realm file"
    (is (not (core/is-realm-file? "not-one")))
    (is (not (core/is-realm-file? "000000000000000009212102"))))
  (testing "realm files"
    (is (core/is-realm-file? "/some/new-account"))
    (is (core/is-realm-file? "/some/new-account.lock"))
    (is (core/is-realm-file? "/some/new-account.management"))
    (is (core/is-realm-file? "/some/new-account.note"))
    (is (core/is-realm-file? "/some/default.realm"))
    (is (core/is-realm-file? valid-account-path))))

(deftest realm-management-file-test
  (testing "not a management file"
    (is (not (core/realm-management-file? "new-account"))))
  (testing "management file"
    (is (core/realm-management-file? "anything.management"))
    (is (core/realm-management-file? "anything.lock"))
    (is (core/realm-management-file? "anything.note"))))

(deftest serialization
  (is (nil? (core/deserialize "")))
  (is (nil? (core/deserialize "giberrish")))
  (is (nil? (core/deserialize nil)))
  (is (nil? (core/deserialize (core/serialize nil)))))
