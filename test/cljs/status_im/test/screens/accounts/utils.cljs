(ns status-im.test.screens.accounts.utils
  (:require [clojure.test :refer-macros [deftest is]]
            [status-im.ui.screens.accounts.utils :as accounts.utils]))

(deftest test-account-update
  (is (= (accounts.utils/account-update {} nil)
         {:db                      {:account/account {}},
          :data-store/save-account {:after-update-event nil}})))

(deftest test-clean-seed-phrase
  (is (= (accounts.utils/clean-seed-phrase nil)
         {:db                      {:account/account {:seed-backed-up? true, :mnemonic nil}},
          :data-store/save-account {:seed-backed-up? true, :mnemonic nil, :after-update-event nil}})))
