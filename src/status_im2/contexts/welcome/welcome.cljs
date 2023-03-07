(ns status-im2.contexts.welcome.welcome
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [react-native.core :as rn]
    [status-im2.contexts.welcome.style :as style]
    [status-im2.contexts.onboarding.common.background :as background]
    [status-im2.contexts.onboarding.common.page-nav :as page-nav]))

(defn page-title
  []
  [rn/view {:style style/title-container}
   [quo/text (merge {:style {:color colors/white}}
                    style/title-text-style) 
    (i18n/label :t/welcome-to-web3)]
   [quo/text (merge {:style {:color colors/white}}
                    style/subtitle-text-style) 
    (i18n/label :t/welcome-to-web3-sub-title)]])

(defn welcome-button
  []
  [rn/view {:style style/welcome-buttons}
   [quo/button
    {:on-press                  #(rf/dispatch [:init-root :shell-stack])
     :type                      :primary
     :accessibility-label       :welcome-button
     :override-background-color (colors/custom-color :magenta 60)}
    (i18n/label :t/start-using-status)]])

(defn views
  []
  [:<>
   [rn/view {:style style/blur-screen-container}
    [background/view true]]
   [rn/view {:style style/welcome-container}
    [page-nav/navigate-back]
    [page-title]
    [rn/view {:style style/illustration}
     [quo/text
      style/subtitle-text-style
      "Illustration here"]]
    [welcome-button]]])