(ns status-im.contexts.settings.wallet.saved-addresses.view
  (:require
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.resources :as resources]
    [status-im.contexts.settings.wallet.saved-addresses.sheets.address-options.view :as address-options]
    [status-im.contexts.settings.wallet.saved-addresses.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- empty-state
  []
  (let [theme (quo.theme/use-theme)]
    [quo/empty-state
     {:title           (i18n/label :t/no-saved-addresses)
      :description     (i18n/label :t/you-like-to-type-43-characters)
      :image           (resources/get-themed-image :sweating-man theme)
      :container-style style/empty-container-style}]))

(defn- saved-address
  [{:keys [name address chain-short-names customization-color has-ens? ens]}]
  (let [full-address           (str chain-short-names address)
        on-press-saved-address (rn/use-callback
                                #(rf/dispatch
                                  [:show-bottom-sheet
                                   {:theme   :dark
                                    :shell?  true
                                    :content (fn []
                                               [address-options/view
                                                {:address             address
                                                 :chain-short-names   chain-short-names
                                                 :full-address        full-address
                                                 :name                name
                                                 :customization-color customization-color}])}])
                                [address chain-short-names full-address name customization-color])]
    [quo/saved-address
     {:blur?           true
      :user-props      {:name                name
                        :address             full-address
                        :ens                 (when has-ens? ens)
                        :customization-color customization-color
                        :blur?               true}
      :container-style {:margin-horizontal 8}
      :on-press        on-press-saved-address}]))

(defn- header
  [{:keys [title]}]
  [quo/divider-label
   {:tight? true
    :blur?  true}
   title])

(defn- footer
  []
  [rn/view {:height 8}])

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- add-address-to-save
  []
  (rf/dispatch [:navigate-to-within-stack
                [:screen/settings.add-address-to-save :screen/settings.saved-addresses]]))

(defn view
  []
  (let [inset-top           (safe-area/get-top)
        customization-color (rf/sub [:profile/customization-color])
        saved-addresses     (rf/sub [:wallet/grouped-saved-addresses])]
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
        :on-press            add-address-to-save
        :customization-color customization-color
        :icon                :i/add}]]
     [rn/section-list
      {:key-fn                         :title
       :sticky-section-headers-enabled false
       :render-section-header-fn       header
       :render-section-footer-fn       footer
       :sections                       saved-addresses
       :render-fn                      saved-address
       :separator                      [rn/view {:style {:height 4}}]
       :content-container-style        {:flex-grow 1}
       :empty-component                [empty-state]}]]))
