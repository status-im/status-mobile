(ns status-im.ui.screens.hardwallet.settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components.common]))

(defn- action-row [{:keys [icon label on-press color-theme]}]
  [react/touchable-highlight
   {:on-press on-press}
   [react/view {:flex-direction :row
                :margin-top     15}
    [react/view {:background-color (case color-theme
                                     :red colors/red-transparent-10
                                     colors/blue-light)
                 :width            40
                 :height           40
                 :border-radius    50
                 :align-items      :center
                 :justify-content  :center}
     [vector-icons/icon icon {:color (case color-theme
                                       :red colors/red
                                       colors/blue)}]]
    [react/view {:align-items     :center
                 :justify-content :center
                 :margin-left     16}
     [react/text {:style {:font-size 17
                          :color     (case color-theme
                                       :red colors/red
                                       colors/blue)}}
      (i18n/label label)]]]])

(defn- activity-indicator [loading?]
  (when loading?
    [react/view {:margin-top 35}
     [react/activity-indicator {:animating true
                                :size      :large}]]))

(defn- reset-card-next-button [disabled?]
  [react/view {:margin-right  18
               :margin-bottom 15}
   [components.common/bottom-button
    {:on-press   #(re-frame/dispatch [:keycard-settings.ui/reset-card-next-button-pressed])
     :disabled?  disabled?
     :forward?   true}]])

(defview reset-card []
  (letsubs [disabled? [:keycard-reset-card-disabled?]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/reset-card)]
     [react/view {:flex             1
                  :background-color :white}
      [react/view {:margin-top  71
                   :flex        1
                   :align-items :center}
       [react/image {:source (:warning-sign resources/ui)
                     :style  {:width  160
                              :height 160}}]]
      [react/view {:flex               1
                   :padding-horizontal 30}
       [react/text {:style {:typography :header
                            :text-align :center}}
        (i18n/label :t/reset-card-description)]
       [activity-indicator disabled?]]
      [react/view {:flex-direction   :row
                   :justify-content  :space-between
                   :align-items      :center
                   :width            "100%"
                   :height           68
                   :border-top-width 1
                   :border-color     colors/gray-light}
       [react/view {:flex 1}]
       [reset-card-next-button disabled?]]]]))

(defn- card-blocked []
  [react/view
   [react/text {:style {:font-size          20
                        :text-align         :center
                        :padding-horizontal 40}}
    (i18n/label :t/keycard-blocked)]])

(defview keycard-settings []
  (letsubs [paired-on [:keycard-paired-on]
            puk-retry-counter [:hardwallet/puk-retry-counter]
            pairing [:keycard-account-pairing]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/status-keycard)]
     [react/view {:flex             1
                  :background-color :white}
      [react/view {:margin-top  47
                   :flex        1
                   :align-items :center}
       [react/image {:source (:hardwallet-card resources/ui)
                     :style  {:width  255
                              :height 160}}]
       (when paired-on
         [react/view {:margin-top 27}
          [react/text
           (i18n/label :t/linked-on {:date paired-on})]])]
      [react/view {:margin-left    16
                   :flex           1
                   :width          "90%"
                   :flex-direction :column}
       (if (zero? puk-retry-counter)
         [card-blocked]
         [react/view
          [action-row {:icon     :main-icons/info
                       :label    :t/help-capitalized
                       :on-press #(.openURL react/linking "https://hardwallet.status.im")}]
          (when pairing
            [react/view
             [action-row {:icon     :main-icons/add
                          :label    :t/change-pin
                          :on-press #(re-frame/dispatch [:keycard-settings.ui/change-pin-pressed])}]
             [action-row {:icon     :main-icons/close
                          :label    :t/unpair-card
                          :on-press #(re-frame/dispatch [:keycard-settings.ui/unpair-card-pressed])}]])])]
      (when pairing
        [react/view {:margin-bottom 35
                     :margin-left   16}
         [action-row {:icon        :main-icons/logout
                      :color-theme :red
                      :label       :t/reset-card
                      :on-press    #(re-frame/dispatch [:keycard-settings.ui/reset-card-pressed])}]])]]))
