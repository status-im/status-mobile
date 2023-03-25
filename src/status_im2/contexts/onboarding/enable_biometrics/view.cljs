(ns status-im2.contexts.onboarding.enable-biometrics.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im2.contexts.onboarding.enable-biometrics.style :as style]
            [utils.i18n :as i18n]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [utils.re-frame :as rf]))

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?  true
     :mid-section {:type :text-only :main-text ""}
    }]])

(defn page
  []
  [rn/view {:style style/page-container}
   [navigation-bar]
   [rn/view {:style {:padding-horizontal 20}}
    [quo/text
     {:size   :heading-1
      :weight :semi-bold
      :style  {:color colors/white}} "Enable-biometrics"]
    [quo/button
     {:on-press       #(rf/dispatch [:onboarding-2/create-account-and-login])
      :type           :grey
      :override-theme :dark
      :style          {}} (i18n/label :t/continue)]]])

(defn enable-biometrics
  []
  [rn/view {:style {:flex 1}}
   [background/view true]
   [page]])
