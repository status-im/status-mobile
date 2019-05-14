(ns status-im.android.core
  (:require [reagent.core :as reagent]))

(def react-native (js/require "react-native"))
(def view (reagent/adapt-react-class (.-View react-native)))
(def text (reagent/adapt-react-class (.-Text react-native)))

(def native-modules (.-NativeModules react-native))
(def splash-screen (.-SplashScreen native-modules))

(defn main []
  (reagent/create-class
   {:reagent-render
    (fn []
      [view
       {:width            100
        :height           100
        :background-color :green}
       [text
        "Empty init screen"]])}))

(defn app-root [props]
  (reagent/create-class
   {:component-will-mount (fn []
                            (.hide splash-screen))
    :display-name         "root"
    :reagent-render       main}))

(defn init []
  (.registerComponent (.-AppRegistry react-native)
                      "StatusIm"
                      #(reagent/reactify-component app-root)))
