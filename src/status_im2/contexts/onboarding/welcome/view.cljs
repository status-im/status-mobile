(ns status-im2.contexts.onboarding.welcome.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im2.contexts.onboarding.common.style :as onboarding-style]
    [status-im2.contexts.onboarding.common.background.view :as background]))

(defn page-title
  []
  [rn/view {:style onboarding-style/title-container}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               onboarding-style/title-text}
    (i18n/label (if false
                  :t/welcome-to-web3
                  :t/welcome-back))]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               onboarding-style/regular-text}
    (i18n/label :t/welcome-to-web3-sub-title)]])


(defn navigation-bar
  [root]
  [quo/page-nav
   {:horizontal-description? false
    :one-icon-align-left?    true
    :align-mid?              false
    :page-nav-color          :transparent
    :left-section            {:icon                  :i/arrow-left
                              :icon-background-color colors/white-opa-5
                              :type                  :shell
                              :on-press              #(rf/dispatch [:init-root root])}}])

(defn view
  []
  [safe-area/consumer
   (fn [insets]
     [rn/view {:style (onboarding-style/page-container insets)}
      [background/view true]
      [navigation-bar :enable-notifications]
      [page-title]
      [rn/view {:style onboarding-style/page-illustration}
       [quo/text
        "Illustration here"]]
      [rn/view {:style (onboarding-style/buttons insets)}
       [quo/button
        {:on-press                  #(rf/dispatch [:init-root :shell-stack])
         :type                      :primary
         :accessibility-label       :welcome-button
         :override-background-color (colors/custom-color :purple 60)}
        (i18n/label :t/start-using-status)]]])])