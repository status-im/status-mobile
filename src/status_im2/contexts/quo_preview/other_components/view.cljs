(ns status-im2.contexts.quo-preview.other-components.view
  (:require [quo.core :as quo]
            [re-frame.core :as rf]
            [react-native.core :as rn]
            [status-im2.common.floating-button-page.view :as floating-button-page]))

(defn view []
  (fn []
    [rn/view {:margin-top 44 :flex 1
              :border-width 1
              :border-color :green}
     
     [floating-button-page/view
      {:button-height 64
       :blur? false
       :button-props {:container-style     {:z-index 2}
                      :customization-color :army
                      :on-press            #(js/alert "to be implemented")}
       :button-label "Save address"}
      [quo/page-nav
       {:type       :no-title
        :text-align :left
        :right-side []
        :background :blur
        :icon-name  :i/close
        :on-press   #(rf/dispatch [:navigate-back])}]
      [rn/view {:style {:flex 1
                        :height 360
                        }}
       [quo/input {:label "label"
                   :value "value"}]]
      quo/button]
      #_[rn/view {:position :absolute
                :top -44
                :bottom 0
                :left 10
                :right 200
                :height 460
                :border-width 1
                :border-color :purple
                :background-color :yellow
                :z-index 50}]
      ]))
