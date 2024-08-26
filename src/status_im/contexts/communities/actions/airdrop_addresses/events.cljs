(ns status-im.contexts.communities.actions.airdrop-addresses.events
  (:require
    [utils.re-frame :as rf]))

(defn set-airdrop-address
  [{:keys [db]} [community-id airdrop-address]]
  {:db (assoc-in db [:communities/all-airdrop-addresses community-id] airdrop-address)})

(rf/reg-event-fx :communities/set-airdrop-address set-airdrop-address)
