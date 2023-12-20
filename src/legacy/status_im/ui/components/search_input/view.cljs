(ns legacy.status-im.ui.components.search-input.view
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]))

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
