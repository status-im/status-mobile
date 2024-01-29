(ns status-im.integration-test.events
  (:require
    [utils.number]
    [utils.re-frame :as rf]))

(rf/reg-event-fx
 [:integration-test/rpc-checked]
 (fn []))

(rf/reg-event-fx
 :integration-test/dispatch-rpc-success
 (fn [_ [response check-result]]
   (check-result response)
   (rf/dispatch
    [:integration-test/rpc-checked])))

(rf/reg-event-fx
 :integration-test/dispatch-rpc
 (fn [_
      [{:keys [rpc-endpoint
               params
               check-result]}]]
   {:fx [[:json-rpc/call
          [{:method     rpc-endpoint
            :params     params
            :on-success #(rf/dispatch [:integration-test/dispatch-rpc-success % check-result])
            :on-error   #(prn {:title (str "failed test " rpc-endpoint)
                               :error %})}]]]}))
