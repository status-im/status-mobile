(ns status-im.contexts.settings.wallet.wallet-options.view
  (:require [quo.core :as quo]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.settings.wallet.wallet-options.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn open-saved-addresses-settings-modal
  []
  (rf/dispatch [:open-modal :screen/settings.saved-addresses]))

(defn open-keypairs-and-accounts-settings-modal
  []
  (rf/dispatch [:open-modal :screen/settings.keypairs-and-accounts]))

(defn basic-settings-options
  []
  [{:title    (i18n/label :t/keypairs-and-accounts)
    :blur?    true
    :on-press open-keypairs-and-accounts-settings-modal
    :action   :arrow}
   {:title    (i18n/label :t/saved-addresses)
    :blur?    true
    :on-press open-saved-addresses-settings-modal
    :action   :arrow}])

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
  (rf/dispatch [:open-modal :screen/settings.network-settings]))

(defn advanced-settings-options
  []
  [{:title    (i18n/label :t/network-settings)
    :blur?    true
    :on-press open-network-settings-modal
    :action   :arrow}])

(defn advanced-settings
  []
  [quo/category
   {:key       :advanced-wallet-settings
    :label     (i18n/label :t/advanced)
    :data      (advanced-settings-options)
    :blur?     true
    :list-type :settings}])

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  [quo/overlay
   {:type            :shell
    :container-style (style/page-wrapper safe-area/top)}
   [quo/page-nav
    {:key        :header
     :background :blur
     :icon-name  :i/arrow-left
     :on-press   navigate-back}]
   [quo/page-top
    {:title                     (i18n/label :t/wallet)
     :title-accessibility-label :wallet-settings-header}]
   [basic-settings]
   [advanced-settings]])
