(ns status-im.contexts.settings.wallet.keypairs-and-accounts.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.actions.view :as actions]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.style :as style]
            [status-im.feature-flags :as ff]
            [utils.address :as utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn on-options-press
  [{:keys [drawer-props keypair]}]
  (rf/dispatch [:show-bottom-sheet
                {:content (fn [] [actions/view
                                  {:drawer-props drawer-props
                                   :keypair      keypair}])

                 :theme   (:theme drawer-props)
                 :shell?  true}]))

(defn options-drawer-props
  [{{:keys [name]} :keypair
    :keys          [type stored theme shortened-key customization-color profile-picture]}]
  (cond-> {:theme  theme
           :type   type
           :blur?  true
           :title  name
           :stored stored}
    (= type :default-keypair)
    (assoc :description         shortened-key
           :customization-color customization-color
           :profile-picture     profile-picture)
    (= type :keypair)
    (assoc :icon-avatar :i/seed)))

(defn- keypair
  [{keypair-type :type
    :keys        [accounts name keycard?]
    :as          item}
   _ _
   {:keys [profile-picture compressed-key customization-color]}]
  (let [theme            (quo.theme/use-theme)
        default-keypair? (= keypair-type :profile)
        shortened-key    (when default-keypair?
                           (utils/get-shortened-compressed-key compressed-key))
        on-press         (rn/use-callback
                          (fn []
                            (on-options-press
                             {:keypair      item
                              :drawer-props (options-drawer-props
                                             {:theme               theme
                                              :keypair             item
                                              :type                (if default-keypair?
                                                                     :default-keypair
                                                                     :keypair)
                                              :stored              (if keycard? :on-keycard :on-device)
                                              :shortened-key       shortened-key
                                              :customization-color customization-color
                                              :profile-picture     profile-picture})}))
                          [customization-color default-keypair? item
                           profile-picture shortened-key theme])]
    [quo/keypair
     {:blur?               true
      :status-indicator    false
      :stored              (if keycard? :on-keycard :on-device)
      :action              (if default-keypair? :none :options)
      :accounts            accounts
      :customization-color customization-color
      :container-style     style/keypair-container-style
      :profile-picture     (when default-keypair? profile-picture)
      :type                (if default-keypair? :default-keypair :other)
      :on-options-press    on-press
      :details             {:full-name name
                            :address   shortened-key}}]))

(defn on-missing-keypair-options-press
  [_event keypair-data]
  (rf/dispatch [:show-bottom-sheet
                {:theme   :dark
                 :shell?  true
                 :content (fn [] [actions/view
                                  {:keypair      keypair-data
                                   :drawer-props (options-drawer-props
                                                  {:theme   :dark
                                                   :type    :keypair
                                                   :stored  :missing
                                                   :blur?   true
                                                   :keypair keypair-data})}])}]))

(defn view
  []
  (let [insets                        (safe-area/get-insets)
        compressed-key                (rf/sub [:profile/compressed-key])
        profile-picture               (rf/sub [:profile/image])
        customization-color           (rf/sub [:profile/customization-color])
        {missing-keypairs  :missing
         operable-keypairs :operable} (rf/sub [:wallet/settings-keypairs-accounts])
        on-import-press               (rn/use-callback #(rf/dispatch [:open-modal
                                                                      :screen/settings.scan-keypair-qr
                                                                      (map :key-uid missing-keypairs)])
                                                       [missing-keypairs])]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/standard-title
      {:title               (i18n/label :t/keypairs-and-accounts)
       :container-style     style/title-container
       :accessibility-label :keypairs-and-accounts-header
       :customization-color customization-color}]
     [rn/view {:style style/settings-keypairs-container}
      [rn/flat-list
       {:data                    operable-keypairs
        :render-fn               keypair
        :header                  (when (seq missing-keypairs)
                                   [quo/missing-keypairs
                                    {:blur?            true
                                     :show-import-all? (ff/enabled? ::ff/settings.import-all-keypairs)
                                     :keypairs         missing-keypairs
                                     :on-import-press  on-import-press
                                     :container-style  style/missing-keypairs-container-style
                                     :on-options-press on-missing-keypair-options-press}])
        :render-data             {:profile-picture     profile-picture
                                  :compressed-key      compressed-key
                                  :customization-color customization-color}
        :content-container-style (style/list-container (:bottom insets))}]]]))
