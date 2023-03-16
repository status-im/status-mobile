(ns status-im2.contexts.onboarding.create-password.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.create-password.style :as style]
            [utils.i18n :as i18n]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.re-frame :as rf]))

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?            true
     :mid-section           {:type :text-only :main-text ""}
     :left-section          {:type                :blur-bg
                             :icon                :i/arrow-left
                             :icon-override-theme :dark
                             :on-press            #(rf/dispatch [:navigate-back])}
     :right-section-buttons [{:type                :blur-bg
                              :icon                :i/info
                              :icon-override-theme :dark
                              :on-press            #(js/alert "Pending")}]}]])

(defn page
  []
  [rn/view {:style style/page-container}
   [navigation-bar]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:size   :heading-1
      :weight :semi-bold
      :style  {:color colors/white}} "Create profile password"]
    [quo/button
     {;:on-press #(rf/dispatch [:navigate-to :enable-biometrics])
      ;:on-press #(rf/dispatch [:generate-and-derive-addresses])
      :on-press #(rf/dispatch
                  [:create-account-and-login
                   {:password     "password"
                    :display-name "display-name"
                    :image-path   "image-path"
                    :color        "ffffff"}])
      :style    {}} (i18n/label :t/continue)]]])

(defn create-password
  []
  [rn/view {:style {:flex 1}}
   [background/view true]
   [page]])
