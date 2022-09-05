(ns quo2.screens.main
  (:require [quo.components.safe-area :as safe-area]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [quo.theme :as theme]
            [quo2.screens.activity-logs :as activity-logs]
            [quo2.screens.button :as button]
<<<<<<< HEAD
            [quo2.screens.community-card-view :as community-card]
            [quo2.screens.context-tags :as context-tags]
            [quo2.screens.group-avatar :as group-avatar] 
=======
            [quo2.screens.context-tags :as context-tags]
            [quo2.screens.group-avatar :as group-avatar]
>>>>>>> e0d9650f8 (Add icons)
            [quo2.screens.counter :as counter]
            [quo2.screens.segmented :as segmented]
            [quo2.screens.info-message :as info-message]
            [quo2.screens.information-box :as information-box]
<<<<<<< HEAD
            [quo2.screens.messages-gap :as messages-gap]
            [quo2.screens.permission-tag :as permission-tag]
            [quo2.screens.status-tags :as status-tags]
            [quo2.screens.tabs :as tabs]
            [quo2.screens.text :as text]
            [quo2.screens.token-overview :as token-overview]
            [quo2.screens.wallet-user-avatar :as wallet-user-avatar]
=======
            [quo2.screens.status-tags :as status-tags]
            [quo2.screens.tabs :as tabs]
            [quo2.screens.text :as text]
            [quo2.screens.user-avatar :as user-avatar]
>>>>>>> e0d9650f8 (Add icons)
            [re-frame.core :as re-frame]))

(def screens [{:name      :quo2-texts
               :insets    {:top false}
               :component text/preview-text}
              {:name      :quo2-wallet-user-avatar
               :insets    {:top false}
               :component wallet-user-avatar/preview-wallet-user-avatar}
              {:name      :quo2-button
               :insets    {:top false}
               :component button/preview-button}
<<<<<<< HEAD
              {:name      :quo2-token-overview
               :insets    {:top false}
               :component token-overview/preview-token-overview}
              {:name      :quo2-icon-avatar
               :insets    {:top false}
               :component icon-avatar/preview-icon-avatar}
=======
              {:name      :quo2-user-avatar
               :insets    {:top false}
               :component user-avatar/preview-user-avatar}
>>>>>>> e0d9650f8 (Add icons)
              {:name      :quo2-status-tags
               :insets    {:top false}
               :component status-tags/preview-status-tags}
              {:name      :quo2-messages-gap
               :insets    {:top false}
               :component messages-gap/preview-messages-gap}
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
               :component information-box/preview-information-box}
              {:name      :quo2-permission-tag
               :insets    {:top false}
               :component permission-tag/preview-permission-tag}
              {:name      :quo2-filter-tags
               :insets    {:top false}
               :component filter-tags/preview-filter-tags}
              {:name      :quo2-community-cards
               :insets    {:top false}
               :component community-card/preview-community-card}])

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
