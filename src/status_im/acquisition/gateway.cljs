(ns status-im.acquisition.gateway
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.i18n :as i18n]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.waku.core :as waku]
            [status-im.utils.types :as types]))

(def acquisition-gateway "https://test-referral.status.im")

(def acquisition-routes {:clicks        (str acquisition-gateway "/clicks")
                         :registrations (str acquisition-gateway "/registrations")})

(def network-statuses {:initiated 1
                       :in-flight 2
                       :error     3
                       :success   4})

(defn get-url [type referral]
  (if (= type :clicks)
    (str (get acquisition-routes :clicks) "/" referral)
    (get acquisition-routes :registrations)))

(fx/defn handle-error
  {:events [::on-error]}
  [{:keys [db]} error]
  {:db               (assoc-in db [:acquisition :network-status]
                               (get network-statuses :error))
   :utils/show-popup {:title   (i18n/label :t/http-gateway-error)
                      :content (str error)}})

(fx/defn handle-acquisition
  {:events [::handle-acquisition]}
  [{:keys [db] :as cofx} {:keys [message on-success method url]}]
  (let [msg (types/clj->json message)]
    {:db             (assoc-in db [:acquisition :network-status]
                               (get network-statuses :initiated))
     ::json-rpc/call [{:method     (json-rpc/call-ext-method (waku/enabled? cofx) "signMessageWithChatKey")
                       :params     [msg]
                       :on-error   #(re-frame/dispatch [::on-error (i18n/label :t/sign-request-failed)])
                       :on-success #(re-frame/dispatch [::call-acquisition-gateway
                                                        {:chat-key   (get-in db [:multiaccount :public-key])
                                                         :message    msg
                                                         :method     method
                                                         :url        url
                                                         :on-success on-success} %])}]}))
(fx/defn call-acquisition-gateway
  {:events [::call-acquisition-gateway]}
  [{:keys [db]}
   {:keys [chat-key message on-success type url method]}
   sig]
  (let [payload {:chat_key chat-key
                 :msg      message
                 :sig      sig
                 :version  2}]
    {:db        (assoc-in db [:acquisition :network-status]
                          (get network-statuses :in-flight))
     :http-post {:url        url
                 :opts       {:headers {"Content-Type" "application/json"}
                              :method  method}
                 :data       (types/clj->json payload)
                 :on-success (fn [response]
                               (re-frame/dispatch [:set-in [:acquisition :network-status]]
                                                  (get network-statuses :success))
                               (re-frame/dispatch (conj on-success (types/json->clj (get response :response-body)))))
                 :on-error   (fn [error]
                               (re-frame/dispatch [::on-error (:error (types/json->clj (get error :response-body)))]))}}))

(fx/defn get-referrer
  [cofx referrer on-success handled-error handle-error]
  {:http-get {:url        (get-url :clicks referrer)
              :on-success (fn [response]
                            (re-frame/dispatch (on-success (types/json->clj response))))
              :on-error   (fn [response]
                            (let [error (types/json->clj response)]
                              (if (handled-error error)
                                (handle-error error)
                                (re-frame/dispatch [::on-error (:error error)]))))}})
(re-frame/reg-sub
 ::network-status
 (fn [db]
   (get-in db [:acquisition :network-status])))
