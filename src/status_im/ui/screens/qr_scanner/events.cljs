(ns status-im.ui.screens.qr-scanner.events
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.camera :as camera]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(handlers/register-handler-fx
  :scan-qr-code
  (fn [{:keys [db]} [_ identifier handler]]
    {:db (assoc-in db [:qr-codes identifier] handler)
     :request-permissions-fx {:permissions [:camera]
                              :on-allowed  #(re-frame/dispatch [:navigate-to :qr-scanner {:current-qr-context identifier}])
                              :on-denied   #(utils/show-popup (i18n/label :t/error)
                                                              (i18n/label :t/camera-access-error))}}))

(handlers/register-handler-fx
  :clear-qr-code
  (fn [{:keys [db]} [_ identifier]]
    {:db (update db :qr-codes dissoc identifier)}))

(handlers/register-handler-fx
  :set-qr-code
  (fn [{:keys [db]} [_ context data]]
    (merge {:db (-> db
                    (update :qr-codes dissoc context)
                    (dissoc :current-qr-context))}
           (when-let [handler (get-in db [:qr-codes context])]
             {:dispatch [handler context data]}))))
