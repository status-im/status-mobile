(ns status-im.contexts.wallet.wallet-connect.processing-events
  (:require [native-module.core :as native-module]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            [taoensso.timbre :as log]
            [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet-connect/process-session-request
 (fn [{:keys [db]} [event]]
   (let [method (wallet-connect-core/get-request-method event)
         screen (wallet-connect-core/method-to-screen method)]
     (if screen
       {:db (assoc-in db [:wallet-connect/current-request :event] event)
        :fx [(condp = method
               constants/wallet-connect-eth-send-transaction-method
               [:dispatch [:wallet-connect/process-eth-send-transaction]]

               constants/wallet-connect-eth-sign-method
               [:dispatch [:wallet-connect/process-eth-sign]]

               constants/wallet-connect-eth-sign-transaction-method
               [:dispatch [:wallet-connect/process-eth-sign-transaction]]

               constants/wallet-connect-eth-sign-typed-method
               [:dispatch [:wallet-connect/process-sign-typed]]

               constants/wallet-connect-eth-sign-typed-v4-method
               [:dispatch [:wallet-connect/process-sign-typed]]

               constants/wallet-connect-personal-sign-method
               [:dispatch [:wallet-connect/process-personal-sign]])

             [:dispatch [:open-modal screen]]]}
       (log/error "Didn't find screen for Wallet Connect method"
                  {:method method
                   :event  :wallet-connect/process-session-request})))))

(rf/reg-event-fx
 :wallet-connect/process-personal-sign
 (fn [{:keys [db]}]
   (let [[raw-data address] (wallet-connect-core/get-db-current-request-params db)
         parsed-data        (native-module/hex-to-utf8 raw-data)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      address
                     :raw-data     raw-data
                     :display-data (or parsed-data raw-data))})))

(rf/reg-event-fx
 :wallet-connect/process-eth-sign
 (fn [{:keys [db]}]
   (let [[address raw-data] (wallet-connect-core/get-db-current-request-params db)
         parsed-data        (native-module/hex-to-utf8 raw-data)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      address
                     :raw-data     raw-data
                     :display-data (or parsed-data raw-data))})))

(rf/reg-event-fx
 :wallet-connect/process-eth-send-transaction
 (fn [{:keys [db]}]
   (let [event        (wallet-connect-core/get-db-current-request-event db)
         display-data (-> event
                          clj->js
                          (js/JSON.stringify nil 2))
         {:keys [to]} (-> event wallet-connect-core/get-request-params first)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      to
                     :raw-data     event
                     :display-data display-data)})))

(rf/reg-event-fx
 :wallet-connect/process-eth-sign-transaction
 (fn [{:keys [db]}]
   (let [event        (wallet-connect-core/get-db-current-request-event db)
         display-data (.stringify js/JSON (clj->js event) nil 2)
         {:keys [to]} (-> event wallet-connect-core/get-request-params first)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      to
                     :raw-data     event
                     :display-data display-data)})))

(rf/reg-event-fx
 :wallet-connect/process-sign-typed
 (fn [{:keys [db]}]
   (let [[address raw-data] (wallet-connect-core/get-db-current-request-params db)
         parsed-data        (try (-> raw-data
                                     transforms/js-parse
                                     (transforms/js-dissoc :types :primaryType)
                                     (transforms/js-stringify 2))
                                 (catch js/Error _ nil))]
     ;; TODO: decide if we should proceed if the typed-data is invalid JSON or fail ahead of time
     (when (nil? parsed-data) (log/error "Invalid typed data"))
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      address
                     :raw-data     (or parsed-data raw-data)
                     :display-data parsed-data)})))
