(ns status-im.test.screens.accounts.utils
  (:require [clojure.test :refer-macros [deftest is]]
            [status-im.accounts.update.core :as accounts.update]))

(deftest test-account-update
  (is (= (accounts.update/account-update {} nil)
         {:db                      {:account/account {}},
          :data-store/save-account {}})))

(deftest test-clean-seed-phrase
  (is (= (accounts.update/clean-seed-phrase nil)
         {:db                      {:account/account {:seed-backed-up? true, :mnemonic nil}},
          :data-store/save-account {:seed-backed-up? true, :mnemonic nil}})))
