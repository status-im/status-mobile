(ns status-im.ui2.screens.chat.photo-selector.view
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [quo.components.safe-area :as safe-area]
            [quo2.core :as quo2]
            [i18n.i18n :as i18n]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]))

(defn selected (reagent/atom []))

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
                                                  {:on-press (fn []
                                                               (if (contains? @selected item)
                                                                 (reset! )
                                                                 (swap! selected conj item)))}
                                                  [rn/image {:source {:uri item}
                                                             :style  {:width (- (/ window-width 3) 0.67)
                                                                      :height (/ window-width 3)
                                                                      :margin-left (when (not= (mod index 3) 0) 1)
                                                                      :margin-bottom 1}}]
                                                  [rn/view {:style {:position :absolute
                                                                    :width (- (/ window-width 3) 0.67)
                                                                    :height (/ window-width 3)
                                                                    :background-color (colors/theme-colors colors/white-opa-40 colors/neutral-80-opa-40)}}]]
                                                  )
                       :data                    camera-roll-photos
                       :num-columns             3
                       :content-container-style {:width "100%"
                                                 :padding-bottom 40}
                       :style {:border-radius 20}}]
        ]))])
