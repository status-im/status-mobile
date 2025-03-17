(ns status-im.contexts.keycard.nfc.events
  (:require [utils.re-frame :as rf]))

(rf/reg-event-fx :keycard.ios/start-nfc
 (fn [_]
   {:fx [[:effects.keycard.ios/start-nfc]]}))

(rf/reg-event-fx :keycard.ios/on-start-nfc-success
 (fn [{:keys [db]} [{:keys [on-cancel-event-vector]}]]
   {:db (assoc-in db [:keycard :on-nfc-cancelled-event-vector] on-cancel-event-vector)}))

(rf/reg-event-fx :keycard.ios/on-nfc-timeout
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:keycard :card-connected?] false)
    :fx [[:dispatch-later [{:ms 500 :dispatch [:keycard.ios/start-nfc]}]]]}))

(rf/reg-event-fx :keycard/on-check-nfc-enabled-success
 (fn [{:keys [db]} [nfc-enabled?]]
   {:db (assoc-in db [:keycard :nfc-enabled?] nfc-enabled?)}))

(rf/reg-event-fx :keycard.ios/on-nfc-user-cancelled
 (fn [{:keys [db]}]
   {:db (assoc-in db [:keycard :on-nfc-cancelled-event-vector] nil)
    :fx [(when-let [on-nfc-cancelled-event-vector (get-in db [:keycard :on-nfc-cancelled-event-vector])]
           [:dispatch on-nfc-cancelled-event-vector])]}))
