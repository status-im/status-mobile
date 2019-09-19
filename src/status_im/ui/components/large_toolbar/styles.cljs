(ns status-im.ui.components.large-toolbar.styles
  (:require [status-im.ui.components.colors :as colors]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.toolbar.styles :as toolbar.styles]
            [status-im.utils.platform :as platform]))

(defonce toolbar-shadow-component-height
  (let [status-bar-height  (get platform/platform-specific :status-bar-default-height)]
    (+ 50 toolbar.styles/toolbar-height (if (zero? status-bar-height) 50 status-bar-height))))

(defonce toolbar-statusbar-height
  (+ (get platform/platform-specific :status-bar-default-height) toolbar.styles/toolbar-height))

(defn minimized-toolbar-fade-in [anim-opacity]
  (animation/timing
   anim-opacity
   {:toValue 1
    :duration 200
    :easing (.-ease (animation/easing))
    :useNativeDriver true}))

(defn minimized-toolbar-fade-out [anim-opacity]
  (animation/timing
   anim-opacity
   {:toValue 0
    :duration 200
    :easing (.-ease (animation/easing))
    :useNativeDriver true}))

(defn- ios-shadow-opacity-anim [scroll-y]
  (if platform/ios?
    (animation/interpolate scroll-y
                           {:inputRange  [0 toolbar-statusbar-height]
                            :outputRange [0 1]
                            :extrapolate "clamp"})
    0))

(defn- android-shadow-elevation-anim [scroll-y]
  (if platform/android?
    (animation/interpolate scroll-y
                           {:inputRange  [0 toolbar-statusbar-height]
                            :outputRange [0 9]
                            :extrapolate "clamp"})
    0))

(defn bottom-border-opacity-anim [scroll-y]
  (animation/interpolate scroll-y
                         {:inputRange  [0 toolbar-statusbar-height]
                          :outputRange [1 0]
                          :extrapolate "clamp"}))

(defn animated-content-wrapper [anim-opacity]
  {:flex           1
   :align-self     :stretch
   :opacity        anim-opacity})

(def minimized-toolbar
  {:z-index   100
   :elevation 9})

(defn flat-list-with-large-header-bottom [scroll-y]
  {:height              16
   :opacity             (bottom-border-opacity-anim scroll-y)
   :border-bottom-width 1
   :border-bottom-color colors/gray-lighter})

(defn flat-list-with-large-header-shadow [window-width scroll-y]
  (cond-> {:flex             1
           :align-self       :stretch
           :position         :absolute
           :height           toolbar-shadow-component-height
           :width            window-width
           :top              (- toolbar-shadow-component-height)
           :shadow-radius    8
           :shadow-offset    {:width 0 :height 2}
           :shadow-opacity   1
           :shadow-color     "rgba(0, 9, 26, 0.12)"
           :elevation        (android-shadow-elevation-anim scroll-y)
           :background-color colors/white}
    platform/ios?
    (assoc :opacity (ios-shadow-opacity-anim scroll-y))))

(def flat-list
  {:z-index -1})
