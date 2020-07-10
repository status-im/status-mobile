(ns status-im.keycard.test-menu
  (:require [status-im.ui.components.react :as react]
            [status-im.keycard.simulated-keycard :as simulated-keycard]))

(defn button [label accessibility-label handler]
  [react/view
   {:style {:width           50
            :height          40
            :justify-content :center
            :align-items     :center}}
   [react/text
    {:on-press            handler
     :accessibility-label accessibility-label}
    label]])

(defn test-menu []
  [react/view
   {:style {:position        :absolute
            :top             100
            :right           0
            :width           50
            :justify-content :center
            :align-items     :center}}
   [button "conn" :connect-card simulated-keycard/connect-card]
   [button "conn sell" :connect-selected-card simulated-keycard/connect-selected-card]
   [button "pair" :connect-pairing-card simulated-keycard/connect-pairing-card]
   [button "disc" :disconnect-card simulated-keycard/disconnect-card]
   [button "res"  :keycard-reset-state simulated-keycard/reset-state]])

