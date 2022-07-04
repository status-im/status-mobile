(ns quo2.screens.alert
  (:require [quo.react-native :as rn]))

(def dummy-buttons [{:text "button1"
                     :on-press #(prn "pressed")
                     :on-dismiss #(prn "dismissed")
                     :cancelable false}
                    {:text "button2"
                     :on-press #(prn "pressed")
                     :on-dismiss #(prn "dismissed")
                     :cancelable false}])

(def dummy-title "Hey there")

(def dummy-description "description")

(defn alert
  "An alert component, just like the react-native one.
   title and description are strings and
   buttons is a clj array of maps that contains
   :keys [text on-press on-dismiss and cancelable]"
  [title description buttons] 
    [rn/view 
     (rn/alert title description buttons)])
