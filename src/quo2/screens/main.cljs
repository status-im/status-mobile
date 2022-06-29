(ns quo2.screens.main
  (:require [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [quo2.screens.button :as button]
            [quo2.screens.text :as text]
            [quo2.screens.tabs :as tabs]
            [quo2.screens.status-tags :as status-tags]
            [quo2.screens.counter :as counter]
            [quo2.screens.segmented :as segmented]
            [quo.core :as quo]))

(def screens [{:name      :quo2-texts
               :insets    {:top false}
               :component text/preview-text}
              {:name      :quo2-button
               :insets    {:top false}
               :component button/preview-button}
              {:name      :quo2-status-tags
               :insets    {:top false}
               :component status-tags/preview-status-tags}
              {:name      :quo2-tabs
               :insets    {:top false}
               :component tabs/preview-tabs}
              {:name      :quo2-segmented
               :insets    {:top false}
               :component segmented/preview-segmented}
              {:name      :quo2-counter
               :insets    {:top false}
               :component counter/preview-counter}])

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

(def main-screens [{:name      :quo2-preview
                    :insets    {:top false}
                    :component main-screen}])
