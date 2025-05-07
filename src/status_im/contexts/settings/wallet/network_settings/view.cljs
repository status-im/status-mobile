(ns status-im.contexts.settings.wallet.network-settings.view
  (:require [quo.context]
            [quo.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.settings.wallet.network-settings.style :as style]
            [status-im.contexts.settings.wallet.network-settings.testnet-mode.view :as testnet]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn make-network-settings-item
  [customization-color network]
  (let [{:keys [full-name source active? deactivatable? chain-id]} network]
    {:blur?        true
     :action       :selector
     :action-props {:type                :toggle
                    :blur?               false
                    :customization-color customization-color
                    :disabled?           (not deactivatable?)
                    :checked?            active?
                    :on-change           #(rf/dispatch
                                           [:wallet/toggle-network-active
                                            chain-id])}
     :title        full-name
     :image        :icon-avatar
     :image-props  {:icon source
                    :size :size-20}}))

(defn networks-list
  []
  (let [networks            (rf/sub [:wallet/networks])
        customization-color (rf/sub [:profile/customization-color])]
    [quo/category
     {:key       :layer-2-settings
      :data      (mapv (partial make-network-settings-item customization-color) networks)
      :blur?     true
      :list-type :settings}]))

(defn on-change-testnet
  [theme]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [testnet/view
                                  {:blur? true}])
                 :theme   theme
                 :shell?  true}]))

(defn advanced-settings
  []
  (let [theme            (quo.context/use-theme)
        testnet-mode?    (rf/sub [:profile/test-networks-enabled?])
        on-press-testnet (rn/use-callback
                          (fn []
                            (if-not testnet-mode?
                              (on-change-testnet theme)
                              (on-change-testnet theme)))
                          [theme])]
    [quo/category
     {:key       :advanced-settings
      :data      [{:blur?        true
                   :title        (i18n/label :t/testnet-mode)
                   :action       :selector
                   :image        :icon
                   :image-props  :i/settings
                   :action-props {:on-change on-press-testnet
                                  :checked?  (boolean testnet-mode?)}}]
      :blur?     true
      :list-type :settings}]))

(defn view
  []
  (let [insets                safe-area/insets
        max-active-networks   (rf/sub [:wallet/max-available-active-networks])
        active-networks-count (rf/sub [:wallet/active-networks-count])]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [rn/view {:style style/title-container}
      [quo/standard-title
       {:title               (i18n/label :t/network-settings)
        :blur?               true
        :accessibility-label :network-settings-header}]
      [quo/fraction-counter
       {:blur?                 true
        :show-counter-warning? true
        :left-value            active-networks-count
        :right-value           max-active-networks
        :suffix                (i18n/label :t/active)}]]
     [rn/scroll-view
      {:style                   {:flex 1}
       :content-container-style (style/settings-container (:bottom insets))}
      [rn/view {:style style/advanced-settings-container}
       [advanced-settings]]
      [rn/view {:style style/networks-container}
       [networks-list]]]]))
