(ns status-im.payments.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [oops.core :refer [oget ocall]]
            [status-im.signing.core :as signing]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [status-im.utils.build :as build]
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
   ;; NOTE: Consume all items so we can retest it many times
   (ocall js-dependencies/react-native-iap "consumeAllItemsAndroid")
   (-> (init-connection)
       (.then #(re-frame/dispatch [:set-in [:iap/payment :can-make-payment] %]))
       (.catch (fn [e]
                 (log/error "INIT PAYMENT ERROR:" (js->clj e)))))))

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
   ;; NOTE: Should call firstly get-products then show button with request-purchase
   (request-purchase sku false)))

(handlers/register-handler-fx
 ::request-payment
 (fn [_ [_ sku]]
   {::request-purchase sku}))

;; Gateway handling

(fx/defn handle-purchase
  {:events [::handle-purchase]}
  [{:keys [db] :as cofx} on-success purchase-data]
  (let [message  {:invite_code nil
                  :cid         nil
                  :type        "purchase"
                  :chat_key    (get-in db [:multiaccount :public-key])
                  :semver      build/version
                  :platform    platform/os
                  :receipt     purchase-data}
        address  (ethereum/default-address db)
        msg      (types/clj->json message)
        msg-hash (ethereum/sha3 msg)]
    (signing/sign cofx
                  {:message   {:address address
                               :data    msg-hash
                               :typed?  false}
                   :on-result [::call-payment-gateway
                               {:purchse    nil
                                :address    address
                                :message    msg
                                :on-success on-success}]})))

(fx/defn call-payment-gateway
  {:events [::call-payment-gateway]}
  [cofx {:keys [purchase address message on-success]} sig]
  (let [payload {:address address
                 :msg     message
                 :sig     sig
                 :version 2}]
    {:http-post {:url                   payment-gateway
                 :data                  (types/clj->json payload)
                 :response-validator    (fn [] false)
                 :success-event-creator (fn [r] [::gateway-on-success purchase on-success r])
                 :failure-event-creator (fn [e] [::gateway-on-error e])}}))

(fx/defn gateway-success
  {:events [::gateway-on-success]}
  [cofx purchase on-success]
  {::confirm-purchase purchase
   :dispatch          [on-success]})

(fx/defn gateway-error
  {:events [::gateway-on-error]}
  [cofx opts]
  (handle-error cofx "Backend error"))
