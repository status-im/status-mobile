(ns status-im.ui.screens.popover.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.animation :as anim]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.utils.platform :as platform]
            [status-im.ui.screens.wallet.signing-phrase.views :as signing-phrase]
            [status-im.ui.screens.wallet.request.views :as request]
            [status-im.ui.screens.profile.user.views :as profile.user]
            [status-im.ui.screens.multiaccounts.recover.views :as multiaccounts.recover]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.ui.screens.biometric.views :as biometric]
            [status-im.ui.components.colors :as colors]))

(defn hide-panel-anim
  [bottom-anim-value alpha-value window-height]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         (- window-height)
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0
                               :duration        500
                               :useNativeDriver true})])))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value {:toValue         0
                                     :useNativeDriver true})
     (anim/timing alpha-value {:toValue         0.4
                               :duration        500
                               :useNativeDriver true})])))

(defn popover-view [popover window-height]
  (let [bottom-anim-value (anim/create-value window-height)
        alpha-value       (anim/create-value 0)
        clear-timeout     (atom nil)
        current-popover   (reagent/atom nil)
        update?           (reagent/atom nil)
        request-close     (fn []
                            (reset! clear-timeout
                                    (js/setTimeout
                                     #(do (reset! current-popover nil)
                                          (re-frame/dispatch [:hide-popover])) 200))
                            (hide-panel-anim
                             bottom-anim-value alpha-value (- window-height))
                            true)
        on-show           (fn []
                            (show-panel-anim bottom-anim-value alpha-value)
                            (when platform/android?
                              (.removeEventListener js-dependencies/back-handler
                                                    "hardwareBackPress"
                                                    request-close)
                              (.addEventListener js-dependencies/back-handler
                                                 "hardwareBackPress"
                                                 request-close)))
        on-hide           (fn []
                            (when platform/android?
                              (.removeEventListener js-dependencies/back-handler
                                                    "hardwareBackPress"
                                                    request-close)))]
    (reagent/create-class
     {:component-will-update
      (fn [_ [_ popover _]]
        (when @clear-timeout (js/clearTimeout @clear-timeout))
        (cond
          @update?
          (do (reset! update? false)
              (on-show))

          (and @current-popover popover)
          (do (reset! update? true)
              (js/setTimeout #(reset! current-popover popover) 600)
              (hide-panel-anim bottom-anim-value alpha-value (- window-height)))

          popover
          (do (reset! current-popover popover)
              (on-show))

          :else
          (do (reset! current-popover nil)
              (on-hide))))
      :component-will-unmount on-hide
      :reagent-render
      (fn []
        (when @current-popover
          (let [{:keys [view style]} @current-popover]
            [react/view {:position :absolute :top 0 :bottom 0 :left 0 :right 0}
             [react/animated-view
              {:style {:flex 1 :background-color colors/black-persist :opacity alpha-value}}]
             [react/animated-view {:style
                                   {:position  :absolute
                                    :height    window-height
                                    :left      0
                                    :right     0
                                    :transform [{:translateY bottom-anim-value}]}}
              [react/touchable-highlight
               {:style    {:flex 1 :align-items :center :justify-content :center}
                :on-press request-close}
               [react/view (merge {:background-color colors/white
                                   :border-radius    16
                                   :margin           32
                                   :shadow-offset    {:width 0 :height 2}
                                   :shadow-radius    8
                                   :shadow-opacity   1
                                   :shadow-color     "rgba(0, 9, 26, 0.12)"}
                                  style)
                [react/touchable-opacity {:active-opacity 1}
                 (cond
                   (vector? view)
                   view

                   (= :signing-phrase view)
                   [signing-phrase/signing-phrase]

                   (= :share-account view)
                   [request/share-address]

                   (= :share-chat-key view)
                   [profile.user/share-chat-key]

                   (= :custom-seed-phrase view)
                   [multiaccounts.recover/custom-seed-phrase]

                   (= :enable-biometric view)
                   [biometric/enable-biometric-popover]

                   (= :secure-with-biometric view)
                   [biometric/secure-with-biometric-popover]

                   (= :disable-password-saving view)
                   [biometric/disable-password-saving-popover]

                   :else
                   [view])]]]]])))})))

(views/defview popover []
  (views/letsubs [popover [:popover/popover]
                  {window-height :height} [:dimensions/window]]
    [popover-view popover window-height]))
