(ns status-im.contexts.settings.wallet.network-settings.view
  (:require [quo.core :as quo]
            [quo.foundations.resources :as resources]
            [quo.theme]
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
  [{:keys [network-name]}]
  {:blur?       true
   :title       (i18n/label network-name)
   :image       :icon-avatar
   :image-props {:icon (resources/get-network network-name)
                 :size :size-20}})

(defn mainnet-settings
  [mainnet-details]
  [quo/category
   {:key       :mainnet-settings
    :data      [(make-network-settings-item mainnet-details)]
    :blur?     true
    :list-type :settings}])

(defn layer-2-settings
  [networks-by-name]
  [quo/category
   {:key       :layer-2-settings
    :label     (i18n/label :t/layer-2)
    :data      (map make-network-settings-item
                    [(:optimism networks-by-name)
                     (:arbitrum networks-by-name)])
    :blur?     true
    :list-type :settings}])

(defn testnet-mode-setting
  [{:keys [on-enable on-disable]}]
  (let [testnet-mode-enabled? (rf/sub [:profile/test-networks-enabled?])
        on-change-testnet     (rn/use-callback
                               (fn [active?]
                                 (if active? (on-enable) (on-disable)))
                               [on-enable on-disable])]
    {:blur?        true
     :title        (i18n/label :t/testnet-mode)
     :action       :selector
     :image        :icon
     :image-props  :i/settings
     :action-props {:on-change on-change-testnet
                    :checked?  (boolean testnet-mode-enabled?)}}))

(defn advanced-settings
  [{:keys [enable-testnet disable-testnet]}]
  [quo/category
   {:key       :advanced-settings
    :label     (i18n/label :t/advanced)
    :data      [(testnet-mode-setting {:on-enable  enable-testnet
                                       :on-disable disable-testnet})]
    :blur?     true
    :list-type :settings}])

(defn on-change-testnet
  [{:keys [enable? theme]}]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [testnet/view {:enable? enable?}])
                 :theme   theme}]))

(defn view
  []
  (let [insets           (safe-area/get-insets)
        theme            (quo.theme/use-theme)
        networks-by-name (rf/sub [:wallet/network-details-by-network-name])
        enable-testnet   (rn/use-callback
                          (fn []
                            (on-change-testnet {:theme   theme
                                                :enable? true}))
                          [theme])
        disable-testnet  (rn/use-callback
                          (fn []
                            (on-change-testnet {:theme   theme
                                                :enable? false}))
                          [theme])]
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
        :accessibility-label :network-settings-header}]]
     [rn/view {:style (style/settings-container (:bottom insets))}
      (when networks-by-name
        [rn/view {:style style/networks-container}
         [mainnet-settings (:mainnet networks-by-name)]
         [layer-2-settings networks-by-name]])
      [rn/view {:style style/advanced-settings-container}
       [advanced-settings
        {:enable-testnet  enable-testnet
         :disable-testnet disable-testnet}]]]]))
