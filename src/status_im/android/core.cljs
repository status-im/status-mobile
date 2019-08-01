(ns status-im.android.core
  (:require [reagent.core :as reagent]
            [status-im.ui.screens.views :as views]
            [status-im.ui.components.react :as react]
            [status-im.core :as core]
            [status-im.react-native.js-dependencies :as rn-dependencies]))

(defn app-root [_]
  (reagent/create-class
   {:component-will-mount (fn []
                            (.hide react/splash-screen)
                            (.useScreens (rn-dependencies/react-native-screens)))
    :display-name         "root"
    :reagent-render       views/main}))

(defn init []
  (core/init app-root))