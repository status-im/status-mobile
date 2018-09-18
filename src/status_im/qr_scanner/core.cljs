(ns status-im.qr-scanner.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]))

(defn scan-qr-code
  [{:keys [modal?] :as identifier} qr-codes {:keys [db]}]
  {:db                     (assoc db :qr-codes qr-codes)
   :request-permissions-fx {:permissions [:camera]
                            :on-allowed  #(re-frame/dispatch [(if modal? :navigate-to-modal :navigate-to)
                                                              :qr-scanner {:current-qr-context identifier}])
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
         (when-let [qr-codes (:qr-codes db)]
           {:dispatch [(:handler qr-codes) context data (dissoc qr-codes :handler)]})))
