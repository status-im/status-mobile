(ns status-im.ui.screens.keycard.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.keycard.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.react-native.resources :as resources]
            [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.hardwallet.pin.views :as pin.views]
            [status-im.utils.core :as utils.core]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.components.topbar :as topbar]))

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

;; NOTE(Ferossgp): Seems like it should be in popover
(defn blank []
  [react/view {:flex             1
               :justify-content  :center
               :align-items      :center
               :background-color colors/gray-transparent-40}
   [react/view {:background-color colors/white
                :height           433
                :width            "85%"
                :border-radius    16
                :flex-direction   :column
                :justify-content  :space-between
                :align-items      :center}
    [react/view {:margin-top         32
                 :padding-horizontal 34}
     [react/text {:style {:typography :title-bold
                          :text-align :center}}
      (i18n/label :t/blank-keycard-title)]
     [react/view {:margin-top 16}
      [react/text {:style {:color       colors/gray
                           :line-height 22
                           :text-align  :center}}
       (i18n/label :t/blank-keycard-text)]]]
    [react/view
     [react/image {:source      (resources/get-image :keycard)
                   :resize-mode :center
                   :style       {:width  144
                                 :height 114}}]]
    [react/view {:margin-bottom 32}
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:navigate-back])}
      [react/view {:background-color colors/blue-light
                   :align-items      :center
                   :justify-content  :center
                   :flex-direction   :row
                   :width            133
                   :height           44
                   :border-radius    10}
       [react/text {:style {:color colors/blue}}
        (i18n/label :t/ok-got-it)]]]]]])

;; NOTE(Ferossgp): Seems like it should be in popover
(defn wrong []
  [react/view {:flex             1
               :justify-content  :center
               :align-items      :center
               :background-color colors/gray-transparent-40}
   [react/view {:background-color colors/white
                :height           413
                :width            "85%"
                :border-radius    16
                :flex-direction   :column
                :justify-content  :space-between
                :align-items      :center}
    [react/view {:margin-top         32
                 :padding-horizontal 34}
     [react/text {:style {:typography :title-bold
                          :text-align :center}}
      (i18n/label :t/wrong-keycard-title)]
     [react/view {:margin-top 16}
      [react/text {:style {:color       colors/gray
                           :line-height 22
                           :text-align  :center}}
       (i18n/label :t/wrong-keycard-text)]]]
    [react/view
     [react/image {:source (resources/get-image :keycard-wrong)
                   :style  {:width  255
                            :height 124}}]]
    [react/view {:margin-bottom 32}
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:navigate-back])}
      [react/view {:background-color colors/blue-light
                   :align-items      :center
                   :justify-content  :center
                   :flex-direction   :row
                   :width            133
                   :height           44
                   :border-radius    10}
       [react/text {:style {:color colors/blue}}
        (i18n/label :t/ok-got-it)]]]]]])

(defn unpaired []
  [react/view {:flex             1
               :justify-content  :center
               :align-items      :center
               :background-color colors/gray-transparent-40}
   [react/view {:background-color colors/white
                :height           433
                :width            "85%"
                :border-radius    16
                :flex-direction   :column
                :justify-content  :space-between
                :align-items      :center}
    [react/view {:margin-top         32
                 :padding-horizontal 34}
     [react/text {:style {:typography :title-bold
                          :text-align :center}}
      (i18n/label :t/unpaired-keycard-title)]
     [react/view {:margin-top 16}
      [react/text {:style {:color       colors/gray
                           :line-height 22
                           :text-align  :center}}
       (i18n/label :t/unpaired-keycard-text)]]]
    [react/view
     [react/image {:source (resources/get-image :keycard-wrong)
                   :style  {:width  255
                            :height 124}}]]
    [react/view {:margin-bottom  32
                 :flex-direction :column
                 :align-items    :center}
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:keycard.login.ui/pair-card-pressed])}
      [react/view {:background-color colors/blue-light
                   :align-items      :center
                   :justify-content  :center
                   :flex-direction   :row
                   :width            133
                   :height           44
                   :border-radius    10}
       [react/text {:style {:color colors/blue}}
        (i18n/label :t/pair-this-card)]]]
     [react/view {:margin-top 27}
      [react/touchable-highlight
       {:on-press #(re-frame/dispatch [:keycard.login.ui/dismiss-pressed])}
       [react/text {:style {:color colors/blue}}
        (i18n/label :t/dismiss)]]]]]])

;; NOTE(Ferossgp): Seems like it should be in popover
(defn not-keycard []
  [react/view {:flex             1
               :justify-content  :center
               :align-items      :center
               :background-color colors/gray-transparent-40}
   [react/view {:background-color colors/white
                :height           453
                :width            "85%"
                :border-radius    16
                :flex-direction   :column
                :justify-content  :space-between
                :align-items      :center}
    [react/view {:margin-top 32}
     [react/text {:style {:typography :title-bold
                          :text-align :center}}
      (i18n/label :t/not-keycard-title)]
     [react/view {:margin-top         16
                  :padding-horizontal 38}
      [react/text {:style {:color       colors/gray
                           :line-height 22
                           :text-align  :center}}
       (i18n/label :t/not-keycard-text)]]]
    [react/view {:margin-top  16
                 :align-items :center}
     [react/image {:source (resources/get-image :not-keycard)
                   :style  {:width  144
                            :height 120}}]
     [react/view {:margin-top 40}
      [react/touchable-highlight {:on-press #(.openURL react/linking "https://keycard.status.im")}
       [react/view {:flex-direction  :row
                    :align-items     :center
                    :justify-content :center}
        [react/text {:style {:text-align :center
                             :color      colors/blue}}
         (i18n/label :t/learn-more-about-keycard)]
        [vector-icons/tiny-icon :tiny-icons/tiny-external {:color           colors/blue
                                                           :container-style {:margin-left 5}}]]]]]
    [react/view {:margin-bottom 32}
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:navigate-back])}
      [react/view {:background-color colors/blue-light
                   :align-items      :center
                   :justify-content  :center
                   :flex-direction   :row
                   :width            133
                   :height           44
                   :border-radius    10}
       [react/text {:style {:color colors/blue}}
        (i18n/label :t/ok-got-it)]]]]]])

(defview login-pin []
  (letsubs [pin [:hardwallet/pin]
            enter-step [:hardwallet/pin-enter-step]
            status [:hardwallet/pin-status]
            error-label [:hardwallet/pin-error-label]
            multiple-multiaccounts? [:multiple-multiaccounts?]
            {:keys [key-uid name] :as account} [:multiaccounts/login]
            small-screen? [:dimensions/small-screen?]
            retry-counter [:hardwallet/retry-counter]]
    [react/view styles/container
     [topbar/topbar
      {:accessories [{:icon    :main-icons/more
                      :handler #(re-frame/dispatch [:keycard.login.pin.ui/more-icon-pressed])}]
       :navigation
       {:icon                :main-icons/back
        :accessibility-label :back-button
        :handler             #(re-frame/dispatch [:keycard.login.pin.ui/cancel-pressed])}}]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction  :column
                   :justify-content :center
                   :align-items     :center
                   :height          140}
       [react/view {:margin-horizontal 16
                    :flex-direction    :column}
        [react/view {:justify-content :center
                     :align-items     :center
                     :flex-direction  :row}
         [react/view {:width           (if small-screen? 50 69)
                      :height          (if small-screen? 50 69)
                      :justify-content :center
                      :align-items     :center}
          ;;TODO this should be done in a subscription
          [photos/photo (multiaccounts/displayed-photo account) {:size (if small-screen? 45 61)}]
          [react/view {:justify-content  :center
                       :align-items      :center
                       :width            (if small-screen? 18 24)
                       :height           (if small-screen? 18 24)
                       :border-radius    (if small-screen? 18 24)
                       :position         :absolute
                       :right            0
                       :bottom           0
                       :background-color :white
                       :border-width     1
                       :border-color     colors/black-transparent}
           [react/image {:source (resources/get-image :keycard-key)
                         :style  {:width  (if small-screen? 6 8)
                                  :height (if small-screen? 11 14)}}]]]]
        [react/text {:style           {:text-align  :center
                                       :margin-top  (if small-screen? 8 12)
                                       :color       colors/black
                                       :font-weight "500"}
                     :number-of-lines 1
                     :ellipsize-mode  :middle}
         name]
        [react/text {:style           {:text-align  :center
                                       :margin-top  4
                                       :color       colors/gray
                                       :font-family "monospace"}
                     :number-of-lines 1
                     :ellipsize-mode  :middle}
         (utils.core/truncate-str key-uid 14 true)]]]
      [pin.views/pin-view
       {:pin                     pin
        :retry-counter           retry-counter
        :small-screen?           small-screen?
        :status                  status
        :error-label             error-label
        :step                    enter-step
        :save-password-checkbox? true}]
      [react/view {:margin-bottom (if small-screen? 25 32)}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:multiaccounts.recover.ui/recover-multiaccount-button-pressed])}
        [react/text {:style {:color colors/blue}}
         (i18n/label :t/recover-key)]]]]]))

(defn- more-sheet-content []
  [react/view {:flex 1}
   [list-item/list-item
    {:theme     :action
     :title     :t/create-new-key
     :icon      :main-icons/profile
     :on-press  #(re-frame/dispatch [:multiaccounts.create.ui/get-new-key])}]])

(def more-sheet
  {:content        more-sheet-content
   :content-height 65})
