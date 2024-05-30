(ns status-im.contexts.settings.wallet.saved-addresses.sheets.address-options.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.common.not-implemented :as not-implemented]
    [status-im.constants :as constants]
    [status-im.contexts.settings.wallet.saved-addresses.sheets.remove-address.view :as remove-address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [name full-address chain-short-names address] :as opts}]
  (let [open-send-flow                 (rn/use-callback
                                        #(rf/dispatch [:wallet/select-send-address
                                                       {:address     full-address
                                                        :recipient   full-address
                                                        :stack-id    :wallet-select-address
                                                        :start-flow? true}]))
        open-eth-chain-explorer        (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/mainnet-network-name}]))
        open-arb-chain-explorer        (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/arbitrum-network-name}]))
        open-oeth-chain-explorer       (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/optimism-network-name}]))

        open-share                     (rn/use-callback
                                        #(rf/dispatch
                                          [:open-share
                                           {:options (if platform/ios?
                                                       {:activityItemSources
                                                        [{:placeholderItem {:type    :text
                                                                            :content full-address}
                                                          :item            {:default {:type :text
                                                                                      :content
                                                                                      full-address}}
                                                          :linkMetadata    {:title full-address}}]}
                                                       {:title     full-address
                                                        :message   full-address
                                                        :isNewTask true})}]))
        open-remove-confirmation-sheet (rn/use-callback
                                        #(rf/dispatch
                                          [:show-bottom-sheet
                                           {:theme   :dark
                                            :shell?  true
                                            :content (fn []
                                                       [remove-address/view opts])}]))]
    [quo/action-drawer
     [[{:icon                :i/arrow-up
        :label               (i18n/label :t/send-to-user {:user name})
        :blur?               true
        :on-press            open-send-flow
        :accessibility-label :send-to-user}
       {:icon                :i/link
        :right-icon          :i/external
        :label               (i18n/label :t/view-address-on-etherscan)
        :blur?               true
        :on-press            open-eth-chain-explorer
        :accessibility-label :view-address-on-etherscan}
       (when (string/includes? chain-short-names constants/optimism-short-name)
         {:icon                :i/link
          :right-icon          :i/external
          :label               (i18n/label :t/view-address-on-optimistic)
          :blur?               true
          :on-press            open-oeth-chain-explorer
          :accessibility-label :view-address-on-optimistic})
       (when (string/includes? chain-short-names constants/arbitrum-short-name)
         {:icon                :i/link
          :right-icon          :i/external
          :label               (i18n/label :t/view-address-on-arbiscan)
          :blur?               true
          :on-press            open-arb-chain-explorer
          :accessibility-label :view-address-on-arbiscan})
       {:icon                :i/share
        :on-press            open-share
        :label               (i18n/label :t/share-address)
        :blur?               true
        :accessibility-label :share-saved-address}
       {:icon                :i/qr-code
        :label               (i18n/label :t/show-address-qr)
        :blur?               true
        :on-press            not-implemented/alert
        :accessibility-label :show-address-qr-code}
       {:icon                :i/edit
        :label               (i18n/label :t/edit-account)
        :blur?               true
        :on-press            not-implemented/alert
        :accessibility-label :edit-saved-address}
       {:icon                :i/delete
        :label               (i18n/label :t/remove-address)
        :blur?               true
        :on-press            open-remove-confirmation-sheet
        :danger?             true
        :accessibility-label :remove-saved-address
        :add-divider?        true}]]]))
