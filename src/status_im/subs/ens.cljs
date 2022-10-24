(ns status-im.subs.ens
  (:require [re-frame.core :as re-frame]
            [status-im.domain.core :as domain]
            [status-im.ethereum.core :as ethereum]
            [status-im.utils.money :as money]
            [clojure.string :as string]))

(re-frame/reg-sub
 :multiaccount/usernames
 :<- [:multiaccount]
 (fn [multiaccount]
   (:usernames multiaccount)))

(re-frame/reg-sub
 :domain/preferred-name
 :<- [:multiaccount]
 (fn [multiaccount]
   (:preferred-name multiaccount)))

(re-frame/reg-sub
 :domain/search-screen
 :<- [:domain/registration]
 (fn [{:keys [custom-domain? username state]}]
   {:state          state
    :username       username
    :custom-domain? custom-domain?}))

(defn- ens-amount-label
  [chain-id]
  (str (domain/registration-cost chain-id)
       (case chain-id
         3 " STT"
         1 " SNT"
         "")))

(re-frame/reg-sub
 :domain/checkout-screen
 :<- [:domain/registration]
 :<- [:chain-keyword]
 :<- [:multiaccount/default-account]
 :<- [:multiaccount/public-key]
 :<- [:chain-id]
 :<- [:wallet]
 (fn [[{:keys [custom-domain? username address]}
       chain default-account public-key chain-id wallet]]
   (let [address (or address (ethereum/normalized-hex (:address default-account)))
         balance (get-in wallet [:accounts address :balance])]
     {:address           address
      :username          username
      :public-key        public-key
      :custom-domain?    custom-domain?
      :chain             chain
      :amount-label      (ens-amount-label chain-id)
      :sufficient-funds? (money/sufficient-funds?
                          (money/formatted->internal (money/bignumber 10) (ethereum/chain-keyword->snt-symbol chain) 18)
                          (get balance (ethereum/chain-keyword->snt-symbol chain)))})))

(re-frame/reg-sub
 :domain/confirmation-screen
 :<- [:domain/registration]
 (fn [{:keys [username state]}]
   {:state          state
    :username       username}))

(re-frame/reg-sub
 :ens.name/screen
 :<- [:get-screen-params :ens-name-details]
 :<- [:domain/names]
 (fn [[name ens]]
   (let [{:keys [address public-key expiration-date releasable?]} (get ens name)
         pending? (nil? address)]
     (cond-> {:name           name
              :custom-domain? (not (string/ends-with? name ".stateofus.eth"))}
       pending?
       (assoc :pending? true)
       (not pending?)
       (assoc :address    address
              :public-key public-key
              :releasable? releasable?
              :expiration-date expiration-date)))))

(re-frame/reg-sub
 :ens.main/screen
 :<- [:multiaccount/usernames]
 :<- [:multiaccount]
 :<- [:domain/preferred-name]
 :<- [:domain/registrations]
 (fn [[names multiaccount preferred-name registrations]]
   {:names             names
    :multiaccount      multiaccount
    :preferred-name    preferred-name
    :registrations registrations}))