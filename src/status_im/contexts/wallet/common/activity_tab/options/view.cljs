(ns status-im.contexts.wallet.common.activity-tab.options.view
  (:require [quo.core :as quo]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [status-im.contexts.wallet.networks.core :as networks]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys   [chain-id]
    tx-hash :hash}]
  (let [{:keys [block-explorer-name] :as network} (rf/sub [:wallet/network-details-by-chain-id chain-id])
        tx-details-link-on-block-explorer         (networks/get-block-explorer-tx-url network tx-hash)
        open-tx-on-block-explorer                 (rn/use-callback
                                                   #(rf/dispatch [:browser.ui/open-url
                                                                  tx-details-link-on-block-explorer])
                                                   [tx-details-link-on-block-explorer])
        copy-tx-hash-to-clipboard                 (rn/use-callback
                                                   (fn []
                                                     (clipboard/set-string tx-hash)
                                                     (rf/dispatch
                                                      [:toasts/upsert
                                                       {:type :positive
                                                        :text
                                                        (i18n/label
                                                         :t/transaction-hash-copied-to-clipboard)}]))
                                                   [tx-hash])
        share-link-to-block-explorer              (rn/use-callback
                                                   #(rf/dispatch [:open-share
                                                                  {:options
                                                                   {:message
                                                                    tx-details-link-on-block-explorer}}])
                                                   [tx-details-link-on-block-explorer])]
    [quo/action-drawer
     [[{:icon                :i/link
        :accessibility-label :view-on-block-explorer
        :on-press            open-tx-on-block-explorer
        :label               (i18n/label :t/view-on-block-explorer
                                         {:block-explorer-name block-explorer-name})
        :right-icon          :i/external}]
      [{:icon                :i/copy
        :accessibility-label :copy-transaction-hash
        :label               (i18n/label :t/copy-transaction-hash)
        :on-press            copy-tx-hash-to-clipboard}]
      [{:icon                :i/share
        :accessibility-label :share-link-to-block-explorer
        :label               (i18n/label :t/share-link-to-block-explorer
                                         {:block-explorer-name block-explorer-name})
        :on-press            share-link-to-block-explorer}]]]))
