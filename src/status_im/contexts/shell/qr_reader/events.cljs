(ns status-im.contexts.shell.qr-reader.events
  (:require
    [status-im.contexts.shell.qr-reader.sheets.scanned-address :as scanned-address]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 :generic-scanner/scan-success
 (fn [_ [address]]
   {:fx [[:dispatch [:navigate-back]]
         [:dispatch [:show-bottom-sheet {:content #(scanned-address/view address)}]]]}))
