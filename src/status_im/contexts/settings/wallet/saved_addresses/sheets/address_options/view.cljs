(ns status-im.contexts.settings.wallet.saved-addresses.sheets.address-options.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [status-im.contexts.settings.wallet.saved-addresses.sheets.remove-address.view :as remove-address]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn chain-explorer-options
  [address network-details]
  (->> network-details
       (map (fn [{:keys [chain-id block-explorer-name]}]
              {:icon                :i/link
               :right-icon          :i/external
               :label               (i18n/label :t/view-address-on-block-explorer
                                                {:block-explorer-name block-explorer-name})
               :blur?               true
               :on-press            #(rf/dispatch [:wallet/navigate-to-chain-explorer chain-id address])
               :accessibility-label :view-on-block-explorer}))))

(defn view
  [{:keys [name address customization-color] :as address-details}]
  (let [network-details                (rf/sub [:wallet/network-details])
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
     [(concat
       [{:icon                :i/arrow-up
         :label               (i18n/label :t/send-to-user {:user name})
         :blur?               true
         :on-press            open-send-flow
         :accessibility-label :send-to-user}]
       (chain-explorer-options address network-details)
       [{:icon                :i/share
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
         :add-divider?        true}])]]))
