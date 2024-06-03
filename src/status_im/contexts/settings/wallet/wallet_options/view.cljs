(ns status-im.contexts.settings.wallet.wallet-options.view
  (:require [quo.core :as quo]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.settings.wallet.wallet-options.style :as style]
            [status-im.feature-flags :as ff]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn open-saved-addresses-settings-modal
  []
  (rf/dispatch [:navigate-to-within-stack [:screen/settings.saved-addresses :screen/settings]]))

(defn open-keypairs-and-accounts-settings-modal
  []
  (rf/dispatch [:navigate-to-within-stack [:screen/settings.keypairs-and-accounts :screen/settings]]))

(defn basic-settings-options
  []
  [(when (ff/enabled? ::ff/settings.keypairs-and-accounts)
     {:title    (i18n/label :t/keypairs-and-accounts)
      :blur?    true
      :on-press open-keypairs-and-accounts-settings-modal
      :action   :arrow})
   (when (ff/enabled? ::ff/settings.saved-addresses)
     {:title    (i18n/label :t/saved-addresses)
      :blur?    true
      :on-press open-saved-addresses-settings-modal
      :action   :arrow})])

(defn basic-settings
  []
  [quo/category
   {:key       :basic-wallet-settings
    :label     (i18n/label :t/keypairs-accounts-and-addresses)
    :data      (basic-settings-options)
    :blur?     true
    :list-type :settings}])

(defn open-network-settings-modal
  []
  (rf/dispatch [:navigate-to-within-stack [:screen/settings.network-settings :screen/settings]]))

(defn advanced-settings-options
  []
  [{:title    (i18n/label :t/network-settings)
    :blur?    true
    :on-press open-network-settings-modal
    :action   :arrow}])

(defn advanced-settings
  []
  (when (ff/enabled? ::ff/settings.network-settings)
    [quo/category
     {:key       :advanced-wallet-settings
      :label     (i18n/label :t/advanced)
      :data      (advanced-settings-options)
      :blur?     true
      :list-type :settings}]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [inset-top (safe-area/get-top)]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper inset-top)}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/page-top
      {:title                     (i18n/label :t/wallet)
       :title-accessibility-label :wallet-settings-header}]
     [basic-settings]
     [advanced-settings]]))
