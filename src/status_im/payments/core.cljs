(ns status-im.payments.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.http :as http]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.waku.core :as waku]
            [taoensso.timbre :as log]
            [oops.core :refer [oget ocall]]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [status-im.utils.build :as build]
            ["react-native-iap" :as react-native-iap]))

(def payment-gateway "http://c60e4bc2.ngrok.io/events/")

(def rn-iap (oget react-native-iap "default"))
(def purchase-updated-listener (oget react-native-iap "purchaseUpdatedListener"))
(def purchase-error-listener (oget react-native-iap "purchaseErrorListener"))
(def finish-transaction (oget react-native-iap "finishTransaction"))
(def init-connection (oget rn-iap "initConnection"))
(def request-purchase (oget rn-iap "requestPurchase"))
(def get-products (oget rn-iap "getProducts"))

(defn get-product [sku on-success]
  (-> (get-products (clj->js [sku]))
      (.then (comp on-success js->clj))
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
   (-> (finish-transaction purchase false)
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
  (let [address  (ethereum/default-address db)
        message  {:invite_code nil
                  :cid         nil
                  :type        "purchase"
                  :address     address
                  :semver      build/version
                  :platform    platform/os
                  :receipt     purchase-data}
        msg      (types/clj->json message)]
    {::json-rpc/call [{:method     (json-rpc/call-ext-method (waku/enabled? cofx) "signMessageWithChatKey")
                       :params     [msg]
                       :on-error   #(re-frame/dispatch [::on-error "Sign message error"])
                       :on-success #(re-frame/dispatch [::call-payment-gateway
                                                        {:purchase    purchase-data
                                                         :chat-key    (get-in db [:multiaccount :public-key])
                                                         :message    msg
                                                         :on-success on-success} %])}]}))

(fx/defn call-payment-gateway
  {:events [::call-payment-gateway]}
  [cofx {:keys [purchase chat-key message on-success]} sig]
  (let [payload {:chat_key chat-key
                 :msg      message
                 :sig      sig
                 :version  2}]
    {:http-post {:url                   payment-gateway
                 :opts                  {:headers {"Content-Type" "application/json"}}
                 :data                  (types/clj->json payload)
                 :success-event-creator (fn [r] [::gateway-on-success purchase on-success r])
                 :failure-event-creator (fn [e] [::gateway-on-error e])}}))

(fx/defn gateway-success
  {:events [::gateway-on-success]}
  [cofx purchase on-success opts]
  (let [response (types/json->clj (get opts :response-body))]
    {::confirm-purchase purchase
     :dispatch          [on-success response]}))

(fx/defn gateway-error
  {:events [::gateway-on-error]}
  [cofx opts]
  (prn opts)
  (handle-error cofx "Backend error"))
