(ns status-im2.contexts.quo-preview.other-components.view
  (:require [quo.core :as quo]
            [re-frame.core :as rf]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [status-im2.common.floating-button-page.view :as floating-button-page]
            [status-im2.common.resources :as resources]))

(defn view []
  (let [button-pressed? (reagent/atom false)
        acc-height      (reagent/atom 300)]
    (fn []
      [rn/view {:margin-top   (safe-area/get-top)
                :flex         1
                :border-width 1
                :border-color :green}

       #_[rn/view {:style {:position :absolute
                         :top      0
                         :bottom   0
                         :left     0
                         :right    0}}

        [rn/image {:style  {:flex 1}
                   :source (resources/get-mock-image :dark-blur-bg)}]]
       #_[rn/view {:style {:position         :absolute
                         :top              10
                         :left             10
                         :right            10
                         :background-color :yellow
                         :height           730
                         :z-index          10}}]

       [floating-button-page/view
        {:blur?  false
         :header [quo/page-nav
                  {:type       :no-title
                   :text-align :left
                   :right-side []
                   :background :blur
                   :icon-name  :i/close
                   :on-press   #(rf/dispatch [:navigate-back])}]
         :footer [quo/button {:container-style     {:z-index 2}
                              :customization-color (if @button-pressed? :army :blue)
                              :on-press            (fn []
                                                     (prn @acc-height)
                                                     (swap! acc-height + 20)
                                                     (swap! button-pressed? not))} ;#(js/alert "to be implemented")
                  "Save address"]
         #_#_:button-props {:container-style     {:z-index 2}
                            :customization-color :army
                            :on-press            #(js/alert "to be implemented")}
         ;:button-label  "Save address"
         }
        [rn/view {:style {:flex     1
                          ;:height   @acc-height
                          :overflow :hidden}}
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]
         [quo/input {:label "label"
                     :value "value"}]]

        ]
       #_[rn/view {:position         :absolute
                   :top              -44
                   :bottom           0
                   :left             10
                   :right            200
                   :height           460
                   :border-width     1
                   :border-color     :purple
                   :background-color :yellow
                   :z-index          50}]
       ])))
