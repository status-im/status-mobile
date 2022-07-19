(ns quo2.screens.main
  (:require [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [quo2.screens.button :as button]
            [quo2.screens.token-overview :as token-overview]
            [quo2.screens.text :as text]
            [quo2.screens.tabs :as tabs]
            [quo2.screens.status-tags :as status-tags]
            [quo2.screens.context-tags :as context-tags]
            [quo2.screens.group-avatar :as group-avatar]
            [quo2.screens.activity-logs :as activity-logs]
            [quo2.screens.token-tag :as token-tag]
            [quo2.screens.counter :as counter]
            [quo2.screens.icon-avatar :as icon-avatar]
            [quo2.screens.segmented :as segmented]
            [quo2.screens.info-message :as info-message]
            [quo2.screens.information-box :as information-box]
            [quo.components.safe-area :as safe-area]
            [quo.core :as quo]))

(def screens [{:name      :quo2-texts
               :insets    {:top false}
               :component text/preview-text}
              {:name      :quo2-button
               :insets    {:top false}
               :component button/preview-button}
<<<<<<< HEAD
<<<<<<< HEAD
              {:name      :quo2-token-overview
               :insets    {:top false}
               :component token-overview/preview-token-overview}
=======
               {:name      :quo2-icon-avatar
=======
              {:name      :quo2-icon-avatar
>>>>>>> adefff9b0 (Linting)
               :insets    {:top false}
               :component icon-avatar/preview-icon-avatar}
>>>>>>> 796182c2f (icon-avatar component)
              {:name      :quo2-status-tags
               :insets    {:top false}
               :component status-tags/preview-status-tags}
              {:name      :quo2-context-tags
               :insets    {:top false}
               :component context-tags/preview-context-tags}
              {:name      :quo2-group-avatar
               :insets    {:top false}
               :component group-avatar/preview-group-avatar}
              {:name      :quo2-activity-logs
               :insets    {:top false}
               :component activity-logs/preview-activity-logs}
              {:name      :quo2-tabs
               :insets    {:top false}
               :component tabs/preview-tabs}
              {:name      :quo2-token-tag
               :insets    {:top false}
               :component token-tag/preview-token-tag}
              {:name      :quo2-segmented
               :insets    {:top false}
               :component segmented/preview-segmented}
              {:name      :quo2-counter
               :insets    {:top false}
               :component counter/preview-counter}
              {:name      :info-message
               :insets    {:top false}
               :component info-message/preview-info-message}
              {:name      :information-box
               :insets    {:top false}
               :component information-box/preview-information-box}])

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
  (fn []
    [safe-area/consumer
     (fn [insets]
       [rn/scroll-view {:flex               1
                        :padding-top        (:top insets)
                        :padding-bottom     8
                        :padding-horizontal 16
                        :background-color   (:ui-background @colors/theme)}
        [theme-switcher]
        [rn/view
         (for [{:keys [name]} screens]
           ^{:key name}
           [rn/touchable-opacity {:on-press #(re-frame/dispatch [:navigate-to name])}
            [rn/view {:style {:padding-vertical 8}}
             [quo/text (str "Preview " name)]]])]])]))

(def main-screens [{:name      :quo2-preview
                    :insets    {:top false}
                    :component main-screen}])
