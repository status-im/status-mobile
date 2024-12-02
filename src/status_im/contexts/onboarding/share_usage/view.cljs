(ns status-im.contexts.onboarding.share-usage.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.events-helper :as events-helper]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.share-usage.learn-more-sheet :as learn-more-sheet]
    [status-im.contexts.onboarding.share-usage.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- share-usage-data-fn
  [enabled? next-screen]
  (rf/dispatch [:centralized-metrics/toggle-centralized-metrics enabled? true])
  (if next-screen
    (rf/dispatch [:navigate-to-within-stack [next-screen :screen/onboarding.share-usage]]) ;; Onboarding
    (rf/dispatch [:navigate-back]))) ;; Login Screen

(defn view
  []
  (let [insets           (safe-area/get-insets)
        next-screen      (:next-screen (rf/sub [:get-screen-params :screen/onboarding.share-usage]))
        share-usage-data (rn/use-callback #(share-usage-data-fn true next-screen))
        maybe-later      (rn/use-callback #(share-usage-data-fn false next-screen))
        learn-more       (rn/use-callback #(rf/dispatch [:show-bottom-sheet
                                                         {:content learn-more-sheet/view
                                                          :shell?  true}]))]
    [:<>
     [quo/page-nav
      {:margin-top (:top insets)
       :background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back
       :right-side [{:icon-left           :i/info
                     :accessibility-label :learn-more
                     :label               (i18n/label :t/learn-more)
                     :on-press            learn-more}]}]
     [quo/text-combinations
      {:container-style                 style/title-container
       :title                           (i18n/label :t/help-us-improve-status)
       :title-accessibility-label       :share-usage-title
       :description                     (i18n/label :t/collecting-usage-data)
       :description-accessibility-label :share-usage-subtitle}]
     [rn/image
      {:resize-mode :contain
       :style       (style/page-illustration (:width (rn/get-window)))
       :source      (resources/get-image :biometrics)}]
     [rn/view {:style (style/buttons insets)}
      [quo/button
       {:size                40
        :accessibility-label :share-usage-data
        :on-press            share-usage-data}
       (i18n/label :t/agree)]
      [quo/button
       {:size                40
        :accessibility-label :maybe-later-button
        :background          :blur
        :type                :grey
        :on-press            maybe-later
        :container-style     {:margin-top 12}}
       (i18n/label :t/maybe-later)]]]))
