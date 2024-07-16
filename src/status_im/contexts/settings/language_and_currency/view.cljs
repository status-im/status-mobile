(ns status-im.contexts.settings.language-and-currency.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.navigation :as navigation]
            [utils.re-frame :as rf]))

(defn- on-currency-press
  []
  (rf/dispatch [:open-modal :screen/settings.currency-selection]))

(defn view
  []
  (let [{:keys [display-name code token?]
         :as   currency} (rf/sub [:profile/currency-info])
        currency-title   (rn/use-memo
                          #(if token? code (str code " · " (:symbol currency)))
                          [currency])]
    [quo/overlay
     {:type       :shell
      :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   navigation/navigate-back}]
     [quo/page-top
      {:title (i18n/label :t/language-and-currency)}]
     [quo/category
      {:label     (i18n/label :t/currency)
       :data      [{:title             currency-title
                    :on-press          on-currency-press
                    :description       :text
                    :action            :arrow
                    :description-props {:text display-name}}]
       :blur?     true
       :list-type :settings}]]))
