(ns status-im.contexts.settings.wallet.saved-addresses.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.common.resources :as resources]
            [status-im.contexts.settings.wallet.saved-addresses.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn empty-state
  []
  (let [theme (quo.theme/use-theme)]
    [quo/empty-state
     {:title           (i18n/label :t/no-saved-addresses)
      :description     (i18n/label :t/you-like-to-type-43-characters)
      :image           (resources/get-themed-image :sweating-man theme)
      :container-style style/empty-container-style}]))

(defn on-press-add-saved-address
  []
  (when true
    (rf/dispatch [:wallet/add-address-to-save
                  {:title                :t/add-address
                   :description          :t/save-address-description
                   :input-title          :t/address-or-ens-name
                   :accessibility-label  :add-address-to-save
                   :screen               :screen/wallet.add-address-to-save
                   :confirm-screen       :screen/wallet.confirm-address-to-save
                   :confirm-screen-props {:button-label :t/save-address
                                          :address-type :t/address
                                          :placeholder  :t/saved-address}}])))

(defn view
  []
  (let [inset-top           (safe-area/get-top)
        customization-color (rf/sub [:profile/customization-color])
        saved-addresses?    (rf/sub [:wallet/saved-addresses?])
        navigate-back       (rn/use-callback #(rf/dispatch [:navigate-back]))]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper inset-top)}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [rn/view {:style style/title-container}
      [quo/standard-title
       {:title               (i18n/label :t/saved-addresses)
        :accessibility-label :saved-addresses-header
        :right               :action
        :on-press            on-press-add-saved-address
        :customization-color customization-color
        :icon                :i/add}]]
     (when-not saved-addresses?
       [empty-state])]))
