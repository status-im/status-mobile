(ns quo2.components.drawers.action-drawers
  (:require  [status-im.ui.components.react :as react]
             [quo.react-native :as rn]
             [quo2.components.markdown.text :as text]
             [quo2.components.icon :as icon]
             [quo2.foundations.colors :as colors]))

(defn- get-icon-color [section]
  (if (= section :bottom)
    (colors/theme-colors colors/danger-50 colors/danger-60)
    (colors/theme-colors colors/neutral-50 colors/neutral-40)))

(defn action [section]
  (fn [{:keys [icon label sub-label right-icon on-press]}]
    [rn/touchable-opacity {:on-press on-press}
     [react/view {:style
                  {:flex 1
                   :height (if sub-label 56 47)
                   :margin-horizontal 20
                   :flex-direction :row}}
      [react/view {:style
                   {:height 20
                    :margin-top :auto
                    :margin-bottom :auto
                    :margin-right 12
                    :width 20}}
       [icon/icon icon
        {:color (get-icon-color section)
         :size 20}]]
      [react/view
       {:style
        {:flex 1
         :justify-content :center}}
       [text/text
        {:size :paragraph-1
         :weight :medium
         :style
         {:color (when (= section :bottom)
                   (colors/theme-colors colors/danger-50 colors/danger-60))}}
        label]
       (when sub-label [text/text
                        {:size :paragraph-2
                         :style
                         {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
                        sub-label])]
      (when right-icon
        [react/view {:style
                     {:height 20
                      :margin-top :auto
                      :margin-bottom :auto
                      :width 20}}
         [icon/icon right-icon
          {:color (get-icon-color section)
           :size 20}]])]]))

(defn action-drawer [{:keys [actions actions-with-consequence]}]
  [:<> {:style
        {:flex 1}}
   (map (action :top) actions)
   (when actions-with-consequence
     [:<>
      [rn/view {:style {:border-top-width 1
                        :border-top-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
                        :margin-top       8
                        :margin-bottom    7
                        :align-items      :center
                        :flex-direction   :row}}]
      (map (action :bottom) actions-with-consequence)])])
