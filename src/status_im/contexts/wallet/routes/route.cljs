(ns status-im.contexts.wallet.routes.route)

(defn get-best-suggested-route
  [routes]
  (-> routes :Best first))

(defn error-response
  [routes]
  (:ErrorResponse routes))

(defn routes-uuid
  [routes]
  (get routes :Uuid))

(defn sent-transactions-map
  [sent-transactions]
  (reduce
   (fn [acc transaction]
     (let [tx-hash (:hash transaction)]
       (assoc acc
              tx-hash
              {:status      :pending
               :transaction transaction})))
   {}
   sent-transactions))
