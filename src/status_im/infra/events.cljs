(ns status-im.infra.events
  (:require
    [status-im.infra.transform :as transform]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn convert-saved-addresses
  [_ [raw-saved-addresses]]
  (let [saved-addresses (transform/rpc->saved-addresses raw-saved-addresses)]
    {:fx [[:dispatch [:domain/reconcile-saved-addresses saved-addresses]]]}))

(rf/reg-event-fx :infra/convert-saved-addresses convert-saved-addresses)

(defn get-saved-addresses
  [_]
  {:fx [[:json-rpc/call
         [{:method     "wakuext_getSavedAddresses"
           :on-success [:infra/convert-saved-addresses]
           :on-error   [:infra/saved-addresses-rpc-error :get-saved-addresses]}]]]})

(rf/reg-event-fx :infra/get-saved-addresses get-saved-addresses)

(defn saved-addresses-rpc-error
  [_ [action error]]
  (log/warn (str "[wallet] [saved-addresses] Failed to " action)
            {:error error}))

(rf/reg-event-fx :infra/saved-addresses-rpc-error saved-addresses-rpc-error)


