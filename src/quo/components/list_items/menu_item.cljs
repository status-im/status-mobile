(ns quo.components.list-items.menu-item
  (:require
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.context]
    [quo.foundations.colors :as colors :refer [theme-colors]]
    [react-native.core :as rn]))

(defn themes
  [type theme]
  (case type
    :main        {:icon-color (theme-colors colors/neutral-50 colors/neutral-40 theme)
                  :background (theme-colors colors/white colors/neutral-95 theme)
                  :text-color (theme-colors colors/neutral-100 colors/white theme)}
    :danger      {:icon-color (theme-colors colors/danger-50 colors/danger-60 theme)
                  :background (theme-colors colors/white colors/neutral-95 theme)
                  :text-color (theme-colors colors/danger-50 colors/danger-60 theme)}
    :transparent {:icon-color (theme-colors colors/neutral-50 colors/neutral-10 theme)
                  :text-color (theme-colors colors/neutral-100 colors/white theme)}))

(defn menu-item
  [{:keys [type title accessibility-label icon on-press style-props subtitle subtitle-color]
    :or   {type :main}}]
  (let [theme                                      (quo.context/use-theme)
        {:keys [icon-color text-color background]} (themes type theme)]
    [rn/touchable-opacity
     (merge {:accessibility-label accessibility-label
             :style               (merge style-props
                                         {:background-color background
                                          :height           48
                                          :flex-direction   :row
                                          :align-items      :center})}
            (when on-press
              {:on-press on-press}))
     [rn/view
      {:style (cond-> {:flex-direction :row
                       :flex-grow      0
                       :flex-shrink    1
                       :align-items    :center}
                icon (assoc :padding-horizontal 20))}
      [rn/view
       {:style (cond-> {:width           20
                        :height          20
                        :align-items     :center
                        :justify-content :center}
                 icon (assoc :margin-right 12))}
       (when icon [icons/icon icon {:color icon-color}])]
      [rn/view
       [text/text
        {:weight          :medium
         :style           {:color text-color}
         :ellipsize-mode  :tail
         :number-of-lines 1
         :size            :paragraph-1}
        title]
       (when subtitle
         [text/text
          {:weight          :medium
           :style           {:color subtitle-color}
           :ellipsize-mode  :tail
           :number-of-lines 1
           :size            :paragraph-1}
          subtitle])]]]))
