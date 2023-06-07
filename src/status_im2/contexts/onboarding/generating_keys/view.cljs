(ns status-im2.contexts.onboarding.generating-keys.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.onboarding.generating-keys.style :as style]
            [status-im2.contexts.onboarding.common.navigation-bar.view :as navigation-bar]
            [utils.i18n :as i18n]
            [status-im2.common.resources :as resources]
            [status-im2.common.parallax.view :as parallax]
            [status-im2.contexts.onboarding.common.background.view :as background]))

(defn page-title
  []
  [quo/title
   {:title                       (i18n/label :t/generating-keys)
    :title-accessibility-label    :generating-keys-title}])

(defn generating-keys
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [background/view true]
     [parallax/video
      {:layers (resources/get-parallax-video :generate-keys)
       :disable-parallax? true}]
     [rn/view
      [navigation-bar/navigation-bar {:disable-back-button? true}]
      [page-title]]]))


