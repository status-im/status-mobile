(ns status-im.payments.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [oops.core :refer [oget ocall]]
            ["react-native-iap" :as react-native-iap]))

(def payment-gateway "")

(def rn-iap (oget react-native-iap "default"))
(def purchase-updated-listener (oget react-native-iap "purchaseUpdatedListener"))
(def purchase-error-listener (oget react-native-iap "purchaseErrorListener"))
(def finish-transaction (oget react-native-iap "finishTransaction"))
(def init-connection (oget rn-iap "initConnection"))
(def request-purchase (oget rn-iap "requestPurchase"))
(def get-products (oget rn-iap "getProducts"))

(defn get-product [sku on-success]
  (-> (get-products (clj->js [sku]))
      (.then on-success)
      (.catch #(log/warn "GET PRODUCTS ERROR:" (js->clj %)))))

(defn clear-purchase-listeners [active-listeners]
  (doseq [listener active-listeners]
    (.remove listener)))

(defn purchase-listeners [on-success]
  [(purchase-updated-listener #(re-frame/dispatch [::handle-purchase on-success %]))
   (purchase-error-listener (fn [e]
                              (re-frame/dispatch [::on-error (oget e "message")])
                              (log/warn "PAYMENT ERROR:" (js->clj e))))])

(fx/defn handle-error
  {:events [::on-error]}
  [_ message]
  {:utils/show-popup {:title   "Purchase failed"
                      :content message}})

(re-frame/reg-fx
 ::init-connection
 (fn []
   (-> (init-connection)
       (.then #(re-frame/dispatch [:set-in [:iap/payment :can-make-payment] %]))
       (.catch (fn [e]
                 (re-frame/dispatch [::on-error (oget e "message")])
                 (log/error "PAYMENT ERROR:" (js->clj e)))))))

(re-frame/reg-sub
 ::can-make-payment
 (fn [db]
   (get-in db [:iap/payment :can-make-payment])))

(re-frame/reg-fx
 ::confirm-purchase
 (fn [purchase]
   (-> (finish-transaction purchase)
       (.catch (fn [e]
                 (re-frame/dispatch [::on-error (oget e "message")])
                 (log/warn "FINISH PAYMENT ERROR:" (js->clj e)))))))

(re-frame/reg-fx
 ::request-purchase
 (fn [sku]
   (let [start-purchase (fn []
                          (-> (request-purchase sku  false)
                              (.catch (fn [e]
                                        (re-frame/dispatch [::on-error (oget e "message")])
                                        (log/warn "REQUEST PAYMENT ERROR:" (js->clj e))))))]
     (get-product sku start-purchase))))

(handlers/register-handler-fx
 ::request-payment
 (fn [_ [_ sku]]
   {::request-purchase sku}))

;; Gateway handling

(handlers/register-handler-fx
 ::handle-purchase
 (fn [_ [_ on-success purchase-data]]
   ;; TODO: add wallet address to request
   {::call-payment-gateway {:purchase   purchase-data
                            :on-success on-success}}))

(re-frame/reg-fx
 ::call-payment-gateway
 (fn [_ [_ {:keys [purchase-data on-success]}]]
   (prn purchase-data)))

(fx/defn gateway-success
  {:events [::gateway-on-success]}
  [cofx opts])

(fx/defn gateway-error
  {:events [::gateway-on-error]}
  [cofx opts]
  (handle-error cofx "Backend error"))
