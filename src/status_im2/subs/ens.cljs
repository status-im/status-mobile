(ns status-im2.subs.ens
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ens.core :as ens]
            [status-im.ethereum.core :as ethereum]
            [utils.money :as money]))

(re-frame/reg-sub
 :ens/preferred-name
 :<- [:profile/profile]
 (fn [multiaccount]
   (:preferred-name multiaccount)))

(re-frame/reg-sub
 :ens/search-screen
 :<- [:ens/registration]
 (fn [{:keys [custom-domain? username state]}]
   {:state          state
    :username       username
    :custom-domain? custom-domain?}))

(defn- ens-amount-label
  [chain-id]
  (str (ens/registration-cost chain-id)
       (case chain-id
         3 " STT"
         1 " SNT"
         "")))

(re-frame/reg-sub
 :ens/checkout-screen
 :<- [:ens/registration]
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
                          (money/formatted->internal (money/bignumber 10)
                                                     (ethereum/chain-keyword->snt-symbol chain)
                                                     18)
                          (get balance (ethereum/chain-keyword->snt-symbol chain)))})))

(re-frame/reg-sub
 :ens/confirmation-screen
 :<- [:ens/registration]
 (fn [{:keys [username state]}]
   {:state    state
    :username username}))

(re-frame/reg-sub
 :ens.name/screen
 :<- [:get-screen-params :ens-name-details]
 :<- [:ens/names]
 (fn [[name ens]]
   (let [{:keys [address public-key expiration-date releasable?]} (get ens name)
         pending?                                                 (nil? address)]
     (cond-> {:name           name
              :custom-domain? (not (string/ends-with? name ".stateofus.eth"))}
       pending?
       (assoc :pending? true)
       (not pending?)
       (assoc :address         address
              :public-key      public-key
              :releasable?     releasable?
              :expiration-date expiration-date)))))

(re-frame/reg-sub
 :ens.main/screen
 :<- [:ens/names]
 :<- [:profile/profile]
 :<- [:ens/preferred-name]
 :<- [:ens/registrations]
 (fn [[names multiaccount preferred-name registrations]]
   (let [not-in-progress-names (reduce (fn [acc {:keys [username custom-domain?]}]
                                         (let [full-name (ens/fullname custom-domain? username)]
                                           (remove #(= % full-name) acc)))
                                       (keys names)
                                       (vals registrations))]
     {:names           not-in-progress-names
      :profile/profile multiaccount
      :preferred-name  preferred-name
      :registrations   registrations})))
