(ns status-im2.contexts.welcome.welcome
  (:require
   [quo2.core :as quo]
   [quo2.foundations.colors :as colors]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]
   [react-native.core :as rn]
   [react-native.blur :as blur]
   [status-im2.contexts.welcome.style :as style]))

(defn page-title
  []
  [rn/view {:style style/title-container}
   [quo/text
    style/title-text-style
    "Welcome to web3"]
   [quo/text
    style/subtitle-text-style
    "What are you waiting for? Go explore!"]])

(defn navigate-back
  []
  [quo/page-nav
   (merge {:horizontal-description? false
           :one-icon-align-left?    true
           :align-mid?              false
           :page-nav-color          :transparent
           :mid-section             {:type            :text-with-description
                                     :main-text       nil
                                     :description-img nil}
           :left-section {:icon                  :i/close
                          :icon-background-color (colors/theme-colors
                                                  colors/neutral-50
                                                  colors/neutral-40)
                          :on-press              #(rf/dispatch [:navigate-back])}})])

(defn welcome-button
  []
  [rn/view {:style style/welcome-buttons}
   [quo/button
    {:on-press                  #(rf/dispatch [:init-root :shell-stack])
     :type                      :primary
     :accessibility-label       :welcome-button
     :override-background-color colors/danger-60}
    (i18n/label :t/start-using-status)]])

(defn views
  []
  [:<> {:style {:flex     1}}
   [blur/view {:style style/blur-screen-container
               :blur-amount        20
               :blur-type          :x-light
               :overlay-color      :transparent}]
   [rn/view {:style style/welcome-container}
    [navigate-back]
    [page-title]
    [rn/view {:style style/illustration}
     [quo/text
      style/subtitle-text-style
      "Illustration here"]]
    [welcome-button]]])