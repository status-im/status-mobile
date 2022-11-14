(ns status-im.ui2.components.search-input
  (:require [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo2.foundations.colors :as quo2.colors]))

(defn search-input [{:keys [search-active?]}]
  (let [input-ref        (atom nil)
        placeholder-text (reagent/atom :t/search-contacts)
        search-active?   (or search-active? (reagent/atom nil))]
    (fn [{:keys [on-focus on-change search-border-width show-cancel? search-border-radius on-blur on-cancel search-filter auto-focus] :or {show-cancel? false}}]
      [quo/text-input {:placeholder            (i18n/label @placeholder-text)
                       :placeholder-text-color (if (= @placeholder-text :t/search-contacts)
                                                 (quo2.colors/theme-colors quo2.colors/neutral-30 quo2.colors/neutral-50)
                                                 (quo2.colors/theme-colors quo2.colors/neutral-30 quo2.colors/neutral-60))
                       :accessibility-label    :search-input
                       :text-padding-left      0
                       :blur-on-submit         true
                       :multiline              false
                       :get-ref                #(reset! input-ref %)
                       :default-value          search-filter
                       :auto-focus             auto-focus
                       :on-cancel              on-cancel
                       :show-cancel            show-cancel?
                       :auto-correct           false
                       :auto-capitalize        :none
                       :container-style        {:border-radius    (or search-border-radius 10)
                                                :border-width     (or search-border-width 1)
                                                :border-color     (:ui-01  @colors/theme)
                                                :background-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
                                                :overflow         :hidden}
                       :input-style            {:height           32
                                                :padding-top      2
                                                :padding-bottom   2
                                                :background-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)}
                       :on-focus               #(do
                                                  (when on-focus
                                                    (on-focus search-filter))
                                                  (reset! search-active? true)
                                                  (reset! placeholder-text :t/who-are-you-looking-for))
                       :on-blur                #(do
                                                  (when on-blur
                                                    (on-blur))
                                                  (reset! search-active? false)
                                                  (reset! placeholder-text :t/search-contacts))
                       :on-change              (fn [e]
                                                 (let [^js native-event (.-nativeEvent ^js e)
                                                       text         (.-text native-event)]
                                                   (when on-change
                                                     (on-change text))))}])))
