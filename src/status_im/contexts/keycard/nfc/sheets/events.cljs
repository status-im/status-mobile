(ns status-im.contexts.keycard.nfc.sheets.events
  (:require [re-frame.core :as rf]
            [react-native.platform :as platform]))

(rf/reg-event-fx :keycard/show-connection-sheet
 (fn [{:keys [db]} [{:keys [on-cancel-event-vector theme]} :as args]]
   (if platform/android?
     {:db (assoc-in db
           [:keycard :connection-sheet-opts]
           {:on-close #(rf/dispatch on-cancel-event-vector)
            :theme    theme})
      :fx [[:effects.keycard/start-nfc nil]
           [:dismiss-keyboard true]
           [:show-nfc-sheet nil]]}
     {:effects.keycard/start-nfc
      {:on-success #(rf/dispatch [:keycard.ios/on-start-nfc-success args])}})))

(rf/reg-event-fx :keycard/hide-connection-sheet
 (fn [{:keys [db]} [{:keys [success?]}]]
   (if platform/android?
     {:db (-> db
              (assoc-in [:keycard :connection-sheet-opts :hide?] true)
              (assoc-in [:keycard :connection-sheet-opts :success?] success?))
      :fx [[:effects.keycard/stop-nfc nil]]}
     {:effects.keycard/stop-nfc nil})))

(rf/reg-event-fx :keycard/connection-sheet-hidden
 (fn [{:keys [db]}]
   {:db             (assoc-in db [:keycard :connection-sheet-opts] nil)
    :hide-nfc-sheet nil}))
