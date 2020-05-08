(ns status-im.acquisition.gateway
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.waku.core :as waku]
            [status-im.utils.types :as types]))

(def acquisition-gateway "https://test-referral.status.im")

(def acquisition-routes {:clicks        (str acquisition-gateway "/clicks")
                         :registrations (str acquisition-gateway "/registrations")})

(defn get-url [type referral]
  (if (= type :clicks)
    (str (get acquisition-routes :clicks) "/" referral)
    (get acquisition-routes :registrations)))

(fx/defn handle-error
  {:events [::on-error]}
  [_ error]
  {:utils/show-popup {:title   "Request failed"
                      :content (str error)}})

(fx/defn handle-acquisition
  {:events [::handle-acquisition]}
  [{:keys [db] :as cofx} {:keys [message on-success method url]}]
  (let [msg (types/clj->json message)]
    {::json-rpc/call [{:method     (json-rpc/call-ext-method (waku/enabled? cofx) "signMessageWithChatKey")
                       :params     [msg]
                       :on-error   #(re-frame/dispatch [::on-error "Could not sign message"])
                       :on-success #(re-frame/dispatch [::call-acquisition-gateway
                                                        {:chat-key   (get-in db [:multiaccount :public-key])
                                                         :message    msg
                                                         :method     method
                                                         :url        url
                                                         :on-success on-success} %])}]}))
(fx/defn call-acquisition-gateway
  {:events [::call-acquisition-gateway]}
  [cofx
   {:keys [chat-key message on-success type url method] :as kek}
   sig]
  (let [payload {:chat_key chat-key
                 :msg      message
                 :sig      sig
                 :version  2}]
    {:http-post {:url                   url
                 :opts                  {:headers {"Content-Type" "application/json"}
                                         :method  method}
                 :data                  (types/clj->json payload)
                 :success-event-creator (fn [response]
                                          [on-success (types/json->clj (get response :response-body))])
                 :failure-event-creator (fn [error]
                                          [::on-error (:error (types/json->clj (get error :response-body)))])}}))

(fx/defn get-referrer
  [cofx referrer on-success handled-error handle-error]
  {:http-get {:url                   (get-url :clicks referrer)
              :success-event-creator (fn [response]
                                       (on-success (types/json->clj response)))
              :failure-event-creator (fn [response]
                                       (let [error (types/json->clj response)]
                                         (if (handled-error error)
                                           (handle-error error)
                                           [::on-error (:error error)])))}})
