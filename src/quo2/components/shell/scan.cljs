(ns quo2.components.shell.scan
  (:require [quo.react :as react]
            [quo.react-native :as rn]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im.ui.components.react :as rn-comps :refer [open-blur-overlay]]
            [status-im.ui.screens.qr-scanner.views :refer [camera
                                                           on-barcode-read topbar]]
            [status-im.utils.dimensions :as dimensions]
            [status-im.utils.handlers :refer [<sub]]))

(defn corner [outside-border]
  (let [{:keys [width height]} (dimensions/window)]
    [rn/view {:style (merge {:border-color     colors/white
                             :border-width     2
                             :width            (* 0.20 width)
                             :height           (* 0.12 height)
                             :position         :absolute
                             :background-color :transparent}
                            (case outside-border
                              :top-left {:top                    0
                                         :left                   0
                                         :border-top-left-radius 16
                                         :border-right-width 0
                                         :border-bottom-width 0}
                              :top-right {:top                     0
                                          :right                   0
                                          :border-top-right-radius 16
                                          :border-left-width 0
                                          :border-bottom-width 0}
                              :bottom-left {:bottom                    0
                                            :left                      0
                                            :border-bottom-left-radius 16
                                            :border-right-width 0
                                            :border-top-width 0}
                              :bottom-right {:bottom                     0
                                             :right                      0
                                             :border-bottom-right-radius 16
                                             :border-left-width 0
                                             :border-top-width 0}))}]))

(defn- viewfinder [finished-animation? hole-dimensions size flashlight-on?]
  [:f>
   (fn []
     [rn/view {:style {:position        :absolute
                       :align-items     :center
                       :justify-content :center}}
      [rn/view
       [rn/view {:style {:flex-direction :row :justify-content :space-between}}
        [corner :top-left]
        [corner :top-right]]
       [reanimated/view {:style (reanimated/apply-animations-to-style
                                 {:width  size
                                  :height size}
                                ;;  ^^^^^ TODO Border radius not working making edges
                                 {})}
        [rn/touchable-opacity {:style
                               {:width            32
                                :height           32
                                :background-color (if @flashlight-on?
                                                    colors/white
                                                    colors/white-opa-60)
                                :border-radius    10
                                :position         :absolute
                                :justify-content  :center
                                :z-index          10
                                :elevation        10
                                :align-items      :center
                                :bottom           20
                                :right            20}
                               :on-press #(swap! flashlight-on? not)}
         [icons/icon (if @flashlight-on?
                       :main-icons/flashlight-on
                       :main-icons/flashlight-off) {:size 20
                                                    :color colors/neutral-95}]]
        [rn/view {:style {:position :absolute :z-index 0 :elevation 0}}
         [rn/hole-view (cond->
                        {:style {:width            2000
                                 :height           2000
                                 :top              -1000
                                 :left             -1000}}
                         @finished-animation? (assoc :holes [{:x            1000
                                                              :y            1000
                                                              :width        @hole-dimensions
                                                              :height       @hole-dimensions
                                                              :borderRadius 16}]))]]]
       [rn/view {:style {:flex-direction :row :justify-content :space-between :z-index 5
                         :elevation 5}}
        [corner :bottom-left]
        [corner :bottom-right]]]])])

(defn preview
  []
  [:f>
   (fn []
     (let [{:keys [width]}               (dimensions/window)
           {:keys [min-scale max-scale]} {:min-scale (* width 0.4)
                                          :max-scale (* width 0.9)}
           hole-dimensions               (reagent/atom (* width 0.9))
           finished-animation?           (reagent/atom false)
           updateHoleWidth               #(do
                                            (reset! finished-animation? true)
                                            (reset! hole-dimensions (* % 0.9)))
           flashlight-on?                (reagent/atom false)
           size                          (reanimated/use-shared-value min-scale)
           difference                    (- max-scale min-scale)]
       [:<>
        (react/effect!
         (fn []
           (reanimated/animate-shared-value-with-delay size
                                                       max-scale
                                                       difference
                                                       :easing1 0)
           (js/setTimeout #(updateHoleWidth width) 300)
           []))
        
        [rn-comps/blur-overlay {:blurStyle    "light"
                                :radius       14
                                :brightness   -200
                                :customStyles {:align-items :center :justify-content :center :width "100%" :height "100%"}
                                :children     (reagent/as-element [viewfinder finished-animation? hole-dimensions size flashlight-on?])}]]))])

(defn qr-test []
  (let [read-once?             (reagent/atom false)
        camera-flashlight      (<sub [:wallet.send/camera-flashlight])
        opts                   (<sub [:get-screen-params])
        camera-ref             (reagent/atom nil)
        _ (js/setTimeout #(open-blur-overlay) 5000)]
    ;; [rn/view {:style {:z-index          1000
    ;;                   :elevation        1000}}
    ;;  [topbar camera-flashlight opts]
    ;;  [rn/view {:flex 1}
    ;;   [rn-comps/with-activity-indicator
    ;;    {}
    ;;    [camera
    ;;     {:ref            #(reset! camera-ref %)
    ;;      :style          {:flex 1}
    ;;      :camera-options {:zoomMode :off}
    ;;      :scan-barcode   true
    ;;      :on-read-code   #(when-not @read-once?
    ;;                         (reset! read-once? true)
    ;;                         (on-barcode-read opts %))}]]

    ;;    [preview]]] 
    ;; [rn/view {:style {:z-index          1000
    ;;                   :elevation        1000}}
    [:<>
     [topbar camera-flashlight opts]
     [:<>
      [rn-comps/with-activity-indicator
       {}
       [camera
        {:ref            #(reset! camera-ref %)
         :style          {:flex 1}
         :camera-options {:zoomMode :off}
         :scan-barcode   true
         :on-read-code   #(when-not @read-once?
                            (reset! read-once? true)
                            (on-barcode-read opts %))}]]
      [preview]]]))