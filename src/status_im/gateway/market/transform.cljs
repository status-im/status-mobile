(ns status-im.gateway.market.transform
  (:require
    [oops.core :as oops]
    [utils.transforms :as transforms]))

(defn wallet-signal-message->clj
  "Wallet signal comes as js object and has .-message field that is json-encoded.
  This function extracts message field and converts it from json to clj"
  [signal-payload]
  (-> (oops/oget signal-payload "message")
      (transforms/json->clj {:keywords-to-kebab-case? true})))

(defn leaderboard-message->positions-by-token
  "Extract token ids and index them by position in a leaderboard"
  [{:keys [page page-size cryptocurrencies] :as _leaderboard-message}]
  (let [start-from (-> page
                       dec
                       (* page-size)
                       inc)]
    (reduce (fn [acc cryptocurrency]
              (let [position (+ start-from (count acc))]
                (assoc acc (:id cryptocurrency) position)))
            {}
            cryptocurrencies)))

(defn leaderboard-message->prices-by-token-id
  "Extract price-related data from leaderboard message and index it by token id"
  [{:keys [cryptocurrencies]}]
  (reduce
   (fn [acc token-data]
     (assoc acc
            (:id token-data)
            (select-keys token-data
                         [:id
                          :current-price
                          :market-cap
                          :total-volume
                          :price-change-percentage-24h])))
   {}
   cryptocurrencies))

(defn price-update-message->price-updates-by-token-id
  "Extract prices and index them by token id"
  [{:keys [prices]}]
  (reduce
   (fn [acc {:keys [id] :as price}]
     (assoc acc id price))
   {}
   prices))

(defn leaderboard-message->token-data-by-token-id
  "Extract token information from leaderboard message and index it by token id"
  [{:keys [cryptocurrencies]}]
  (reduce
   (fn [acc token-data]
     (assoc acc
            (:id token-data)
            (select-keys token-data
                         [:id
                          :symbol
                          :name
                          :image])))
   {}
   cryptocurrencies))
