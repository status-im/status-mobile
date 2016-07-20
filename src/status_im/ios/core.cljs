(ns status-im.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(set! js/window.React (js/require "react"))
(set! js/ReactNative (js/require "react-native"))

(def app-registry (.-AppRegistry js/ReactNative))
(def text (r/adapt-react-class (.-Text js/ReactNative)))
(def view (r/adapt-react-class (.-View js/ReactNative)))
(def image (r/adapt-react-class (.-Image js/ReactNative)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight js/ReactNative)))

(def logo-img (js/require "./images/cljs.png"))

(defn alert [title]
      (.alert (.-Alert js/ReactNative) title))

(defn app-root []
  (fn []
    [view {:style {:flex-direction "column" :margin 40 :align-items :center}}
     [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align :center}} "Test"]
     [image {:source logo-img
             :style  {:width 80 :height 80 :margin-bottom 30}}]
     [touchable-highlight {:style    {:background-color "#999" :padding 10 :border-radius 5}
                           :on-press #(alert "HELLO!")}
      [text {:style {:color :white :text-align :center :font-weight "bold"}} "press me"]]]))

(defn init []
      (.registerComponent app-registry "StatusIm" #(r/reactify-component app-root)))
