(ns status-im.qr-scanner.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]
            [status-im.utils.fx :as fx]))

(fx/defn scan-qr-code
  [{:keys [db]} {:keys [modal? deny-handler] :as identifier} qr-codes]
  {:db                     (assoc db :qr-codes qr-codes)
   :request-permissions-fx {:permissions [:camera]
                            :on-allowed  #(re-frame/dispatch [(if modal? :navigate-to-modal :navigate-to-clean)
                                                              :qr-scanner {:current-qr-context identifier}])
                            :on-denied   (if (nil? deny-handler)
                                           (fn []
                                             (utils/set-timeout
                                              #(utils/show-popup (i18n/label :t/error)
                                                                 (i18n/label :t/camera-access-error))
                                              50))
                                           #(re-frame/dispatch [deny-handler qr-codes]))}})

(fx/defn set-qr-code
  [{:keys [db]} context data]
  (merge {:db (-> db
                  (update :qr-codes dissoc context)
                  (dissoc :current-qr-context))}
         (when-let [qr-codes (:qr-codes db)]
           {:dispatch [(:handler qr-codes) context data (dissoc qr-codes :handler)]})))

(fx/defn set-qr-code-cancel
  [{:keys [db]} context]
  (merge {:db (-> db
                  (update :qr-codes dissoc context)
                  (dissoc :current-qr-context))}
         (when-let [qr-codes (:qr-codes db)]
           (when-let [handler (:cancel-handler qr-codes)]
             {:dispatch [handler context qr-codes]}))))