(ns status-im.gateway.events
  (:require
    [utils.re-frame :as rf]))

;; Log the error message and call the error handler event, if one exists.
(rf/reg-event-fx :gate.rpc/log-error
 (fn [_ [{:keys [method params error-handler-event]} error]]
   {:fx [[:effects.log/warn
          [(str "Failed to  call rpc: " method)
           {:method method
            :params params
            :error  error}]]
         (when error-handler-event
           [:dispatch [error-handler-event error]])]}))

(rf/reg-event-fx :gate.rpc/call
 (fn [_ [{:keys [method params error-handler-event]}]]
   ;; TODO(volodymyr.kozieiev): :json-rpc/call should be moved to gateway layer
   {:json-rpc/call [{:method   method
                     :params   params
                     :on-error [:gate.rpc/log-error
                                {:method              method
                                 :params              params
                                 :error-handler-event error-handler-event}]}]}))

