(ns status-im.contexts.wallet.common.activity-tab.options.view
  (:require [networks.core :as networks]
            [quo.core :as quo]
            [react-native.clipboard :as clipboard]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys   [chain-id]
    tx-hash :hash}]
  (let [tx-details-link-on-block-explorer (networks/tx-details-url chain-id tx-hash)
        open-tx-on-block-explorer         (rn/use-callback
                                           #(rf/dispatch [:browser.ui/open-url
                                                          tx-details-link-on-block-explorer])
                                           [tx-details-link-on-block-explorer])
        copy-tx-hash-to-clipboard         (rn/use-callback
                                           (fn []
                                             (clipboard/set-string tx-hash)
                                             (rf/dispatch
                                              [:toasts/upsert
                                               {:type :positive
                                                :text
                                                (i18n/label
                                                 :t/transaction-hash-copied-to-clipboard)}]))
                                           [tx-hash])
        share-link-to-block-explorer      (rn/use-callback
                                           #(rf/dispatch [:open-share
                                                          {:options
                                                           {:message
                                                            tx-details-link-on-block-explorer}}])
                                           [tx-details-link-on-block-explorer])]
    [quo/action-drawer
     [[{:icon                :i/link
        :accessibility-label :view-on-block-explorer
        :on-press            open-tx-on-block-explorer
        :label               (i18n/label :t/view-on-chain-explorer
                                         {:chain-explorer-name (networks/chain-explorer-name
                                                                chain-id)})
        :right-icon          :i/external}]
      [{:icon                :i/copy
        :accessibility-label :copy-transaction-hash
        :label               (i18n/label :t/copy-transaction-hash)
        :on-press            copy-tx-hash-to-clipboard}]
      [{:icon                :i/share
        :accessibility-label :share-link-to-block-explorer
        :label               (i18n/label :t/share-link-to-chain-explorer
                                         {:chain-explorer-name (networks/chain-explorer-name
                                                                chain-id)})
        :on-press            share-link-to-block-explorer}]]]))
