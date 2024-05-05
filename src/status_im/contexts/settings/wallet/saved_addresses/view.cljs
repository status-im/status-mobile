(ns status-im.contexts.settings.wallet.saved-addresses.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
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

(defn- saved-address
  [{:keys        [address colorId chainShortNames isTest]
    address-name :name}]
  [quo/saved-address
   {:user-props {:name                address-name
                 :address             address
                 :customization-color (keyword colorId)}}])

(defn header
  [{:keys [title]}]
  [quo/divider-label
   {:container-style {:background-color :transparent
                      :border-top-color colors/white-opa-5
                      :margin-top       16}}
   title])

(defn footer
  []
  [rn/view {:height 8}])

(defn view
  []
  (let [inset-top           (safe-area/get-top)
        customization-color (rf/sub [:profile/customization-color])
        saved-addresses     (rf/sub [:wallet/grouped-saved-addresses])
        navigate-back       (rn/use-callback #(rf/dispatch [:navigate-back]))]
    (rn/use-effect
     (fn []
       (rf/dispatch [:wallet/get-saved-addresses])))
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
        :customization-color customization-color
        :icon                :i/add}]]

     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :render-section-header-fn       header
       :render-section-footer-fn       footer
       :sections                       saved-addresses
       :render-fn                      saved-address
       :separator                      [rn/view {:style {:height 4}}]}]
     (when-not (seq saved-addresses)
       [empty-state])]))
