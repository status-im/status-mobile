(ns status-im.test.multiaccounts.update.core
  (:require [clojure.test :refer-macros [deftest is]]
            [status-im.multiaccounts.update.core :as multiaccounts.update]))

(deftest test-multiaccount-update
  (is (= (multiaccounts.update/multiaccount-update {} nil)
         {:db                      {:multiaccount {}},
          :data-store/save-multiaccount {}})))

(deftest test-clean-seed-phrase
  (is (= (multiaccounts.update/clean-seed-phrase nil)
         {:db                      {:multiaccount {:seed-backed-up? true, :mnemonic nil}},
          :data-store/save-multiaccount {:seed-backed-up? true, :mnemonic nil}})))
