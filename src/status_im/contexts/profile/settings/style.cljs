(ns status-im.contexts.profile.settings.style
  (:require [quo.foundations.colors :as colors]
            [react-native.platform :as platform]
            [status-im.contexts.shell.jump-to.constants :as jump-to.constants]))

(defn navigation-wrapper
  [{:keys [customization-color inset theme]}]
  {:padding-top      inset
   :background-color (colors/resolve-color customization-color theme 40)})

(def ^:const footer-padding 20)
(def ^:const ios-bottom-offset -10)

(defn footer-container
  [bottom]
  {:padding-horizontal footer-padding
   :padding-top        footer-padding
   :padding-bottom     (+ jump-to.constants/floating-shell-button-height
                          footer-padding
                          (if platform/ios? ios-bottom-offset bottom))})

(defn floating-shell-button-style
  [{:keys [bottom]}]
  {:position :absolute
   :bottom   (if platform/ios?
               (+ bottom ios-bottom-offset)
               0)})
