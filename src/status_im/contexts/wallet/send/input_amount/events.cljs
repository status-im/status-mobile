(ns status-im.contexts.wallet.send.input-amount.events
  (:require
    [status-im.contexts.wallet.send.input-amount.controlled-input-logic :as controlled-input-logic]
    [utils.re-frame :as rf]))

(def input-value-path [:wallet :ui :screens :input-amount-screen :input-value])
(def crypto-currency?-path [:wallet :ui :screens :input-amount-screen :crypto-currency?])

(rf/reg-event-fx :send-input-amount-screen/set-token-input-value
 (fn [{:keys [db]} [v]]
   {:db (assoc-in db input-value-path v)}))

(rf/reg-event-fx :send-input-amount-screen/swap-between-fiat-and-crypto
 (fn [{:keys [db]} [token-input-converted-value]]
   {:db (update-in db crypto-currency?-path not)
    :fx [[:dispatch
          [:send-input-amount-screen/set-token-input-value token-input-converted-value]]]}))

(rf/reg-event-fx :send-input-amount-screen/token-input-add-character
 (fn [{:keys [db]} [c max-decimals]]
   (let [input-value   (get-in db input-value-path)
         new-text      (str input-value c)
         regex-pattern (str "^\\d*\\.?\\d{0," max-decimals "}$")
         regex         (re-pattern regex-pattern)]
     (when (re-matches regex new-text)
       {:db (update-in db input-value-path #(controlled-input-logic/add-character % c))}))))

(rf/reg-event-fx :send-input-amount-screen/token-input-delete-last
 (fn [{:keys [db]}]
   {:db (update-in db input-value-path controlled-input-logic/delete-last)}))

(rf/reg-event-fx :send-input-amount-screen/token-input-delete-all
 (fn [{:keys [db]}]
   {:db (assoc-in db input-value-path "")}))


