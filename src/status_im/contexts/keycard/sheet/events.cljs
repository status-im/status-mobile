(ns status-im.contexts.keycard.sheet.events
  (:require [re-frame.core :as rf]
            [react-native.platform :as platform]
            [taoensso.timbre :as log]))

(rf/reg-event-fx :keycard/show-connection-sheet-component
 (fn [{:keys [db]} [{:keys [on-cancel-event-vector]}]]
   {:db (assoc-in db [:keycard :connection-sheet-opts] {:on-close #(rf/dispatch on-cancel-event-vector)})
    :fx [[:dismiss-keyboard true]
         [:show-nfc-sheet nil]]}))

(rf/reg-event-fx :keycard/show-connection-sheet
 (fn [_ [args]]
   (if platform/android?
     {:dispatch [:keycard/show-connection-sheet-component args]}
     {:effects.keycard.ios/start-nfc
      {:on-success
       (fn []
         (log/debug "nfc started successfully. next: show-connection-sheet")
         (rf/dispatch [:keycard.ios/on-start-nfc-success args]))
       :on-failure
       (fn []
         (log/debug "nfc failed star starting. not calling show-connection-sheet"))}})))

(rf/reg-event-fx :keycard/hide-connection-sheet
 (fn [{:keys [db]}]
   (if platform/android?
     {:db (assoc-in db [:keycard :connection-sheet-opts] nil)
      :fx [[:hide-nfc-sheet nil]]}
     {:effects.keycard.ios/stop-nfc nil})))
