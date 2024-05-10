(ns status-im.contexts.settings.wallet.wallet-options.view
  (:require [quo.core :as quo]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.settings.wallet.wallet-options.style :as style]
            [status-im.feature-flags :as ff]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn open-saved-addresses-settings-modal
  []
  (rf/dispatch [:open-modal :screen/settings.saved-addresses]))

(defn open-keypairs-and-accounts-settings-modal
  []
  (rf/dispatch [:open-modal :screen/settings.keypairs-and-accounts]))

(defn gen-basic-settings-options
  []
  [(when (ff/enabled? ::ff/settings.keypairs-and-accounts)
     {:title    (i18n/label :t/keypairs-and-accounts)
      :blur?    true
      :on-press open-keypairs-and-accounts-settings-modal
      :action   :arrow})
   {:title    (i18n/label :t/saved-addresses)
    :blur?    true
    :on-press open-saved-addresses-settings-modal
    :action   :arrow}])

(defn basic-settings
  []
  [quo/category
   {:key       :basic-wallet-settings
    :label     (i18n/label :t/keypairs-accounts-and-addresses)
    :data      (gen-basic-settings-options)
    :blur?     true
    :list-type :settings}])

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
     [basic-settings]]))
