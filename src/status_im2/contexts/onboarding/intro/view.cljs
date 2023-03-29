(ns status-im2.contexts.onboarding.intro.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.intro.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn view
  []
  [rn/view {:style style/page-container}
   [background/view false]
   [quo/drawer-buttons
    {:top-card    {:on-press            (fn []
                                          (rf/dispatch [:navigate-to :sign-in])
                                          (rf/dispatch [:hide-terms-of-services-opt-in-screen]))
                   :heading             (i18n/label :t/sign-in)
                   :accessibility-label :already-use-status-button}
     :bottom-card {:on-press            (fn []
                                          (rf/dispatch [:navigate-to :new-to-status])
                                          (rf/dispatch [:hide-terms-of-services-opt-in-screen]))
                   :heading             (i18n/label :t/new-to-status)
                   :accessibility-label :new-to-status-button}}
    (i18n/label :t/you-already-use-status)
    [quo/text
     {:style style/text-container}
     [quo/text
      {:size   :paragraph-2
       :style  style/plain-text
       :weight :semi-bold}
      (i18n/label :t/by-continuing-you-accept)]
     [quo/text
      {:on-press #(rf/dispatch [:open-modal :privacy-policy])
       :size     :paragraph-2
       :style    style/highlighted-text
       :weight   :semi-bold}
      (i18n/label :t/terms-of-service)]]]])
