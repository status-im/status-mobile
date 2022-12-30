(ns status-im.ui.screens.onboarding.welcome.views
  (:require [cljs-bean.core :as bean]
            [quo.core :as quo]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.onboarding.welcome.styles :as styles]))

(defn welcome-image-wrapper
  []
  (let [dimensions (reagent/atom {})]
    (fn []
      [react/view
       {:on-layout (fn [^js e]
                     (reset! dimensions (bean/->clj (-> e .-nativeEvent .-layout))))
        :style     {:align-items     :center
                    :justify-content :center
                    :flex            1}}
       (let [padding    0
             image-size (- (min (:width @dimensions) (:height @dimensions)) padding)]
         [react/image
          {:source      (resources/get-theme-image :welcome)
           :resize-mode :contain
           :style       {:width image-size :height image-size}}])])))

(defn welcome
  []
  [react/view {:style styles/welcome-view}
   [welcome-image-wrapper]
   [react/i18n-text {:style styles/welcome-text :key :welcome-to-status}]
   [react/i18n-text
    {:style styles/welcome-text-description
     :key   :welcome-to-status-description}]
   [react/view {:align-items :center :margin-bottom 50}
    [quo/button
     {:on-press            #(re-frame/dispatch [:welcome-lets-go])
      :accessibility-label :lets-go-button}
     (i18n/label :t/lets-go)]]])