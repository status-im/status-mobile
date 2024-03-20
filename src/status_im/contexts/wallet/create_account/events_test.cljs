(ns status-im.contexts.wallet.create-account.events-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    matcher-combinators.test
    [status-im.contexts.wallet.create-account.events :as events]))

(deftest confirm-account-origin
  (let [db          {:wallet {:ui {:create-account {}}}}
        props       ["key-uid"]
        expected-db {:wallet {:ui {:create-account {:selected-keypair-uid "key-uid"}}}}
        effects     (events/confirm-account-origin {:db db} props)
        result-db   (:db effects)]
    (is (match? result-db expected-db))))

(deftest store-secret-phrase
  (let [db          {}
        props       [{:secret-phrase "test-secret" :random-phrase "random-test"}]
        expected-db {:wallet {:ui {:create-account {:secret-phrase "test-secret"
                                                    :random-phrase "random-test"}}}}
        effects     (events/store-secret-phrase {:db db} props)
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
  (let [db               {:wallet {:ui {:create-account {:secret-phrase "test-secret"}}}}
        props            [{:keypair-name "test-keypair"}]
        expected-effects [[:effects.wallet/create-account-from-mnemonic
                           {:secret-phrase "test-secret" :keypair-name "test-keypair"}]]
        effects          (events/new-keypair-continue {:db db} props)]
    (is (match? effects {:fx expected-effects}))))

(deftest clear-new-keypair
  (let [db          {:wallet {:ui {:create-account {:new-keypair "test-keypair"}}}}
        expected-db {:wallet {:ui {:create-account {}}}}
        effects     (events/clear-new-keypair {:db db})]
    (is (match? (:db effects) expected-db))))
