(ns react-native.blur
  (:require ["@react-native-community/blur" :as blur]
            [react-native.platform :as platform]
            [reagent.core :as reagent]
            [react-native.core :as rn]))

(def view (reagent/adapt-react-class (.-BlurView blur)))

;; blur view is currently not working correctly on Android.
(def ios-view (if platform/ios? view rn/view))
