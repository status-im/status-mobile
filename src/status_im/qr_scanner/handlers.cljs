(ns status-im.qr-scanner.handlers
  (:require [re-frame.core :refer [register-handler after dispatch debug enrich]]))

(defn set-current-identifier [db [_ identifier]]
  (assoc-in db [:qr-codes :identifier] identifier))

(register-handler :scan-qr-code
  (-> set-current-identifier
      ((after (fn [_ _] (dispatch [:navigate-to :qr-scanner]))))))

(register-handler :clear-qr-code [db [_ identifier]]
  (update-in db [:qr-codes] dissoc identifier))
