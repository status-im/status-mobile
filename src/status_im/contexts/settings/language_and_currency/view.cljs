(ns status-im.contexts.settings.language-and-currency.view
  (:require [quo.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- on-currency-press
  []
  (rf/dispatch [:open-modal :screen/settings.currency-selection]))

(defn view
  []
  (let [{:keys [name short-name token?]
         :as   currency} (rf/sub [:profile/currency-info])
        currency-title   (if token? name (str short-name " · " (:symbol currency)))]
    [quo/overlay
     {:type       :shell
      :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/page-top
      {:title (i18n/label :t/language-and-currency)}]
     [quo/category
      {:label     (i18n/label :t/currency)
       :data      [{:title             currency-title
                    :on-press          on-currency-press
                    :description       :text
                    :action            :arrow
                    :description-props {:text name}}]
       :blur?     true
       :list-type :settings}]]))
