(ns status-im.contexts.browser.components.dapp-bar
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn container
  [& children]
  (into [rn/view
         {:style {:padding            8
                  :background-color   colors/neutral-80
                  :flex-direction     :row
                  :border-radius      20
                  :height             44
                  :margin-horizontal  12
                  :padding-horizontal 8
                  :flex               1
                  :align-items        :center
                  :justify-content    :space-between}}]
        children))

(defn title
  [{:keys [text container-style]}]
  [rn/view
   {:style (merge {:padding-horizontal 2
                   :flex               1
                   :align-items        :center
                   :justify-content    :center}
                  container-style)}
   [quo/text
    {:size            :paragraph-1
     :number-of-lines 1
     :weight          :bold
     :style           {:color colors/white-opa-80}}
    text]])

(defn info-button
  []
  [rn/view
   {:style
    {:width           24
     :height          24
     :align-items     :center
     :justify-content :center}}
   [quo/icon :i/info
    {:size  20
     :color colors/white-opa-80}]])
