(ns legacy.status-im.keycard.test-menu
  (:require
    [legacy.status-im.keycard.simulated-keycard :as simulated-keycard]
    [legacy.status-im.ui.components.react :as react]))

(defn button
  [label accessibility-label handler]
  [react/view
   {:style {:width           20
            :height          30
            :justify-content :center
            :align-items     :center}}
   [react/text
    {:on-press            handler
     :style               {:font-size 8}
     :accessibility-label accessibility-label}
    label]])

(defn test-menu
  []
  [react/view
   {:style {:position        :absolute
            :top             70
            :right           0
            :width           50
            :justify-content :center
            :align-items     :center}}
   [button "conn" :connect-card simulated-keycard/connect-card]
   [button "conn sell" :connect-selected-card simulated-keycard/connect-selected-card]
   [button "pair" :connect-pairing-card simulated-keycard/connect-pairing-card]
   [button "disc" :disconnect-card simulated-keycard/disconnect-card]
   [button "res" :keycard-reset-state simulated-keycard/reset-state]])
