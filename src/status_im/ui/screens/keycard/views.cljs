(ns status-im.ui.screens.keycard.views
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.keycard.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.react-native.resources :as resources]
            [re-frame.core :as re-frame]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.status-bar.view :as status-bar]))

(defn connection-lost []
  [react/view {:flex             1
               :justify-content  :center
               :align-items      :center
               :background-color colors/gray-transparent-40}
   [react/view {:background-color colors/white
                :height           478
                :width            "85%"
                :border-radius    16
                :flex-direction   :column
                :justify-content  :space-between
                :align-items      :center}
    [react/view {:margin-top 32}
     [react/text {:style {:typography :title-bold
                          :text-align :center}}
      (i18n/label :t/connection-with-the-card-lost)]
     [react/view {:margin-top 16}
      [react/text {:style {:color      colors/gray
                           :text-align :center}}
       (i18n/label :t/connection-with-the-card-lost-text)]]]
    [react/view {:margin-top 16}
     [react/image {:source      (resources/get-image :keycard-connection)
                   :resize-mode :center
                   :style       {:width  200
                                 :height 200}}]]
    [react/view {:margin-bottom 43}
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:keycard.onboarding.connection-lost.ui/cancel-setup-pressed])}
      [react/text {:style {:color      colors/red
                           :text-align :center}}
       (i18n/label :t/cancel-keycard-setup)]]]]])

(defn nfc-on []
  [react/view styles/container
   [toolbar/toolbar
    {:transparent? true
     :style        {:margin-top 32}}
    toolbar/default-nav-back
    nil]
   [react/view {:flex            1
                :flex-direction  :column
                :justify-content :space-between
                :align-items     :center}
    [react/view {:flex-direction :column
                 :align-items    :center}
     [react/view {:margin-top 16}
      [react/text {:style {:typography :header}}
       (i18n/label :t/turn-nfc-on)]]]
    [react/view
     [react/view {:align-items     :center
                  :justify-content :center}
      [react/image {:source (resources/get-image :keycard-nfc-on)
                    :style  {:width  170
                             :height 170}}]]]
    [react/view
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:keycard.onboarding.nfc-on/open-nfc-settings-pressed])}
      [react/text {:style {:font-size     15
                           :line-height   22
                           :color         colors/blue
                           :text-align    :center
                           :margin-bottom 30}}
       (i18n/label :t/open-nfc-settings)]]]]])

(defn loading [title-label]
  [react/view styles/container
   [toolbar/toolbar {:transparent? true
                     :style        {:margin-top 32}}
    nil nil]
   [react/view {:flex            1
                :flex-direction  :column
                :justify-content :space-between
                :align-items     :center}
    [react/view {:flex-direction :column
                 :align-items    :center}
     [react/view {:margin-top 16}
      [react/activity-indicator {:animating true
                                 :size      :large}]]
     [react/view {:margin-top 16}
      [react/text {:style {:typography :header
                           :text-align :center}}
       (i18n/label title-label)]]
     [react/view {:margin-top 16
                  :width      311}
      [react/text {:style {:font-size   15
                           :line-height 22
                           :color       colors/gray
                           :text-align  :center}}
       (i18n/label :t/this-will-take-few-seconds)]]]
    [react/view {:flex            1
                 :align-items     :center
                 :justify-content :center}
     [react/image {:source      (resources/get-image :keycard-phone)
                   :resize-mode :center
                   :style       {:width  160
                                 :height 170}}]
     [react/view {:margin-top 10}
      [react/text {:style {:text-align  :center
                           :color       colors/gray
                           :font-size   15
                           :line-height 22}}
       (i18n/label :t/hold-card)]]]]])

(defn pairing []
  (loading :t/keycard-onboarding-pairing-header))

(defn welcome []
  [react/view {:flex            1
               :justify-content :space-between
               :align-items     :center
               :flex-direction  :column}
   [react/view]
   [react/view {:align-items :center}
    [react/image {:source      (resources/get-image :status-logo)
                  :resize-mode :center
                  :style       {:width  64
                                :height 64}}]
    [react/view {:margin-top 24}
     [react/i18n-text {:style {:typography :header
                               :text-align :center}
                       :key   :welcome-to-status}]]
    [react/view {:margin-top 16}
     [react/i18n-text {:style {:text-align        :center
                               :margin-horizontal 39
                               :color             colors/gray}
                       :key   :welcome-screen-text}]]]
   [react/view {:align-items :center :margin-bottom 52}
    [react/activity-indicator {:size      :large
                               :animating true}]]])
