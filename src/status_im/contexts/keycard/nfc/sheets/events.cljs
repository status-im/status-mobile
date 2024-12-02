(ns status-im.contexts.keycard.nfc.sheets.events
  (:require [re-frame.core :as rf]
            [react-native.platform :as platform]))

(rf/reg-event-fx :keycard/show-connection-sheet
 (fn [{:keys [db]} [{:keys [on-cancel-event-vector]} :as args]]
   (if platform/android?
     {:db (assoc-in db
           [:keycard :connection-sheet-opts]
           {:on-close #(rf/dispatch on-cancel-event-vector)})
      :fx [[:effects.keycard.ios/start-nfc nil]
           [:dismiss-keyboard true]
           [:show-nfc-sheet nil]]}
     {:effects.keycard.ios/start-nfc
      {:on-success #(rf/dispatch [:keycard.ios/on-start-nfc-success args])}})))

(rf/reg-event-fx :keycard/hide-connection-sheet
 (fn [{:keys [db]}]
   (if platform/android?
     {:db (assoc-in db [:keycard :connection-sheet-opts] nil)
      :fx [[:effects.keycard.ios/stop-nfc nil]
           [:hide-nfc-sheet nil]]}
     {:effects.keycard.ios/stop-nfc nil})))
