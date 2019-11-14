(ns status-im.qr-scanner.core
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]
            [status-im.utils.fx :as fx]
            [status-im.utils.navigation :as navigation]))

(fx/defn scan-qr-code
  [{:keys [db]} {:keys [deny-handler] :as identifier} qr-codes]
  {:db                     (assoc-in db [:qr-codes identifier] qr-codes)
   :request-permissions-fx {:permissions [:camera]
                            :on-allowed  #(re-frame/dispatch
                                           [:navigate-to :qr-scanner
                                            {:current-qr-context identifier}])
                            :on-denied   (if (nil? deny-handler)
                                           (fn []
                                             (utils/set-timeout
                                              #(utils/show-popup (i18n/label :t/error)
                                                                 (i18n/label :t/camera-access-error))
                                              50))
                                           #(re-frame/dispatch [deny-handler qr-codes]))}})

(fx/defn set-qr-code
  [{:keys [db]} context data]
  (let [navigation-stack {:chat-stack :home}]
    (navigation/navigate-reset {:index   0
                                :key     :chat-stack
                                :actions [{:routeName :home}]})
    (merge {:db (-> db
                    (update :qr-codes dissoc context)
                    (dissoc :current-qr-context)
                    (update :navigation-stack navigation-stack))}
           (when-let [qr-codes (get-in db [:qr-codes context])]
             {:dispatch [(:handler qr-codes) context data (dissoc qr-codes :handler)]}))))

(fx/defn set-qr-code-cancel
  [{:keys [db]} context]
  (merge {:db (-> db
                  (update :qr-codes dissoc context)
                  (dissoc :current-qr-context))}
         (when-let [qr-codes (get-in db [:qr-codes context])]
           (when-let [handler (:cancel-handler qr-codes)]
             {:dispatch [handler context qr-codes]}))))
