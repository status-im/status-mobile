(ns status-im.subs.general
  (:require
    [clojure.string :as string]
    [legacy.status-im.multiaccounts.model :as multiaccounts.model]
    [legacy.status-im.utils.build :as build]
    [re-frame.core :as re-frame]
    [status-im.subs.chat.utils :as chat.utils]
    [utils.ethereum.chain :as chain]))

(re-frame/reg-sub
 :visibility-status-updates/visibility-status-update
 :<- [:multiaccount/public-key]
 :<- [:multiaccount/current-user-visibility-status]
 :<- [:visibility-status-updates]
 (fn [[my-public-key my-status-update status-updates] [_ public-key]]
   (if (or (string/blank? public-key) (= public-key my-public-key))
     my-status-update
     (get status-updates public-key))))

(re-frame/reg-sub
 :visibility-status-updates/online?
 (fn [[_ public-key]]
   [(re-frame/subscribe [:visibility-status-updates/visibility-status-update public-key])])
 (fn [[status-update]]
   (let [visibility-status-type (:status-type status-update)]
     (chat.utils/online? visibility-status-type))))

(re-frame/reg-sub
 :multiaccount/logged-in?
 (fn [db]
   (multiaccounts.model/logged-in? db)))

(re-frame/reg-sub
 :hide-screen?
 :<- [:app-state]
 :<- [:profile/profile]
 (fn [[state multiaccount]]
   (and (= state "inactive") (:preview-privacy? multiaccount))))

(re-frame/reg-sub
 :current-network
 :<- [:networks/networks]
 :<- [:networks/current-network]
 (fn [[networks current-network]]
   (when-let [network (get networks current-network)]
     (assoc network :rpc-network? (get-in network [:config :UpstreamConfig :Enabled])))))

(re-frame/reg-sub
 :chain-name
 :<- [:current-network]
 (fn [network]
   (chain/network->chain-name network)))

(re-frame/reg-sub
 :chain-id
 :<- [:current-network]
 (fn [network]
   (chain/network->chain-id network)))

(re-frame/reg-sub
 :network-name
 :<- [:current-network]
 (fn [network]
   (:name network)))

(re-frame/reg-sub
 :syncing?
 :<- [:sync-state]
 (fn [sync-state]
   (#{:pending :in-progress} sync-state)))

(re-frame/reg-sub
 :dimensions/window-width
 :<- [:dimensions/window]
 :-> :width)

(re-frame/reg-sub
 :dimensions/window-height
 :<- [:dimensions/window]
 :-> :height)

(re-frame/reg-sub
 :get-screen-params
 :<- [:screen-params]
 :<- [:view-id]
 (fn [[params view-id-db] [_ view-id]]
   (get params (or view-id view-id-db))))

(defn- node-version
  [web3-node-version]
  (or web3-node-version "N/A"))

(re-frame/reg-sub
 :get-app-short-version
 (fn [_] build/app-short-version))

(re-frame/reg-sub
 :get-commit-hash
 (fn [_] build/commit-hash))

(re-frame/reg-sub
 :get-app-node-version
 :<- [:web3-node-version]
 node-version)

(re-frame/reg-sub
 :my-profile/recovery
 :<- [:my-profile/seed]
 (fn [seed]
   (or seed {:step :intro})))

(re-frame/reg-sub
 :is-contact-selected?
 :<- [:group/selected-contacts]
 (fn [selected-contacts [_ element]]
   (-> selected-contacts
       (contains? element))))

(re-frame/reg-sub
 :mnemonic
 :<- [:profile/profile]
 (fn [{:keys [mnemonic]}]
   mnemonic))

(re-frame/reg-sub
 :toasts/toast
 :<- [:toasts]
 (fn [toasts [_ toast-id]]
   (get-in toasts [:toasts toast-id])))

(re-frame/reg-sub
 :toasts/toast-cursor
 :<- [:toasts]
 (fn [toasts [_ toast-id & cursor]]
   (get-in toasts (into [:toasts toast-id] cursor))))

(re-frame/reg-sub :network/offline?
 :<- [:network/status]
 (fn [status]
   (= status :offline)))

(re-frame/reg-sub :network/online?
 :<- [:network/status]
 (fn [status]
   (= status :online)))

(re-frame/reg-sub :currencies/categorized
 :<- [:currencies]
 (fn [currencies [_ query]]
   (let [search-lc (string/lower-case query)]
     (reduce
      (fn [acc currency]
        (let [{:keys [popular? token? name short-name]} currency
              matches-query?                            (or (string/includes? (string/lower-case
                                                                               name)
                                                                              search-lc)
                                                            (string/includes? (string/lower-case
                                                                               short-name)
                                                                              search-lc))]
          (cond-> acc
            matches-query?                                   (update :total inc)
            (and popular? matches-query?)                    (update :popular conj currency)
            (and token? matches-query?)                      (update :crypto conj currency)
            (and matches-query? (not popular?) (not token?)) (update :other conj currency))))
      {:total   0
       :popular []
       :crypto  []
       :other   []}
      (vals currencies)))))

(re-frame/reg-sub
 :wakuv2-nodes/validation-errors
 :<- [:wakuv2-nodes/manage]
 (fn [manage]
   (set (keep
         (fn [[k {:keys [error]}]]
           (when error k))
         manage))))
