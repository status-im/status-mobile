(ns status-im.contexts.keycard.events
  (:require [re-frame.core :as rf]
            status-im.contexts.keycard.login.events
            status-im.contexts.keycard.pin.events
            status-im.contexts.keycard.sheet.events
            status-im.contexts.keycard.sign.events
            [status-im.contexts.keycard.utils :as keycard.utils]
            [taoensso.timbre :as log]))

(rf/reg-event-fx :keycard/on-check-nfc-enabled-success
 (fn [{:keys [db]} [nfc-enabled?]]
   {:db (assoc-in db [:keycard :nfc-enabled?] nfc-enabled?)}))

(rf/reg-event-fx :keycard.ios/on-nfc-user-cancelled
 (fn [{:keys [db]}]
   (log/debug "[keycard] nfc user cancelled")
   {:db (assoc-in db [:keycard :pin :status] nil)
    :fx [(when-let [on-nfc-cancelled-event-vector (get-in db [:keycard :on-nfc-cancelled-event-vector])]
           [:dispatch on-nfc-cancelled-event-vector])]}))

(rf/reg-event-fx :keycard/on-card-connected
 (fn [{:keys [db]} _]
   (log/debug "[keycard] card globally connected")
   {:db (assoc-in db [:keycard :card-connected?] true)
    :fx [(when-let [event (get-in db [:keycard :on-card-connected-event-vector])]
           [:dispatch event])]}))

(rf/reg-event-fx :keycard/on-card-disconnected
 (fn [{:keys [db]} _]
   (log/debug "[keycard] card disconnected")
   {:db (assoc-in db [:keycard :card-connected?] false)
    :fx [(when-let [event (get-in db [:keycard :on-card-disconnected-event-vector])]
           [:dispatch event])]}))

(rf/reg-event-fx :keycard.ios/start-nfc
 (fn [_]
   {:effects.keycard.ios/start-nfc nil}))

(rf/reg-event-fx :keycard.ios/on-nfc-timeout
 (fn [{:keys [db]} _]
   (log/debug "[keycard] nfc timeout")
   {:db (assoc-in db [:keycard :card-connected?] false)
    :fx [[:dispatch-later [{:ms 500 :dispatch [:keycard.ios/start-nfc]}]]]}))

(rf/reg-event-fx :keycard/get-application-info
 (fn [_ [{:keys [on-success on-failure]}]]
   (log/debug "[keycard] get-application-info")
   {:effects.keycard/get-application-info {:on-success on-success
                                           :on-failure on-failure}}))

(rf/reg-event-fx :keycard/on-retrieve-pairings-success
 (fn [{:keys [db]} [pairings]]
   {:db (assoc-in db [:keycard :pairings] pairings)
    :fx [[:effects.keycard/set-pairing-to-keycard pairings]]}))

(rf/reg-event-fx :keycard.ios/on-start-nfc-success
 (fn [{:keys [db]} [{:keys [on-cancel-event-vector]}]]
   (log/debug "[keycard] nfc started success")
   {:db (assoc-in db [:keycard :on-nfc-cancelled-event-vector] on-cancel-event-vector)}))

(rf/reg-event-fx :keycard/on-action-with-pin-error
 (fn [{:keys [db]} [error]]
   (log/debug "[keycard] get keys error: " error)
   (let [tag-was-lost?     (keycard.utils/tag-lost? (:error error))
         pin-retries-count (keycard.utils/pin-retries (:error error))]
     (if tag-was-lost?
       {:db (assoc-in db [:keycard :pin :status] nil)}
       (if (nil? pin-retries-count)
         {:effects.utils/show-popup {:title "wrong-keycard"}}
         {:db (-> db
                  (assoc-in [:keycard :application-info :pin-retry-counter] pin-retries-count)
                  (update-in [:keycard :pin] assoc :status :error))
          :fx [[:dispatch [:keycard/hide-connection-sheet]]
               (when (zero? pin-retries-count)
                 [:effects.utils/show-popup {:title "frozen-keycard"}])]})))))
