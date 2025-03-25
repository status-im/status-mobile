(ns status-im.contexts.settings.wallet.saved-addresses.sheets.address-options.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.constants :as constants]
    [status-im.contexts.settings.wallet.saved-addresses.sheets.remove-address.view :as remove-address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [name address customization-color] :as address-details}]
  (let [test-networks-enabled?         (rf/sub [:profile/test-networks-enabled?])
        open-send-flow                 (rn/use-callback
                                        (fn []
                                          (rf/dispatch [:wallet/init-send-flow-for-address
                                                        {:address address
                                                         :recipient
                                                         {:label name
                                                          :customization-color
                                                          customization-color
                                                          :recipient-type :saved-address}
                                                         :stack-id :screen/settings.saved-addresses}]))
                                        [name customization-color])
        open-eth-chain-explorer        (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/mainnet-network-name}])
                                        [address])
        open-arb-chain-explorer        (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/arbitrum-network-name}])
                                        [address])
        open-oeth-chain-explorer       (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/optimism-network-name}])
                                        [address])
        open-base-chain-explorer       (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/base-network-name}])
                                        [address])
        open-status-chain-explorer     (rn/use-callback
                                        #(rf/dispatch [:wallet/navigate-to-chain-explorer
                                                       {:address address
                                                        :network constants/status-network-name}])
                                        [address])
        open-share                     (rn/use-callback
                                        #(rf/dispatch
                                          [:open-share
                                           {:options (if platform/ios?
                                                       {:activityItemSources
                                                        [{:placeholderItem {:type    :text
                                                                            :content address}
                                                          :item            {:default {:type :text
                                                                                      :content
                                                                                      address}}
                                                          :linkMetadata    {:title address}}]}
                                                       {:title     address
                                                        :message   address
                                                        :isNewTask true})}])
                                        [address])
        open-remove-confirmation-sheet (rn/use-callback
                                        #(rf/dispatch
                                          [:show-bottom-sheet
                                           {:theme   :dark
                                            :shell?  true
                                            :content (fn []
                                                       [remove-address/view address-details])}])
                                        [address-details])
        open-show-address-qr           (rn/use-callback
                                        #(rf/dispatch [:open-modal
                                                       :screen/settings.share-saved-address
                                                       address-details])
                                        [address-details])
        open-edit-saved-address        (rn/use-callback
                                        (fn []
                                          (rf/dispatch [:open-modal
                                                        :screen/settings.edit-saved-address
                                                        (merge {:edit? true}
                                                               address-details)]))
                                        [address-details])]
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
       {:icon                :i/link
        :right-icon          :i/external
        :label               (i18n/label :t/view-address-on-optimistic)
        :blur?               true
        :on-press            open-oeth-chain-explorer
        :accessibility-label :view-address-on-optimistic}
       {:icon                :i/link
        :right-icon          :i/external
        :label               (i18n/label :t/view-address-on-arbiscan)
        :blur?               true
        :on-press            open-arb-chain-explorer
        :accessibility-label :view-address-on-arbiscan}
       {:icon                :i/link
        :right-icon          :i/external
        :label               (i18n/label :t/view-address-on-basescan)
        :blur?               true
        :on-press            open-base-chain-explorer
        :accessibility-label :view-address-on-basescan}
       (when test-networks-enabled?
         {:icon                :i/link
          :right-icon          :i/external
          :label               (i18n/label :t/view-address-on-status-explorer)
          :blur?               true
          :on-press            open-status-chain-explorer
          :accessibility-label :view-address-on-status-explorer})
       {:icon                :i/share
        :on-press            open-share
        :label               (i18n/label :t/share-address)
        :blur?               true
        :accessibility-label :share-saved-address}
       {:icon                :i/qr-code
        :label               (i18n/label :t/show-address-qr)
        :blur?               true
        :on-press            open-show-address-qr
        :accessibility-label :show-address-qr-code}
       {:icon                :i/edit
        :label               (i18n/label :t/edit-details)
        :blur?               true
        :on-press            open-edit-saved-address
        :accessibility-label :edit-saved-address}
       {:icon                :i/delete
        :label               (i18n/label :t/remove-address)
        :blur?               true
        :on-press            open-remove-confirmation-sheet
        :danger?             true
        :accessibility-label :remove-saved-address
        :add-divider?        true}]]]))
