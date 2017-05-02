(ns status-im.qr-scanner.handlers
  (:require [re-frame.core :refer [after dispatch debug enrich]]
            [status-im.components.camera :as camera]
            [status-im.navigation.handlers :as nav]
            [status-im.utils.handlers :as u :refer [register-handler]]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(defmethod nav/preload-data! :qr-scanner
  [db [_ _ identifier]]
  (assoc db :current-qr-context identifier))

(defn set-current-identifier [db [_ identifier handler]]
  (assoc-in db [:qr-codes identifier] handler))

(defn navigate-to-scanner
  [_ [_ identifier]]
  (dispatch [:request-permissions
             [:camera]
             (fn []
               (camera/request-access
                 #(if % (dispatch [:navigate-to :qr-scanner identifier])
                        (utils/show-popup (i18n/label :t/error)
                                          (i18n/label :t/camera-access-error)))))]))

(register-handler :scan-qr-code
  (after navigate-to-scanner)
  set-current-identifier)

(register-handler :clear-qr-code
  (fn [db [_ identifier]]
    (update db :qr-codes dissoc identifier)))

(defn handle-qr-request
  [db [_ context data]]
  (when-let [handler (get-in db [:qr-codes context])]
    (dispatch [handler context data])))

(defn clear-qr-request [db [_ context]]
  (-> db
      (update :qr-codes dissoc context)
      (dissoc :current-qr-context)))

(register-handler :set-qr-code
  (-> (u/side-effect! handle-qr-request)
      ((enrich clear-qr-request))
      ((after (fn [{:keys [view-id]}]
                (when (= :qr-scanner view-id)
                  (dispatch [:navigate-back])))))))
