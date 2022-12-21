(ns status-im.ui.components.search-input.view
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo2.foundations.colors :as quo2.colors]
            [reagent.core :as reagent]
            [status-im.i18n.i18n :as i18n]))

(defn search-input
  [{:keys [search-active?]}]
  (let [input-ref      (atom nil)
        search-active? (or search-active? (reagent/atom nil))]
    (fn [{:keys [on-focus on-change on-blur on-cancel search-filter auto-focus]}]
      [quo/text-input
       {:placeholder         (i18n/label :t/search)
        :accessibility-label :search-input
        :blur-on-submit      true
        :multiline           false
        :get-ref             #(reset! input-ref %)
        :default-value       search-filter
        :auto-focus          auto-focus
        :on-cancel           on-cancel
        :show-cancel         true
        :auto-correct        false
        :auto-capitalize     :none
        :container-style     {:border-radius    10
                              :border-width     1
                              :border-color     (:ui-01 @colors/theme)
                              :background-color (quo2.colors/theme-colors quo2.colors/white
                                                                          quo2.colors/neutral-90)
                              :overflow         :hidden}
        :input-style         {:height           32
                              :padding-top      2
                              :padding-bottom   2
                              :background-color (quo2.colors/theme-colors quo2.colors/white
                                                                          quo2.colors/neutral-90)}
        :before              {:icon      :main-icons/search2
                              :style     {:padding-horizontal 8
                                          :background-color   (quo2.colors/theme-colors
                                                               quo2.colors/white
                                                               quo2.colors/neutral-90)}
                              :on-press  #(some-> ^js @input-ref
                                                  (.focus))
                              :icon-opts {:color (quo2.colors/theme-colors quo2.colors/neutral-50
                                                                           quo2.colors/white)}}
        :on-focus            #(do
                                (when on-focus
                                  (on-focus search-filter))
                                (reset! search-active? true))
        :on-blur             #(do
                                (when on-blur
                                  (on-blur))
                                (reset! search-active? false))
        :on-change           (fn [e]
                               (let [^js native-event (.-nativeEvent ^js e)
                                     text             (.-text native-event)]
                                 (when on-change
                                   (on-change text))))}])))

(defn search-input-old
  [{:keys [search-active?]}]
  (let [input-ref      (atom nil)
        search-active? (or search-active? (reagent/atom nil))]
    (fn [{:keys [on-focus on-change on-blur on-cancel search-filter auto-focus]}]
      [quo/text-input
       {:placeholder         (i18n/label :t/search)
        :accessibility-label :search-input
        :blur-on-submit      true
        :multiline           false
        :get-ref             #(reset! input-ref %)
        :default-value       search-filter
        :auto-focus          auto-focus
        :on-cancel           on-cancel
        :show-cancel         true
        :auto-correct        false
        :auto-capitalize     :none
        :input-style         {:height         36
                              :padding-top    2
                              :padding-bottom 2}
        :before              {:icon      :main-icons/search
                              :style     {:padding-horizontal 8}
                              :on-press  #(some-> ^js @input-ref
                                                  (.focus))
                              :icon-opts {:color (:icon-02 @colors/theme)}}
        :on-focus            #(do
                                (when on-focus
                                  (on-focus search-filter))
                                (reset! search-active? true))
        :on-blur             #(do
                                (when on-blur
                                  (on-blur))
                                (reset! search-active? false))
        :on-change           (fn [e]
                               (let [^js native-event (.-nativeEvent ^js e)
                                     text             (.-text native-event)]
                                 (when on-change
                                   (on-change text))))}])))
