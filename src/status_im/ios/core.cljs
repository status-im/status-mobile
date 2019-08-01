(ns status-im.ios.core
  (:require [reagent.core :as reagent]
            [status-im.react-native.js-dependencies :as rn-dependencies]
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.core :as core]
            [re-frame.core :as re-frame]))

(defn app-root [props]
  (reagent/create-class
   {:component-will-mount (fn []
                            (.hide react/splash-screen)
                            (.useScreens (rn-dependencies/react-native-screens)))
    :component-did-mount (fn [this]
                           (re-frame/dispatch [:set-initial-props (reagent/props this)]))
    :display-name   "root"
    :reagent-render views/main}))

(defn init []
  (core/init app-root))
