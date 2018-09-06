(ns status-im.qr-scanner.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]))

(defn scan-qr-code
  [identifier handler {:keys [db]}]
  {:db                     (assoc-in db [:qr-codes identifier] handler)
   :request-permissions-fx {:permissions [:camera]
                            :on-allowed  #(re-frame/dispatch [:navigate-to :qr-scanner {:current-qr-context identifier}])
                            :on-denied   (fn []
                                           (utils/set-timeout
                                            #(utils/show-popup (i18n/label :t/error)
                                                               (i18n/label :t/camera-access-error))
                                            50))}})

(defn set-qr-code
  [context data {:keys [db]}]
  (merge {:db (-> db
                  (update :qr-codes dissoc context)
                  (dissoc :current-qr-context))}
         (when-let [handler (get-in db [:qr-codes context])]
           {:dispatch [handler context data]})))
