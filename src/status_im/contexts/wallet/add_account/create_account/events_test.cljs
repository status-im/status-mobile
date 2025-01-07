(ns status-im.contexts.wallet.add-account.create-account.events-test
  (:require
    [cljs.test :refer-macros [deftest is]]
    [matcher-combinators.test]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.add-account.create-account.events :as events]
    [utils.security.core :as security]))

(deftest confirm-account-origin-test
  (let [db          {:wallet {:ui {:create-account {}}}}
        props       ["key-uid"]
        expected-db {:wallet {:ui {:create-account {:selected-keypair-uid "key-uid"}}}}
        effects     (events/confirm-account-origin {:db db} props)
        result-db   (:db effects)]
    (is (match? result-db expected-db))))

(deftest store-account-generated-test
  (let [db              {:wallet {:ui {:create-account
                                       {:new-keypair {:seed-phrase   "test-secret"
                                                      :random-phrase "random-test"}}}}}
        mnemonic        "my mnemonic"
        masked-mnemonic (security/mask-data mnemonic)
        props           [{:new-account-data {"test"    "data"
                                             :mnemonic mnemonic}
                          :keypair-name     "new-keypair-name"}]
        expected-db     {:wallet {:ui {:create-account
                                       {:new-keypair
                                        {:new-account-data {"test"    "data"
                                                            :mnemonic masked-mnemonic}
                                         :keypair-name     "new-keypair-name"}}}}}
        effects         (events/store-account-generated-with-mnemonic {:db db} props)
        result-db       (:db effects)
        remove-mnemonic #(update-in %
                                    [:wallet :ui :create-account :new-keypair :new-account-data]
                                    dissoc
                                    :mnemonic)
        unmask-mnemonic #(-> %
                             :wallet
                             :ui
                             :create-account
                             :new-keypair
                             :new-account-data
                             :mnemonic
                             security/safe-unmask-data)]
    (is (= (remove-mnemonic result-db) (remove-mnemonic expected-db)))
    (is (= (unmask-mnemonic result-db) (unmask-mnemonic expected-db)))))


(deftest generate-account-for-keypair-test
  (let [db               {:wallet {:ui {:create-account {:new-keypair {:seed-phrase "test-secret"}}}}}
        props            [{:keypair-name "test-keypair"}]
        expected-effects [[:effects.wallet/create-account-from-mnemonic
                           {:mnemonic-phrase "test-secret"
                            :paths           [constants/path-default-wallet]}]]
        effects          (events/generate-account-for-keypair-with-mnemonic {:db db} props)]
    (is (match?
         (update-in effects [:fx 0 1] dissoc :on-success)
         {:fx expected-effects}))
    (is (some? (get-in effects [:fx 0 1 :on-success])))))

(deftest clear-create-account-data-test
  (let [db          {:wallet {:ui {:create-account {:new-keypair "test-keypair"}}}}
        expected-db {:wallet {:ui {:create-account {}}}}
        effects     (events/clear-create-account-data {:db db})]
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
