(ns quo.previews.main
  (:require [quo.previews.header :as header]
            [quo.previews.text :as text]
            [quo.previews.text-input :as text-input]
            [quo.previews.tooltip :as tooltip]
            [quo.previews.button :as button]
            [quo.previews.lists :as lists]
            [quo.previews.bottom-sheet :as bottom-sheet]
            [quo.previews.controls :as controls]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.theme :as theme]
            [quo.previews.icons :as icons]
            [re-frame.core :as re-frame]))

(def screens [{:name      :texts
               :insets    {:top false}
               :component text/preview-text}
              {:name      :tooltip
               :insets    {:top false}
               :component tooltip/preview-tooltip}
              {:name      :text-input
               :insets    {:top false}
               :component text-input/preview-text}
              {:name      :headers
               :insets    {:top false}
               :component header/preview-header}
              {:name      :button
               :insets    {:top false}
               :component button/preview-button}
              {:name      :lists
               :instes    {:top false}
               :component lists/preview}
              {:name      :bottom-sheet
               :insets    {:top false}
               :component bottom-sheet/preview}
              {:name      :controls
               :insets    {:top false}
               :component controls/preview}
              {:name      :icons
               :insets    {:top false}
               :component icons/preview}])

(defn theme-switcher []
  [rn/view {:style {:flex-direction   :row
                    :margin-vertical  8
                    :border-radius    4
                    :background-color (:ui-01 @colors/theme)
                    :border-width     1
                    :border-color     (:ui-02 @colors/theme)}}
   [rn/touchable-opacity {:style    {:padding         8
                                     :flex            1
                                     :justify-content :center
                                     :align-items     :center}
                          :on-press #(theme/set-theme :light)}
    [quo/text "Set light theme"]]
   [rn/view {:width            1
             :margin-vertical  4
             :background-color (:ui-02 @colors/theme)}]
   [rn/touchable-opacity {:style    {:padding         8
                                     :flex            1
                                     :justify-content :center
                                     :align-items     :center}
                          :on-press #(theme/set-theme :dark)}
    [quo/text "Set dark theme"]]])

(defn main-screen []
  [rn/scroll-view {:flex               1
                   :padding-vertical   8
                   :padding-horizontal 16
                   :background-color   (:ui-background @colors/theme)}
   [theme-switcher]
   [rn/view
    (for [{:keys [name]} screens]
      ^{:key name}
      [rn/touchable-opacity {:on-press #(re-frame/dispatch [:navigate-to name])}
       [rn/view {:style {:padding-vertical 8}}
        [quo/text (str "Preview " name)]]])]])

(def main-screens [{:name      :quo-preview
                    :insets    {:top false}
                    :component main-screen}])
