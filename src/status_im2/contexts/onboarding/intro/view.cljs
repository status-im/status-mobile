(ns status-im2.contexts.onboarding.intro.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.intro.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.constants :as constants]
            [status-im2.contexts.syncing.scan-sync-code.view :as scan-sync-code]))

(defn view
  []
  (let [start-top-animation (atom nil)]
    [rn/view {:style style/page-container}
     [background/view false]
     [quo/drawer-buttons
      {:on-init             (fn [start-top-animation-fn reset-top-animation-fn]
                              (reset! start-top-animation start-top-animation-fn)
                              (reset! scan-sync-code/dismiss-animations reset-top-animation-fn))
       :animations-duration constants/onboarding-modal-animation-duration
       :animations-delay    constants/onboarding-modal-animation-delay
       :top-card            {:on-press            (fn []
                                                    (rf/dispatch [:open-modal :sign-in])
                                                    (rf/dispatch [:hide-terms-of-services-opt-in-screen])
                                                    (when @start-top-animation
                                                      (@start-top-animation)))
                             :heading             (i18n/label :t/sign-in)
                             :animated-heading    (i18n/label :t/sign-in-by-syncing)
                             :accessibility-label :already-use-status-button}
       :bottom-card         {:on-press            (fn []
                                                    (rf/dispatch [:navigate-to :new-to-status])
                                                    (rf/dispatch
                                                     [:hide-terms-of-services-opt-in-screen]))
                             :heading             (i18n/label :t/new-to-status)
                             :accessibility-label :new-to-status-button}}
      (i18n/label :t/you-already-use-status)
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
        (i18n/label :t/terms-of-service)]]]]))
