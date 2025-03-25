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
  [{:keys [details testnet-label testnet-mode?]}]
  (let [{:keys [network-name full-name]} details]
    (cond-> {:blur?       true
             :title       full-name
             :image       :icon-avatar
             :image-props {:icon (resources/get-network network-name)
                           :size :size-20}}
      testnet-mode? (assoc
                     :label       :text
                     :label-props testnet-label))))

(defn mainnet-settings
  [{:keys [networks testnet-mode?]}]
  [quo/category
   {:key       :mainnet-settings
    :data      [(make-network-settings-item
                 {:details       (:mainnet networks)
                  :testnet-mode? testnet-mode?
                  :testnet-label (i18n/label :t/sepolia-active)})]
    :blur?     true
    :list-type :settings}])

(defn layer-2-settings
  [{:keys [networks testnet-mode?]}]
  (let [network-items [{:details       (:arbitrum networks)
                        :testnet-mode? testnet-mode?
                        :testnet-label (i18n/label :t/sepolia-active)}
                       {:details       (:base networks)
                        :testnet-mode? testnet-mode?
                        :testnet-label (i18n/label :t/sepolia-active)}
                       {:details       (:optimism networks)
                        :testnet-mode? testnet-mode?
                        :testnet-label (i18n/label :t/sepolia-active)}]
        network-items (if (and testnet-mode? (not-empty (:status networks)))
                        (conj network-items
                              {:details       (:status networks)
                               :testnet-mode? testnet-mode?
                               :testnet-label (i18n/label :t/sepolia-active)})
                        network-items)]
    [quo/category
     {:key       :layer-2-settings
      :label     (i18n/label :t/layer-2)
      :data      (map make-network-settings-item network-items)
      :blur?     true
      :list-type :settings}]))

(defn testnet-mode-setting
  [{:keys [testnet-mode? on-enable on-disable]}]
  (let [on-change-testnet (rn/use-callback
                           (fn []
                             (if-not testnet-mode? (on-enable) (on-disable)))
                           [testnet-mode? on-enable on-disable])]
    {:blur?        true
     :title        (i18n/label :t/testnet-mode)
     :action       :selector
     :image        :icon
     :image-props  :i/settings
     :action-props {:on-change on-change-testnet
                    :checked?  (boolean testnet-mode?)}}))

(defn advanced-settings
  [{:keys [testnet-mode? enable-testnet disable-testnet]}]
  [quo/category
   {:key       :advanced-settings
    :label     (i18n/label :t/advanced)
    :data      [(testnet-mode-setting {:testnet-mode? testnet-mode?
                                       :on-enable     enable-testnet
                                       :on-disable    disable-testnet})]
    :blur?     true
    :list-type :settings}])

(defn on-change-testnet
  [{:keys [enable? blur? theme]}]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [testnet/view
                                  {:enable? enable?
                                   :blur?   blur?}])
                 :theme   theme
                 :shell?  blur?}]))

(defn view
  []
  (let [blur?            true
        insets           (safe-area/get-insets)
        theme            (quo.theme/use-theme)
        networks-by-name (rf/sub [:wallet/network-details-by-network-name])
        testnet-mode?    (rf/sub [:profile/test-networks-enabled?])
        enable-testnet   (rn/use-callback
                          (fn []
                            (on-change-testnet {:theme   theme
                                                :blur?   blur?
                                                :enable? true}))
                          [theme])
        disable-testnet  (rn/use-callback
                          (fn []
                            (on-change-testnet {:theme   theme
                                                :blur?   blur?
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
     [quo/standard-title
      {:title               (i18n/label :t/network-settings)
       :container-style     style/title-container
       :accessibility-label :network-settings-header}]
     [rn/view {:style (style/settings-container (:bottom insets))}
      (when networks-by-name
        [rn/view {:style style/networks-container}
         [mainnet-settings
          {:networks      networks-by-name
           :testnet-mode? testnet-mode?}]
         [layer-2-settings
          {:networks      networks-by-name
           :testnet-mode? testnet-mode?}]])
      [rn/view {:style style/advanced-settings-container}
       [advanced-settings
        {:testnet-mode?   testnet-mode?
         :enable-testnet  enable-testnet
         :disable-testnet disable-testnet}]]]]))
