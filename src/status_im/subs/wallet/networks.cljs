(ns status-im.subs.wallet.networks
  (:require [re-frame.core :as re-frame]
            [status-im.contexts.wallet.networks.core :as networks]
            [status-im.contexts.wallet.networks.filter :as networks.filter]))

#_"
   Since our wallet can handle a limited number of networks simultaneously,
   we can only have N number of networks `active` at a time, so we should be
   careful about which networks subscriptions we use and where.

   - The following subscriptions will return only *active* networks, which should
   be used in most cases within the wallet.

   `:wallet/active-chain-ids` (returns a set of active chain ids)
   `:wallet/active-networks` (returns a vector of active networks)

   - The following subscriptions will return filtered active networks, based
   on the filter state, and should be used in the wallet when showing
   balances and collectibles.

   `:wallet/filtered-chain-ids` (returns a set of chain ids)
   `:wallet/filtered-networks` (returns a vector of networks)

   - The following subscriptions will return *all* the networks we support,
   regardless if active or not. If unsure which to use, it's likely you
   need the active networks instead.

   `:wallet/chain-ids` (returns a set of chain ids)
   `:wallet/networks` (returns a vector of networks)

   - We can also get the details of *any* network by its `chain-id` with:

    `:wallet/networks-by-id` (returns a map of networks indexed by chain-id)
    `:wallet/network-by-id` (returns the network for the passed chain-id)
"

(re-frame/reg-sub
 :wallet/chain-ids-by-mode
 :<- [:wallet]
 :-> :networks/chain-ids-by-mode)

(re-frame/reg-sub
 :wallet/networks-by-id
 :<- [:wallet]
 :-> :networks/by-id)

(re-frame/reg-sub
 :wallet/network-filter
 :<- [:wallet/ui]
 :-> :networks/network-filter)

(re-frame/reg-sub
 :wallet/network-filter?
 :<- [:wallet/network-filter]
 (fn [network-filter]
   (networks.filter/has-filters? network-filter)))

(re-frame/reg-sub
 :wallet/chain-ids
 :<- [:wallet/chain-ids-by-mode]
 :<- [:profile/test-networks-enabled?]
 (fn [[chain-ids-by-mode testnet?]]
   (set (get chain-ids-by-mode (if testnet? :test :prod)))))

(re-frame/reg-sub
 :wallet/networks
 :<- [:wallet/chain-ids]
 :<- [:wallet/networks-by-id]
 (fn [[chain-ids networks-by-id]]
   (->> chain-ids
        (map #(get networks-by-id %))
        vec)))

(re-frame/reg-sub
 :wallet/network-by-id
 :<- [:wallet/networks-by-id]
 (fn [networks [_ chain-id]]
   (get networks chain-id)))

(re-frame/reg-sub
 :wallet/network-name-from-chain-id
 :<- [:wallet/networks-by-id]
 (fn [networks [_ chain-id]]
   (-> networks (get chain-id) :network-name)))

(re-frame/reg-sub
 :wallet/active-chain-ids
 :<- [:wallet/networks]
 (fn [networks]
   (networks/get-active-chain-ids networks)))

(re-frame/reg-sub
 :wallet/active-networks
 :<- [:wallet/networks]
 (fn [networks]
   (networks/get-active-networks networks)))

(re-frame/reg-sub
 :wallet/max-available-active-networks
 :<- [:wallet/chain-ids]
 (fn [chain-ids]
   (min (count chain-ids)
        (networks/get-max-active-networks))))

(re-frame/reg-sub
 :wallet/active-networks-count
 :<- [:wallet/active-chain-ids]
 (fn [chain-ids]
   (count chain-ids)))

(re-frame/reg-sub
 :wallet/filtered-networks
 :<- [:wallet/active-networks]
 :<- [:wallet/network-filter]
 (fn [[active-networks network-filter]]
   (networks/get-filtered-networks active-networks network-filter)))

(re-frame/reg-sub
 :wallet/filtered-chain-ids
 :<- [:wallet/active-networks]
 :<- [:wallet/network-filter]
 (fn [[active-networks network-filter]]
   (networks/get-filtered-chain-ids active-networks network-filter)))

(re-frame/reg-sub
 :wallet/network-filter-toggled?
 :<- [:wallet/filtered-chain-ids]
 (fn [filtered-chain-ids [_ chain-id]]
   (contains? filtered-chain-ids chain-id)))

(re-frame/reg-sub
 :wallet/disable-network-filter?
 :<- [:wallet/filtered-chain-ids]
 (fn [filtered-chain-ids [_ chain-id]]
   (and (= 1 (count filtered-chain-ids))
        (contains? filtered-chain-ids chain-id))))

(re-frame/reg-sub
 :wallet/layer-1-networks
 :<- [:wallet/active-networks]
 (fn [networks]
   (networks/get-networks-for-layer networks 1)))

(re-frame/reg-sub
 :wallet/eth-mainnet-network
 :<- [:wallet/layer-1-networks]
 (fn [networks]
   (first networks)))

(re-frame/reg-sub
 :wallet/layer-2-networks
 :<- [:wallet/active-networks]
 (fn [networks]
   (networks/get-networks-for-layer networks 2)))

(re-frame/reg-sub
 :wallet/balance-for-network-filter
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/current-account-balances-by-network]
 :<- [:wallet/balances-by-network]
 (fn [[current-address current-account-balances balances] [_ chain-id]]
   (-> (if (seq current-address) current-account-balances balances)
       (get chain-id 0))))

(re-frame/reg-sub
 :wallet/collectibles-count-for-network-filter
 :<- [:wallet/current-viewing-account-address]
 :<- [:wallet/current-account-collectibles-by-network]
 :<- [:wallet/collectibles-by-network]
 (fn [[current-address current-account-collectibles collectibles] [_ chain-id]]
   (-> (if (seq current-address) current-account-collectibles collectibles)
       (get chain-id)
       count)))

(re-frame/reg-sub
 :wallet/show-new-chain-indicator?
 :<- [:wallet/ui]
 (fn [ui]
   (-> ui
       (get :networks/new-marked-as-seen? true)
       not)))

(re-frame/reg-sub
 :wallet/max-active-networks-reached?
 :<- [:wallet/active-chain-ids]
 (fn [active-chain-ids]
   (-> active-chain-ids
       count
       (>= (networks/get-max-active-networks)))))

(re-frame/reg-sub
 :wallet/deactivatable-networks
 :<- [:wallet/active-networks]
 (fn [active-networks]
   (filter :deactivatable? active-networks)))
