(ns status-im.contexts.settings.wallet.saved-addresses.sheets.remove-address.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn view
  [{:keys [name address customization-color]}]
  (let [on-press-remove (rn/use-callback
                         #(rf/dispatch [:wallet/delete-saved-address
                                        {:address       address
                                         :toast-message (i18n/label :t/saved-address-removed)}])
                         [address])]
    [:<>
     [quo/drawer-top
      {:title               (i18n/label :t/remove-saved-address)
       :customization-color customization-color
       :type                :context-tag
       :blur?               true
       :context-tag-type    :wallet-user
       :full-name           name}]
     [quo/text
      {:style {:padding-horizontal 20
               :margin-bottom      8}}
      (i18n/label :t/remove-saved-address-description {:name name})]
     [quo/bottom-actions
      {:actions          :two-actions
       :blur?            true
       :button-one-label (i18n/label :t/remove)
       :button-one-props {:on-press on-press-remove
                          :type     :danger}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:on-press hide-bottom-sheet
                          :type     :grey}}]]))
