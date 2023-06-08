(ns status-im2.contexts.onboarding.intro.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.intro.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.constants :as constants]
            [status-im2.contexts.syncing.scan-sync-code.view :as scan-sync-code]
            [react-native.blur :as blur]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]))

(defonce show-blur-overlay? (reagent/atom false))
(defonce overlay-blur-amount (reagent/atom 0))
(defonce blur-anim-opacity-fn (atom nil))
(defonce blur-timer (atom nil))

(defn f-view
  []
  (let [start-top-animation (atom nil)
        blur-opacity (reanimated/use-shared-value 0)]    
    [:<>
     [rn/view {:style style/page-container}
      [background/view false]
      [quo/drawer-buttons
       {:on-init             (fn [start-top-animation-fn reset-top-animation-fn]
                               (reset! start-top-animation start-top-animation-fn)
                               (reset! scan-sync-code/dismiss-animations reset-top-animation-fn))
        :animations-duration constants/onboarding-modal-animation-duration
        :animations-delay    constants/onboarding-modal-animation-delay
        :show-blur-overlay?  @show-blur-overlay?
        :top-card            {:on-press            (fn []
                                                     (rf/dispatch [:open-modal :sign-in])
                                                     (rf/dispatch [:hide-terms-of-services-opt-in-screen])
                                                     (when @start-top-animation
                                                       (@start-top-animation)))
                              :heading             (i18n/label :t/sign-in)
                              :animated-heading    (i18n/label :t/sign-in-by-syncing)
                              :accessibility-label :already-use-status-button}
        :bottom-card         {:on-press            (fn []
                                                     ;(reset! show-blur-overlay? true)
                                                     (reanimated/animate-shared-value-with-delay blur-opacity 1 300 :easing4 300)
                                                     (rf/dispatch [:open-modal :new-to-status])
                                                     (reset! blur-anim-opacity-fn #(reanimated/animate-shared-value-with-timing blur-opacity 0 300 :easing4))
                                                     (reset! overlay-blur-amount 0)
                                                     (reset! blur-timer
                                                             (js/setInterval (fn []
                                                                               (if (< @overlay-blur-amount 20)
                                                                                 (reset! overlay-blur-amount (+ @overlay-blur-amount 1))
                                                                                 (js/clearInterval @blur-timer))) 1))
                                                     (rf/dispatch
                                                      [:hide-terms-of-services-opt-in-screen]))
                              :heading             (i18n/label :t/new-to-status)
                              :accessibility-label :new-to-status-button}}
       [quo/text
        {:style style/plain-text}
        (i18n/label :t/you-already-use-status)]
       [quo/text
        {:style style/text-container}
        [quo/text
         {:style style/plain-text}
         (i18n/label :t/by-continuing-you-accept)]
        [quo/text
         {:on-press #(rf/dispatch [:open-modal :privacy-policy])
          :style    style/highlighted-text}
         (i18n/label :t/terms-of-service)]]]]
     [reanimated/view {:style (reanimated/apply-animations-to-style
                               {:opacity blur-opacity
                                :z-index blur-opacity}
                               {:position :absolute
                                :top      0
                                :left     0
                                :right    0
                                :bottom   300})}
      [blur/view
       {:style {:flex 1
                :background-color colors/neutral-80-opa-80-blur}
        :blur-radius   25
        :blur-type   :transparent
        :blur-amount @overlay-blur-amount}]]]))

(defn view [props]
  [:f> f-view props])
