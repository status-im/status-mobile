(ns status-im.contexts.wallet.collectible.options.view
  (:require
    [quo.core :as quo]
    [react-native.platform :as platform]
    [status-im.config :as config]
    [status-im.constants :as constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(defn get-url
  [chain-id]
  (cond
    (= chain-id constants/ethereum-mainnet-chain-id)
    config/mainnet-chain-explorer-link

    (= chain-id constants/arbitrum-mainnet-chain-id)
    config/arbitrum-mainnet-chain-explorer-link

    (= chain-id constants/optimism-mainnet-chain-id)
    config/optimism-mainnet-chain-explorer-link


    (= chain-id constants/ethereum-goerli-chain-id)
    config/goerli-chain-explorer-link

    (= chain-id constants/optimism-goerli-chain-id)
    config/optimism-goerli-chain-explorer-link

    (= chain-id constants/ethereum-sepolia-chain-id)
    config/sepolia-chain-explorer-link

    (= chain-id constants/arbitrum-sepolia-chain-id)
    config/arbitrum-sepolia-chain-explorer-link

    (= chain-id constants/optimism-sepolia-chain-id)
    config/optimism-sepolia-chain-explorer-link
    :else config/mainnet-chain-explorer-link))

(defn view
  [{:keys [image name chain-id address]}]
  (let [uri (url/replace-port image (rf/sub [:mediaserver/port]))]
    [quo/action-drawer
     [[{:icon                :i/link
        :accessibility-label :view-on-etherscan
        :on-press            (fn []
                               (rf/dispatch [:wallet/navigate-to-chain-explorer-from-bottom-sheet
                                             (get-url chain-id)
                                             address]))
        :label               (i18n/label :t/view-on-eth)
        :right-icon          :i/external}]
      [{:icon                :i/save
        :accessibility-label :save-image
        :label               (i18n/label :t/save-image-to-photos)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (rf/dispatch
                                [:lightbox/save-image-to-gallery
                                 uri
                                 #(rf/dispatch [:toasts/upsert
                                                {:id              :random-id
                                                 :type            :positive
                                                 :container-style {:bottom (when platform/android? 20)}
                                                 :text            (i18n/label :t/photo-saved)}])]))}]
      [{:icon                :i/share
        :accessibility-label :share-collectible
        :label               (i18n/label :t/share-collectible)
        :on-press            (fn []
                               (rf/dispatch [:hide-bottom-sheet])
                               (js/setTimeout
                                #(rf/dispatch [:wallet/share-collectible
                                               {:title name
                                                :uri   uri}])
                                600))}]]]))
