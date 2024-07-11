(ns status-im.contexts.settings.privacy-and-security.view
  (:require
    [quo.core :as quo]
    [quo.theme]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.settings.privacy-and-security.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn view
  []
  (let [insets                       (safe-area/get-insets)
        centralized-metrics-enabled? (rf/sub [:centralized-metrics/enabled?])
        customization-color          (rf/sub [:profile/customization-color])]
    [quo/overlay
     {:type            :shell
      :container-style (style/page-wrapper (:top insets))}
     [quo/page-nav
      {:key        :header
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   navigate-back}]
     [quo/standard-title
      {:title               (i18n/label :t/privacy-and-security)
       :container-style     style/title-container
       :accessibility-label :privacy-and-security-header
       :customization-color customization-color}]
     [quo/category
      {:key       :category
       :data      [{:title        (i18n/label :t/share-usage-data)
                    :image-props  :i/placeholder
                    :blur?        true
                    :action       :selector
                    :action-props {:on-change #(rf/dispatch
                                                [:centralized-metrics/toggle-centralized-metrics
                                                 (not centralized-metrics-enabled?)])
                                   :customization-color customization-color
                                   :checked? centralized-metrics-enabled?}
                    :on-press     identity}]
       :blur?     true
       :list-type :settings}]]))
