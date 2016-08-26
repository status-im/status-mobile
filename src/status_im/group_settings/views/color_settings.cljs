(ns status-im.group-settings.views.color-settings
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                icon
                                                touchable-highlight]]
            [status-im.i18n :refer [label]]
            [status-im.group-settings.styles.color-settings :as st]))

(defn close-chat-color-picker []
  (dispatch [:group-settings :show-color-picker false]))

(def all-colors
  (->> [:#a187d5 :#7099e6 :#424874 :#96c0b7
        :#d3b99f :#eb6464 :#6d98ba :#c17767]
       (partition 4)
       (map-indexed vector)))

(defn color-icon [current-color color]
  (let [selected-color? (= (keyword current-color) color)]
    [touchable-highlight
     {:on-press #(dispatch [:set-chat-color color])
      :style    st/color-highlight}
     [view st/color-icon-comtainer
      [view (st/color-item color)
       (when selected-color?
         [icon :ok st/icon-ok])]]]))

(defview color-settings []
  [show-color-picker [:group-settings :show-color-picker]
   current-color [:chat :color]]
  (when show-color-picker
    [view st/color-settings-container
     [view st/label-container
      [text st/label (label :t/change-color)]]
     [touchable-highlight
      {:on-press close-chat-color-picker
       :style st/close-highlight}
      [view st/close-settings-container
       [icon :close_gray st/close-icon]]]
     [view st/all-colors-container
      (for [[idx colors] all-colors]
        ^{:key idx}
        [view st/color-container
         (for [color (take-last 4 colors)]
           ^{:key color}
           [color-icon current-color color])])]]))
