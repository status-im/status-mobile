(ns status-im.ui2.screens.chat.photo-selector.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo.components.safe-area :as safe-area]
            [quo2.core :as quo2]
            [i18n.i18n :as i18n]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [quo2.components.notifications.info-count :as info-count]
            [react-native.linear-gradient :as linear-gradient]))

(def selected (reagent/atom []))

(defn small-image [item]
  [rn/image {:source {:uri item}
             :style  {:width  56
                      :height 56}}])

(defn bottom-gradient []
  [linear-gradient/linear-gradient
   {:colors [:black :transparent]
    :start  {:x 0 :y 1}
    :end    {:x 0 :y 0}
    :style  {:width    "100%"
             :height   174
             :position :absolute
             :bottom   0}}
   [rn/flat-list {:key-fn     (fn [item] item)
                  :render-fn  small-image
                  :data       @selected
                  :horizontal true
                  }]
   [rn/view {:style {:flex-direction  :row
                     :justify-content :space-between
                     :bottom          40}}
    [quo2/button {:type     :grey
                  :style    {:flex 0.48}
                  :on-press #(println "add text")}
     (i18n/label :t/add-text)]
    [quo2/button {:style    {:flex 0.48}
                  :on-press #(println "test")}
     (i18n/label :t/send)]]
   ])

(defn photo-selector []
  (rf/dispatch [:chat.ui/camera-roll-get-photos 20])
  [:f>
   (fn []
     (let [{window-height :height
            window-width  :width} (rn/use-window-dimensions)
           safe-area          (safe-area/use-safe-area)
           camera-roll-photos (rf/sub [:camera-roll-photos])]
       [rn/view {:style {:height (- window-height (:top safe-area))}}
        [rn/touchable-opacity
         {:on-press #(rf/dispatch [:bottom-sheet/hide])
          :style
          {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
           :width            32
           :height           32
           :border-radius    10
           :justify-content  :center
           :align-items      :center
           :margin-left      20
           :margin-bottom    24}}
         [quo2/icon :i/camera {:color (colors/theme-colors colors/neutral-100 colors/white)}]]
        [rn/view {:style {:flex-direction :row
                          :position       :absolute
                          :align-self     :center}}
         [quo2/text {:weight :medium} (i18n/label :t/recent)]
         [rn/view {:style {:background-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
                           :width            14
                           :height           14
                           :border-radius    7
                           :justify-content  :center
                           :align-items      :center
                           :margin-left      7
                           :margin-top       4}}
          [quo2/icon :i/chevron-down {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]
        [rn/flat-list {:key-fn                  (fn [item] item)
                       :render-fn               (fn [item index]
                                                  [rn/touchable-opacity
                                                   {:active-opacity 1
                                                    :on-press       (fn []
                                                                      (if (some #{item} @selected)
                                                                        (reset! selected (filter #(-> % (not= item)) @selected))
                                                                        (swap! selected conj item)))}
                                                   [rn/image {:source {:uri item}
                                                              :style  {:width         (- (/ window-width 3) 0.67)
                                                                       :height        (/ window-width 3)
                                                                       :margin-left   (when (not= (mod index 3) 0) 1)
                                                                       :margin-bottom 1}}]
                                                   (when (some #{item} @selected)
                                                     [rn/view {:style {:position         :absolute
                                                                       :width            (- (/ window-width 3) 0.67)
                                                                       :height           (/ window-width 3)
                                                                       :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)}}])
                                                   (when (some #{item} @selected)
                                                     [info-count/info-count (+ (.indexOf @selected item) 1) {:width         24
                                                                                                             :height        24
                                                                                                             :border-radius 8
                                                                                                             :top           8
                                                                                                             :right         8}])])
                       :data                    camera-roll-photos
                       :num-columns             3
                       :content-container-style {:width          "100%"
                                                 :padding-bottom 40}
                       :style                   {:border-radius 20}}]
        [bottom-gradient]
        ]))])
