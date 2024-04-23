(ns status-im.contexts.settings.wallet.view
  (:require [quo.core :as quo]
            [react-native.safe-area :as safe-area]
            [status-im.contexts.settings.wallet.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn basic-settings
  []
  [quo/category
   {:key       :basic-wallet-settings
    :label     (i18n/label :t/keypairs-accounts-and-addresses)
    :data      [{:title    (i18n/label :t/saved-addresses)
                 :blur?    true
                 :on-press #(rf/dispatch [:open-modal :screen/settings.saved-addresses])
                 :action   :arrow}]
    :blur?     true
    :list-type :settings}])

(defn view
  []
  (let [insets (safe-area/get-insets)]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   #(rf/dispatch [:navigate-back])}]
     [quo/page-top
      {:title                     (i18n/label :t/wallet)
       :title-accessibility-label :wallet-settings-header}]
     [basic-settings]]))
