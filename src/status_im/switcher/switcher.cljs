(ns status-im.switcher.switcher
  (:require [reagent.core :as reagent]
            [quo2.reanimated :as reanimated]
            [quo2.foundations.colors :as colors]
            [status-im.switcher.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.switcher.constants :as constants]
            [status-im.switcher.animation :as animation]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.react-native.resources :as resources]
            [status-im.switcher.switcher-container :as switcher-container]))

(defn switcher-button [view-id toggle-switcher-screen-fn shared-values]
  [:f>
   (fn []
     (let [touchable-original-style       (styles/switcher-button-touchable view-id)
           close-button-original-style    (styles/switcher-close-button)
           switcher-button-original-style (styles/switcher-button)
           touchable-animated-style       (reanimated/apply-animations-to-style
                                           {:transform [{:scale (:button-touchable-scale shared-values)}]}
                                           touchable-original-style)
           close-button-animated-style    (reanimated/apply-animations-to-style
                                           {:opacity (:close-button-opacity shared-values)}
                                           close-button-original-style)
           switcher-button-animated-style (reanimated/apply-animations-to-style
                                           {:opacity (:switcher-button-opacity shared-values)}
                                           switcher-button-original-style)]
       [reanimated/touchable-opacity {:active-opacity 1
                                      :on-press-in    #(animation/switcher-touchable-on-press-in
                                                        (:button-touchable-scale shared-values))
                                      :on-press-out   toggle-switcher-screen-fn
                                      :style          touchable-animated-style}
        [reanimated/view {:style close-button-animated-style}
         [icons/icon :main-icons/close {:color colors/black}]]
        [reanimated/image {:source (resources/get-image :switcher)
                           :style  switcher-button-animated-style}]]))])

(defn switcher-screen [toggle-switcher-screen-fn shared-values]
  [:f>
   (fn []
     (let [switcher-screen-original-style    (styles/switcher-screen)
           switcher-container-original-style (styles/switcher-screen-container)
           switcher-screen-animated-style    (reanimated/apply-animations-to-style
                                              {:width         (:switcher-screen-size shared-values)
                                               :height        (:switcher-screen-size shared-values)
                                               :bottom        (:switcher-screen-bottom shared-values)
                                               :border-radius (:switcher-screen-radius shared-values)}
                                              switcher-screen-original-style)
           switcher-container-animated-style (reanimated/apply-animations-to-style
                                              {:bottom    (:switcher-container-bottom shared-values)
                                               :transform [{:scale (:switcher-container-scale shared-values)}]}
                                              switcher-container-original-style)]
       [reanimated/view {:style switcher-screen-animated-style}
        [react/blur-view (styles/switcher-blur-background)]
        [reanimated/view {:style switcher-container-animated-style}
         [switcher-container/tabs toggle-switcher-screen-fn]]]))])

(defn switcher [view-id]
  [:f>
   (fn []
     (let [switcher-opened?          (reagent/atom false)
           switcher-button-opacity   (reanimated/use-shared-value 1)
           switcher-screen-size      (reanimated/use-shared-value constants/switcher-pressed-size)
           switcher-screen-radius    (animation/switcher-screen-radius switcher-screen-size)
           switcher-screen-bottom    (animation/switcher-screen-bottom-position switcher-screen-radius view-id)
           shared-values             {:switcher-button-opacity   switcher-button-opacity
                                      :switcher-screen-size      switcher-screen-size
                                      :switcher-screen-radius    switcher-screen-radius
                                      :switcher-screen-bottom    switcher-screen-bottom
                                      :button-touchable-scale    (reanimated/use-shared-value 1)
                                      :switcher-container-scale  (reanimated/use-shared-value 0.9)
                                      :close-button-opacity      (animation/switcher-close-button-opacity switcher-button-opacity)
                                      :switcher-container-bottom (animation/switcher-container-bottom-position switcher-screen-bottom)}
           toggle-switcher-screen-fn #(animation/switcher-touchable-on-press-out switcher-opened? view-id shared-values)]
       [:<>
        [switcher-screen toggle-switcher-screen-fn shared-values]
        [switcher-button view-id toggle-switcher-screen-fn shared-values]]))])
