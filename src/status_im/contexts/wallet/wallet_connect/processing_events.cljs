(ns status-im.contexts.wallet.wallet-connect.processing-events
  (:require [native-module.core :as native-module]
            [re-frame.core :as rf]
            [status-im.constants :as constants]
            [status-im.contexts.wallet.wallet-connect.core :as wallet-connect-core]
            [utils.transforms :as transforms]))

(rf/reg-event-fx
 :wallet-connect/process-session-request
 (fn [{:keys [db]} [event]]
   (let [method (wallet-connect-core/event->method event)
         screen (wallet-connect-core/method->screen method)]
     (when screen
       {:db (assoc-in db [:wallet-connect/current-request :event] event)
        :fx [(condp = method
               constants/wallet-connect-personal-sign-method
               [:dispatch [:wallet-connect/process-personal-sign]]

               constants/wallet-connect-eth-sign-method
               [:dispatch [:wallet-connect/process-eth-sign]]

               constants/wallet-connect-eth-sign-typed-method
               [:dispatch [:wallet-connect/process-sign-typed]]

               constants/wallet-connect-eth-sign-typed-v4-method
               [:dispatch [:wallet-connect/process-sign-typed]])
             [:dispatch [:open-modal screen]]]}))))

(rf/reg-event-fx
 :wallet-connect/process-personal-sign
 (fn [{:keys [db]}]
   (let [event          (get-in db [:wallet-connect/current-request :event])
         request-params (get-in event [:params :request :params])
         address        (second request-params)
         raw-data       (first request-params)
         parsed-data    (native-module/hex-to-utf8 raw-data)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      address
                     :raw-data     raw-data
                     :display-data (or parsed-data raw-data))})))

(rf/reg-event-fx
 :wallet-connect/process-eth-sign
 (fn [{:keys [db]}]
   (let [event          (get-in db [:wallet-connect/current-request :event])
         request-params (get-in event [:params :request :params])
         address        (first request-params)
         raw-data       (second request-params)
         parsed-data    (native-module/hex-to-utf8 raw-data)]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      address
                     :raw-data     raw-data
                     :display-data (or parsed-data raw-data))})))

(rf/reg-event-fx
 :wallet-connect/process-sign-typed
 (fn [{:keys [db]}]
   (let [event          (get-in db [:wallet-connect/current-request :event])
         request-params (get-in event [:params :request :params])
         address        (first request-params)
         raw-data       (second request-params)
         parsed-data    (-> raw-data
                            transforms/json->clj
                            (dissoc :types :primaryType)
                            (transforms/clj->pretty-json 2))]
     {:db (update-in db
                     [:wallet-connect/current-request]
                     assoc
                     :address      address
                     :raw-data     raw-data
                     :display-data parsed-data)})))
