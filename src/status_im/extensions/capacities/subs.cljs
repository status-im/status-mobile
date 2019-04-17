(ns status-im.extensions.capacities.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.money :as money]))

(re-frame/reg-sub
 :extensions/identity
 (fn [_ [_ _ {:keys [value]}]]
   value))

(defn get-token-for [network all-tokens token]
  (if (= token "ETH")
    {:decimals 18
     :address  "0xeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"}
    (tokens/token-for (ethereum/network->chain-keyword network) all-tokens token)))

(re-frame/reg-sub
 :extensions.wallet/balance
 :<- [:wallet/all-tokens]
 :<- [:network]
 :<- [:balance]
 (fn [[all-tokens network balance] [_ _ {token :token}]]
   (let [{:keys [decimals]} (get-token-for network all-tokens token)
         value (or (get balance (keyword token)) (money/bignumber 0))]
     {:value        (money/token->unit value decimals)
      :value-in-wei value})))

(re-frame/reg-sub
 :extensions.wallet/token
 :<- [:wallet/all-tokens]
 :<- [:network]
 (fn [[all-tokens network] [_ _ {token :token amount :amount amount-in-wei :amount-in-wei}]]
   (let [{:keys [decimals] :as m} (get-token-for network all-tokens token)]
     (merge m
            (when amount {:amount-in-wei (money/unit->token amount decimals)})
            (when amount-in-wei {:amount (money/token->unit amount-in-wei decimals)})))))

(defn normalize-token [m]
  (update m :symbol name))

(re-frame/reg-sub
 :extensions.wallet/tokens
 :<- [:wallet/all-tokens]
 :<- [:wallet/visible-tokens-symbols]
 :<- [:network]
 (fn [[all-tokens visible-tokens-symbols network] [_ _ {filter-vector :filter visible :visible}]]
   (let [tokens (map normalize-token (filter #(and (not (:nft? %)) (if visible (contains? visible-tokens-symbols (:symbol %)) true))
                                             (tokens/sorted-tokens-for all-tokens (ethereum/network->chain-keyword network))))]
     (if filter-vector
       (filter #((set filter-vector) (:symbol %)) tokens)
       tokens))))

(re-frame/reg-sub
 :store/get
 (fn [db [_ {id :id} {:keys [key] :as params}]]
   (let [result (get-in db [:extensions/store id key])]
     (if (:reverse params)
       (reverse result)
       result))))

(re-frame/reg-sub
 :store/get-in
 (fn [db [_ {id :id} {:keys [keys]}]]
   (get-in db (into [] (concat [:extensions/store id] keys)))))

(defn- ->contact [{:keys [photo-path address name public-key]}]
  {:photo      photo-path
   :name       name
   :address    (str "0x" address)
   :public-key public-key})

(re-frame/reg-sub
 :extensions.contacts/all
 :<- [:contacts/active]
 (fn [[contacts] _]
   (map #(update % :address ->contact))))

(re-frame/reg-sub
 :store/get-vals
 (fn [db [_ {id :id} {:keys [key]}]]
   (vals (get-in db [:extensions/store id key]))))

(re-frame/reg-sub
 :extensions.time/now
 (fn [_ _]
   (.toLocaleString (js/Date.))))

;;CAPACITIES

(def all
  {'identity            {:data :extensions/identity :arguments {:value :map}}
   'store/get           {:data :store/get :arguments {:key :string :reverse? :boolean}}
   'store/get-in        {:data :store/get-in :arguments {:key :vector}}
   'store/get-vals      {:data :store/get-vals :arguments {:key :string}}
   'time/now            {:data :extensions.time/now}
   'contacts/all        {:data :extensions.contacts/all} ;; :photo :name :address :public-key
   'wallet/collectibles {:data :get-collectible-token :arguments {:token :string :symbol :string}}
   'wallet/balance      {:data :extensions.wallet/balance :arguments {:token :string}}
   'wallet/token        {:data :extensions.wallet/token :arguments {:token :string :amount? :number :amount-in-wei? :number}}
   'wallet/tokens       {:data :extensions.wallet/tokens :arguments {:filter? :vector :visible? :boolean}}})