(ns status-im.test.wallet.events
  (:require [cljs.test :refer [deftest is testing]]
            reagent.core
            [re-frame.core :as re-frame]
            [day8.re-frame.test :refer [run-test-sync]]
            status-im.ui.screens.db
            status-im.ui.screens.subs
            [status-im.ui.screens.events :as events]
            [status-im.ui.screens.wallet.events :as wallet-events]))

(deftest wallet-events
  "update-balance-fail
   update-prices-fail
   clear-error"
  (run-test-sync
   (re-frame/reg-fx ::events/init-store #())
   (re-frame/dispatch [:initialize-db])
   (let [error (re-frame/subscribe [:wallet/error-message])
         message :error]
     (re-frame/dispatch [:update-balance-fail message])
     (is (= message @error)))
   (let [error (re-frame/subscribe [:wallet/error-message])
         message :error]
     (re-frame/dispatch [:update-prices-fail message])
     (is (= message @error)))
   (let [error (re-frame/subscribe [:wallet/error-message])]
     (re-frame/dispatch [:wallet/clear-error-message])
     (is (nil? @error)))))
