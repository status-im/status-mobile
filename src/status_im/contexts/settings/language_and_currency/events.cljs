(ns status-im.contexts.settings.language-and-currency.events
  (:require [status-im.contexts.settings.language-and-currency.data-store :as data-store]
            [utils.collection]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :settings/get-currencies-success
 (fn [{:keys [db]} [currencies]]
   (let [all-currencies (data-store/rpc->currencies currencies)]
     {:db (assoc db
                 :currencies
                 (utils.collection/index-by :id all-currencies))})))

(rf/reg-event-fx :settings/get-currencies
 (fn []
   {:fx [[:json-rpc/call
          [{:method     "appgeneral_getCurrencies"
            :on-success [:settings/get-currencies-success]
            :on-error   [:log-rpc-error {:event :settings/get-currencies}]}]]]}))
