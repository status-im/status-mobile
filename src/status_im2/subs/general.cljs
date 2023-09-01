(ns status-im2.subs.general
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.tokens :as tokens]
            [status-im.multiaccounts.model :as multiaccounts.model]
            [status-im.utils.build :as build]
            [status-im.utils.mobile-sync :as mobile-network-utils]
            [status-im2.constants :as constants]))

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
     (or (= visibility-status-type constants/visibility-status-automatic)
         (= visibility-status-type constants/visibility-status-always-online)))))

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
 :custom-rpc-node
 :<- [:current-network]
 (fn [network]
   (ethereum/custom-rpc-node? network)))

(re-frame/reg-sub
 :chain-keyword
 :<- [:current-network]
 (fn [network]
   (ethereum/network->chain-keyword network)))

(re-frame/reg-sub
 :chain-name
 :<- [:current-network]
 (fn [network]
   (ethereum/network->chain-name network)))

(re-frame/reg-sub
 :chain-id
 :<- [:current-network]
 (fn [network]
   (ethereum/network->chain-id network)))

(re-frame/reg-sub
 :mainnet?
 :<- [:chain-id]
 (fn [chain-id]
   (= 1 chain-id)))

(re-frame/reg-sub
 :ethereum-network?
 :<- [:chain-id]
 (fn [chain-id]
   (< chain-id 6)))

(re-frame/reg-sub
 :network-name
 :<- [:current-network]
 (fn [network]
   (:name network)))

(re-frame/reg-sub
 :disconnected?
 :<- [:peers-count]
 :<- [:waku/v2-flag]
 :<- [:waku/v2-peer-stats]
 (fn [[peers-count wakuv2-flag peer-stats]]
   ;; If wakuv2 is enabled, then fetch connectivity status from
   ;; peer-stats (populated from "wakuv2.peerstats" status-go signal)
   ;; Otherwise use peers-count fetched from "discovery.summary" signal
   (if wakuv2-flag (not (:isOnline peer-stats)) (zero? peers-count))))

(re-frame/reg-sub
 :offline?
 :<- [:network-status]
 :<- [:sync-state]
 :<- [:disconnected?]
 (fn [[network-status sync-state disconnected?]]
   (or disconnected?
       (= network-status :offline)
       (= sync-state :offline))))

(re-frame/reg-sub
 :syncing?
 :<- [:sync-state]
 (fn [sync-state]
   (#{:pending :in-progress} sync-state)))

(re-frame/reg-sub
 :dimensions/window-width
 :<- [:dimensions/window]
 :width)

(re-frame/reg-sub
 :dimensions/window-height
 :<- [:dimensions/window]
 :height)

(re-frame/reg-sub
 :dimensions/small-screen?
 :<- [:dimensions/window-height]
 (fn [height]
   (< height 550)))

(re-frame/reg-sub
 :get-screen-params
 :<- [:screen-params]
 :<- [:view-id]
 (fn [[params view-id-db] [_ view-id]]
   (get params (or view-id view-id-db))))

(re-frame/reg-sub
 :delete-swipe-position
 :<- [:animations]
 (fn [animations [_ type item-id]]
   (get-in animations [type item-id :delete-swiped])))

(re-frame/reg-sub
 :search/recipient-filter
 :<- [:ui/search]
 (fn [search]
   (get search :recipient-filter)))

(re-frame/reg-sub
 :search/currency-filter
 :<- [:ui/search]
 (fn [search]
   (get search :currency-filter)))

(re-frame/reg-sub
 :search/token-filter
 :<- [:ui/search]
 (fn [search]
   (get search :token-filter)))

(defn- node-version
  [web3-node-version]
  (or web3-node-version "N/A"))

(re-frame/reg-sub
 :get-app-version
 :<- [:web3-node-version]
 (fn [web3-node-version]
   (str build/app-short-version "; " (node-version web3-node-version))))

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
 :bottom-sheet-old
 :<- [:bottom-sheet/show?]
 :<- [:bottom-sheet/view]
 :<- [:bottom-sheet/options]
 (fn [[show? view options]]
   {:show?   show?
    :view    view
    :options options}))

(re-frame/reg-sub
 :is-contact-selected?
 :<- [:group/selected-contacts]
 (fn [selected-contacts [_ element]]
   (-> selected-contacts
       (contains? element))))

(re-frame/reg-sub
 :is-participant-selected?
 :<- [:group-chat/selected-participants]
 (fn [selected-participants [_ element]]
   (-> selected-participants
       (contains? element))))

(re-frame/reg-sub
 :ethereum/chain-keyword
 :<- [:current-network]
 (fn [network]
   (ethereum/network->chain-keyword network)))

(re-frame/reg-sub
 :ethereum/native-currency
 :<- [:current-network]
 (fn [network]
   (tokens/native-currency network)))

(re-frame/reg-sub
 :connectivity/state
 :<- [:network-status]
 :<- [:disconnected?]
 :<- [:mailserver/connecting?]
 :<- [:mailserver/connection-error?]
 :<- [:mailserver/request-error?]
 :<- [:network/type]
 :<- [:profile/profile]
 (fn [[network-status disconnected? mailserver-connecting? mailserver-connection-error?
       mailserver-request-error? network-type {:keys [syncing-on-mobile-network? use-mailservers?]}]]
   (merge {:mobile (mobile-network-utils/cellular? network-type)
           :sync   syncing-on-mobile-network?
           :peers  :online}
          (cond
            (= network-status :offline)
            {:peers :offline
             :node  :offline}

            (not use-mailservers?)
            {:node :disabled}

            (or mailserver-connection-error? mailserver-connecting?)
            {:node :connecting}

            mailserver-request-error?
            {:node :error}

            disconnected?
            {:peers :offline
             :node  :offline}

            :else
            {:peers :online
             :node  :online}))))

(re-frame/reg-sub
 :mnemonic
 :<- [:profile/profile]
 (fn [{:keys [mnemonic]}]
   mnemonic))

(re-frame/reg-sub
 :get-profile-unread-messages-number
 :<- [:profile/profile]
 (fn [{:keys [mnemonic]}]
   (if mnemonic 1 0)))

(re-frame/reg-sub
 :mobile-network/syncing-allowed?
 :<- [:network/type]
 :<- [:profile/profile]
 (fn [[network-type {:keys [syncing-on-mobile-network?]}]]
   (or (= network-type "wifi")
       (and
        (= network-type "cellular")
        syncing-on-mobile-network?))))
