(ns status-im.contexts.onboarding.preparing-status.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.resources :as resources]
    [status-im.contexts.onboarding.preparing-status.style :as style]
    [utils.i18n :as i18n]))

(defn title
  []
  [rn/view
   {:style {:margin-top    56
            :height        56
            :margin-bottom 10}}
   [quo/text-combinations
    {:container-style {:margin-horizontal 20
                       :margin-vertical   12}
     :title           (i18n/label :t/preparing-status-for-you)
     :description     (i18n/label :t/hang-in-there)}]])

(defn content
  []
  (let [width (:width (rn/get-window))]
    [rn/image
     {:resize-mode :stretch
      :style       (style/page-illustration width)
      :source      (resources/get-image :preparing-status)}]))

(defn view
  []
  (let [insets (safe-area/get-insets)]
    [rn/view {:style (style/page-container insets)}
     [:<>
      [title]
      [content]]]))
