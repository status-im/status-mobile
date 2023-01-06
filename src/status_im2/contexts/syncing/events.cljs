(ns status-im2.contexts.syncing.events
  (:require [status-im.native-module.core :as status]
            [status-im2.contexts.syncing.sheets.enter-password.view :as sheet]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]
            [utils.security.core :as security]))

(rf/defn initiate-local-pairing-with-connection-string
  {:events [:syncing/input-connection-string-for-bootstrapping]}
  [{:keys [db]} {:keys [data]}]
  (let [config-map        (.stringify js/JSON (clj->js {:keyUID "" :keystorePath "" :password ""}))
        connection-string data]
    (status/input-connection-string-for-bootstrapping
     connection-string
     config-map
     #(log/info "this is response from initiate-local-pairing-with-connection-string " %)
     (fn [error-message]
       (log/debug "error while status/input-connection-string-for-bootstrapping" error-message))
    )))

(rf/defn preparations-for-connection-string
  {:events [:syncing/get-connection-string-for-bootstrapping-another-device]}
  [{:keys [db]} entered-password]
  (let [sha3-pwd   (status/sha3 (str (security/safe-unmask-data entered-password)))
        key-uid    (get-in db [:multiaccount :key-uid])
        config-map (.stringify js/JSON (clj->js {:keyUID key-uid :keystorePath "" :password sha3-pwd}))]
    (status/get-connection-string-for-bootstrapping-another-device
     config-map
     (fn [connection-string]
       (rf/dispatch
        [:bottom-sheet/show-sheet
         {:show-handle? false
          :content      (fn []
                          [sheet/qr-code-view-with-connection-string connection-string])}]))
     (fn [error-message]
       (log/debug "error with preparations-for-connection-string, " error-message)))))
