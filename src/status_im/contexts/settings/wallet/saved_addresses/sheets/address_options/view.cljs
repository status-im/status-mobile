(ns status-im.contexts.settings.wallet.saved-addresses.sheets.address-options.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.constants :as constants]
    [status-im.contexts.settings.wallet.saved-addresses.sheets.remove-address.view :as remove-address]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [name full-address chain-short-names address] :as opts}]
  (let [[_ splitted-address]           (network-utils/split-network-full-address address)
        open-send-flow                 (rn/use-callback
                                        #(rf/dispatch [:wallet/select-send-address
                                                       {:address     full-address
                                                        :recipient   {:label (utils/get-shortened-address
                                                                              splitted-address)
                                                                      :recipient-type :saved-address}
                                                        :stack-id    :wallet-select-address
                                                        :start-flow? true}])
                                        [full-address])
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
                                                        :isNewTask true})}])
                                        [full-address])
        open-remove-confirmation-sheet (rn/use-callback
                                        #(rf/dispatch
                                          [:show-bottom-sheet
                                           {:theme   :dark
                                            :shell?  true
                                            :content (fn []
                                                       [remove-address/view opts])}])
                                        [opts])
        open-show-address-qr           (rn/use-callback
                                        #(rf/dispatch [:open-modal
                                                       :screen/settings.share-saved-address opts])
                                        [opts])
        open-edit-saved-address        (rn/use-callback
                                        (fn []
                                          (rf/dispatch [:wallet/set-address-to-save opts])
                                          (rf/dispatch [:open-modal
                                                        :screen/settings.edit-saved-address
                                                        {:edit? true}]))
                                        [opts])]
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
