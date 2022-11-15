(ns quo2.components.drawers.action-drawers
  (:require [react-native.core :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.components.icon :as icon]
            [quo2.foundations.colors :as colors]))

(defn- get-icon-color [danger?]
  (if danger?
    (colors/theme-colors colors/danger-50 colors/danger-60)
    (colors/theme-colors colors/neutral-50 colors/neutral-40)))

(defn action [{:keys [icon
                      label
                      sub-label
                      right-icon
                      danger?
                      on-press] :as action-props}]
  (when action-props
    [rn/touchable-opacity {:on-press on-press}
     [rn/view {:style
               {:height            (if sub-label 56 47)
                :margin-horizontal 20
                :flex-direction    :row}}
      [rn/view {:style
                {:height        20
                 :margin-top    :auto
                 :margin-bottom :auto
                 :margin-right  12
                 :width         20}}
       [icon/icon icon
        {:color (get-icon-color danger?)
         :size  20}]]
      [rn/view
       {:style
        {:flex            1
         :justify-content :center}}
       [text/text
        {:size   :paragraph-1
         :weight :medium
         :style  {:color
                  (when danger?
                    (colors/theme-colors colors/danger-50 colors/danger-60))}}
        label]
       (when sub-label
         [text/text
          {:size  :paragraph-2
           :style {:color
                   (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
          sub-label])]
      (when right-icon
        [rn/view {:style
                  {:height        20
                   :margin-top    :auto
                   :margin-bottom :auto
                   :width         20}}
         [icon/icon right-icon
          {:color (get-icon-color danger?)
           :size  20}]])]]))

(defn divider []
  [rn/view {:style {:border-top-width 1
                    :border-top-color (colors/theme-colors colors/neutral-10 colors/neutral-80)
                    :margin-top       8
                    :margin-bottom    7
                    :align-items      :center
                    :flex-direction   :row}}])

(defn action-drawer [sections]
  [:<> {:style
        {:flex 1}}
   (interpose
    [divider]
    (for [actions sections]
      (map action actions)))])
