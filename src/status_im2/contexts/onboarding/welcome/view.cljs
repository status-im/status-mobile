(ns status-im2.contexts.onboarding.welcome.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [status-im2.contexts.onboarding.welcome.style :as style]
    [status-im2.contexts.onboarding.common.background.view :as background]))

(defn page-title
  []
  [rn/view {:style style/title-container}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               {:color colors/white}}
    (i18n/label :t/welcome-to-web3)]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
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
  [rn/view {:style style/welcome-container}
   [background/view true]
   [navigation-bar :enable-notifications]
   [page-title]
   [rn/view {:style style/page-illustration}
    [quo/text
     "Illustration here"]]
   [quo/button
    {:on-press                  #(rf/dispatch [:init-root :shell-stack])
     :type                      :primary
     :accessibility-label       :welcome-button
     :override-background-color (colors/custom-color :magenta 60)
     :style                     {:margin 20}}
    (i18n/label :t/start-using-status)]])