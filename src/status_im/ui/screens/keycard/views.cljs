(ns status-im.ui.screens.keycard.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.screens.keycard.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.react-native.resources :as resources]
            [re-frame.core :as re-frame]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.hardwallet.pin.views :as pin.views]
            [status-im.utils.core :as utils.core]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.identicon :as identicon]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.screens.chat.photos :as photos]))

(defview connection-lost []
  (letsubs [{:keys [card-connected?]} [:keycard]]
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
        [react/text {:style {:color              colors/gray
                             :padding-horizontal 50
                             :text-align         :center}}
         (i18n/label :t/connection-with-the-card-lost-text)]]]
      [react/view {:margin-top 16}
       (if card-connected?
         [react/activity-indicator {:size      :large
                                    :animating true}]
         [react/image {:source      (resources/get-image :keycard-connection)
                       :resize-mode :center
                       :style       {:width  200
                                     :height 200}}])]
      [react/view {:margin-bottom 43}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:keycard.connection-lost.ui/cancel-pressed])}
        [react/text {:style {:color      colors/red
                             :text-align :center}}
         (i18n/label :t/cancel)]]]]]))

(defn connection-lost-setup []
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
       (i18n/label :t/connection-with-the-card-lost-setup-text)]]]
    [react/view {:margin-top 16}
     [react/image {:source      (resources/get-image :keycard-connection)
                   :resize-mode :center
                   :style       {:width  200
                                 :height 200}}]]
    [react/view {:margin-bottom 43}
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:keycard.onboarding.connection-lost-setup.ui/cancel-setup-pressed])}
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
      {:on-press #(re-frame/dispatch [:keycard.login.ui/got-it-pressed])}
      [react/view {:background-color colors/blue-light
                   :align-items      :center
                   :justify-content  :center
                   :flex-direction   :row
                   :width            133
                   :height           44
                   :border-radius    10}
       [react/text {:style {:color colors/blue}}
        (i18n/label :t/ok-got-it)]]]]]])

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
      {:on-press #(re-frame/dispatch [:keycard.login.ui/got-it-pressed])}
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
      {:on-press #(re-frame/dispatch [:keycard.login.ui/got-it-pressed])}
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
            {:keys [address name photo-path]} [:multiaccounts/login]]
    [react/view styles/container
     [toolbar/toolbar
      {:transparent? true
       :style        {:margin-top 32}}
      [toolbar/nav-text
       {:handler #(re-frame/dispatch [:keycard.login.pin.ui/cancel-pressed])
        :style   {:padding-left 21}}
       (i18n/label :t/cancel)]
      [react/text {:style {:color colors/gray}}
       (i18n/label :t/step-i-of-n {:number 2
                                   :step   1})]
      [react/view {:margin-right 20}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:keycard.login.pin.ui/more-icon-pressed])}
        [vector-icons/icon :main-icons/more {:color           colors/black
                                             :container-style {:margin-left 5}}]]]]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center
                  :margin-top      60}
      [react/view {:flex-direction  :column
                   :flex            1
                   :justify-content :center
                   :align-items     :center}
       [react/view {:margin-horizontal 16
                    :flex-direction    :column}
        [react/view {:justify-content :center
                     :align-items     :center
                     :flex-direction  :row}
         [react/view {:width           69
                      :height          69
                      :justify-content :center
                      :align-items     :center}
          [photos/photo photo-path {:size 61}]
          [react/view {:justify-content  :center
                       :align-items      :center
                       :width            24
                       :height           24
                       :border-radius    24
                       :position         :absolute
                       :right            0
                       :bottom           0
                       :background-color :white
                       :border-width     1
                       :border-color     colors/black-transparent}
           [react/image {:source (resources/get-image :keycard-key)
                         :style  {:width  8
                                  :height 14}}]]]]
        [react/text {:style           {:text-align  :center
                                       :margin-top  12
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
         (utils.core/truncate-str address 14 true)]]]
      [pin.views/pin-view
       {:pin         pin
        :status      status
        :error-label error-label
        :step        enter-step}]
      [react/view {:margin-bottom 32}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:keycard.login.ui/recover-key-pressed])}
        [react/text {:style {:color colors/blue}}
         (i18n/label :t/recover-key)]]]]]))

(defview login-connect-card []
  (letsubs [status [:hardwallet/pin-status]
            {:keys [address name photo-path]} [:multiaccounts/login]]
    (let [in-progress? (= status :verifying)]
      [react/view styles/container
       [toolbar/toolbar
        {:transparent? true
         :style        {:margin-top 32}}
        nil
        [react/text {:style {:color colors/gray}}
         (i18n/label :t/step-i-of-n {:number 2
                                     :step   2})]
        [react/view {:margin-right 20}
         [react/touchable-highlight
          {:on-press #(re-frame/dispatch [:keycard.login.pin.ui/more-icon-pressed])}
          [vector-icons/icon :main-icons/more {:color           colors/black
                                               :container-style {:margin-left 5}}]]]]
       [react/view {:flex            1
                    :flex-direction  :column
                    :justify-content :space-between
                    :align-items     :center
                    :margin-top      15}
        [react/view {:flex-direction  :column
                     :justify-content :center
                     :align-items     :center}
         [react/view {:margin-horizontal 16
                      :flex-direction    :column}
          [react/view {:justify-content :center
                       :align-items     :center
                       :flex-direction  :row}
           [react/view {:width           69
                        :height          69
                        :justify-content :center
                        :align-items     :center}
            [photos/photo photo-path {:size 61}]
            [react/view {:justify-content  :center
                         :align-items      :center
                         :width            24
                         :height           24
                         :border-radius    24
                         :position         :absolute
                         :right            0
                         :bottom           0
                         :background-color :white
                         :border-width     1
                         :border-color     colors/black-transparent}
             [react/image {:source (resources/get-image :keycard-key)
                           :style  {:width  8
                                    :height 14}}]]]]
          [react/text {:style           {:text-align  :center
                                         :margin-top  12
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
           (utils.core/truncate-str address 14 true)]]]
        [react/view {:margin-bottom   12
                     :flex            1
                     :align-items     :center
                     :justify-content :center}
         [react/image {:source      (resources/get-image :keycard-phone)
                       :resize-mode :center
                       :style       {:width  200
                                     :height 211}}]
         [react/view {:margin-top 10}
          [react/text {:style {:text-align  :center
                               :color       colors/gray
                               :font-size   15
                               :line-height 22}}
           (i18n/label :t/hold-card)]]]
        [react/view {:margin-bottom 50
                     :height        30}
         (when in-progress?
           [react/activity-indicator {:size      :large
                                      :animating true}])]]])))

(defn- more-sheet-content []
  [react/view {:flex           1
               :flex-direction :row
               :margin-top     18}
   [react/view {:flex 1}
    [list-item/list-item
     {:theme     :action
      :title     :t/create-new-key
      :icon      :main-icons/profile
      :on-press  #(re-frame/dispatch [:keycard.login.ui/create-new-key-pressed])}]
    [list-item/list-item
     {:theme     :action
      :title     :t/add-another-key
      :icon      :main-icons/add
      :on-press  #(re-frame/dispatch [:keycard.login.ui/add-key-pressed])}]]])

(def more-sheet
  {:content        more-sheet-content
   :content-height 149})
