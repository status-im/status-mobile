(ns status-im.keycard.test-menu
  (:require [status-im.ui.components.react :as react]
            [status-im.keycard.simulated-keycard :as simulated-keycard]
            [reagent.core :as reagent]
            [status-im.utils.random :as random]))

(def ids (reagent/atom []))

(defn button [label accessibility-label handler]
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

(defn test-menu-view [id]
  (fn []
    (println id @ids)
    (when (= id (last @ids))
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
       [button "res"  :keycard-reset-state simulated-keycard/reset-state]])))

(defn test-menu []
  (let [id (random/id)]
    (reagent/create-class
     {:component-did-mount (fn [] (swap! ids conj id))
      :component-will-unmount (fn [] (reset! ids (vec (remove #(= % id) @ids))))
      :reagent-render (test-menu-view id)})))
