(ns status-im.contexts.wallet.add-account.create-account.events-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    matcher-combinators.test
    [status-im.contexts.wallet.add-account.create-account.events :as events]))

(deftest confirm-account-origin
  (let [db          {:wallet {:ui {:create-account {}}}}
        props       ["key-uid"]
        expected-db {:wallet {:ui {:create-account {:selected-keypair-uid "key-uid"}}}}
        effects     (events/confirm-account-origin {:db db} props)
        result-db   (:db effects)]
    (is (match? result-db expected-db))))

(deftest store-seed-phrase
  (let [db          {}
        props       [{:seed-phrase "test-secret" :random-phrase "random-test"}]
        expected-db {:wallet {:ui {:create-account {:seed-phrase   "test-secret"
                                                    :random-phrase "random-test"}}}}
        effects     (events/store-seed-phrase {:db db} props)
        result-db   (:db effects)]
    (is (match? result-db expected-db))))

(deftest new-keypair-created
  (let [db          {}
        props       [{:new-keypair "test-keypair"}]
        expected-db {:wallet {:ui {:create-account {:new-keypair "test-keypair"}}}}
        effects     (events/new-keypair-created {:db db} props)
        result-db   (:db effects)]
    (is (match? result-db expected-db))))

(deftest new-keypair-continue
  (let [db               {:wallet {:ui {:create-account {:seed-phrase "test-secret"}}}}
        props            [{:keypair-name "test-keypair"}]
        expected-effects [[:effects.wallet/create-account-from-mnemonic
                           {:seed-phrase "test-secret" :keypair-name "test-keypair"}]]
        effects          (events/new-keypair-continue {:db db} props)]
    (is (match? effects {:fx expected-effects}))))

(deftest clear-new-keypair
  (let [db          {:wallet {:ui {:create-account {:new-keypair "test-keypair"}}}}
        expected-db {:wallet {:ui {:create-account {}}}}
        effects     (events/clear-new-keypair {:db db})]
    (is (match? (:db effects) expected-db))))

(deftest get-derived-addresses-test
  (let [db           {}
        password     "test-password"
        derived-from "test-derive-from"
        paths        ["path1"]
        event-args   [{:password password :derived-from derived-from :paths paths}]
        expected-db  (assoc-in db [:wallet :ui :create-account :derivation-path-state] :scanning)
        effects      (events/get-derived-addresses {:db db} event-args)
        result-db    (:db effects)]
    (is (match? result-db expected-db))))
