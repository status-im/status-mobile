(ns status-im.qr-scanner.handlers
  (:require [re-frame.core :refer [register-handler after dispatch debug enrich]]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :as u]))

(defmethod nav/preload-data! :qr-scanner
  [db [_ _ identifier]]
  (assoc db :current-qr-context identifier))

(defn set-current-identifier [db [_ identifier handler]]
  (assoc-in db [:qr-codes identifier] handler))

(defn navigate-to-scanner
  [_ [_ identifier]]
  (dispatch [:navigate-to :qr-scanner identifier]))

(register-handler :scan-qr-code
  (after navigate-to-scanner)
  set-current-identifier)

(register-handler :clear-qr-code
  (fn [db [_ identifier]]
    (update db :qr-codes dissoc identifier)))

(defn handle-qr-request
  [db [_ context data]]
  (let [handler (get-in db [:qr-codes context])]
    (println handler context data)
    (dispatch [handler context data])))

(defn clear-qr-request [db [_ context]]
  (-> db
      (update :qr-codes dissoc context)
      (dissoc :current-qr-context)))

(register-handler :set-qr-code
  (-> (u/side-effect! handle-qr-request)
      ((enrich clear-qr-request))
      ((after #(dispatch [:navigate-back])))))
