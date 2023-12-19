(ns legacy.status-im.signing.eip1559
  (:require
    [re-frame.core :as re-frame]
    [status-im2.common.json-rpc.events :as json-rpc]))

(defonce london-activated? (atom false))

(defn london-is-definitely-activated
  [network-id]
  (contains? #{"1" "3"} network-id))

(defn on-block
  [callback header]
  (let [activated? (contains? header :baseFeePerGas)]
    (reset! london-activated? activated?)
    (callback activated?)))

(defn check-activation
  [callback]
  (json-rpc/call
   {:method     "eth_getBlockByNumber"
    :params     ["latest" false]
    :on-success (partial on-block callback)
    :on-error   #(callback nil)}))

(defn sync-enabled? [] @london-activated?)

(defn enabled?
  ([] @london-activated?)
  ([network-id enabled-callback disabled-callback]
   (let [definitely-activated? (london-is-definitely-activated network-id)
         enabled-callback      (or enabled-callback #())
         disabled-callback     (or disabled-callback #())]
     (cond
       definitely-activated?
       (do
         (reset! london-activated? true)
         (enabled-callback))

       (not definitely-activated?)
       (check-activation
        (fn [activated?]
          (if activated?
            (enabled-callback)
            (disabled-callback))))

       :else
       (do
         (reset! london-activated? false)
         (disabled-callback))))))

(re-frame/reg-fx
 :check-eip1559-activation
 (fn [network-id]
   (enabled? network-id nil nil)))
