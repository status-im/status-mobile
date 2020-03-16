(ns status-im.ui.screens.keycard.recovery.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.multiaccounts.recover.core :as multiaccounts.recover]
            [status-im.ui.components.react :as react]
            [status-im.hardwallet.recovery :as hardwallet.recovery]
            [status-im.ui.screens.keycard.styles :as styles]
            [status-im.ui.screens.keycard.views :as views]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.core :as utils.core]
            [status-im.ui.screens.hardwallet.pin.views :as pin.views]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.topbar :as topbar]))

(defn intro []
  [react/view styles/container
   [topbar/topbar]
   [react/view {:flex            1
                :flex-direction  :column
                :justify-content :space-between
                :align-items     :center}
    [react/view {:flex-direction :column
                 :align-items    :center}
     [react/view {:margin-top 16
                  :width      311}
      [react/text {:style {:typography :header
                           :text-align :center}}
       (i18n/label :t/keycard-recovery-intro-header)]]
     [react/view {:margin-top 16
                  :width      311}
      [react/text {:style {:font-size   15
                           :line-height 22
                           :color       colors/gray
                           :text-align  :center}}
       (i18n/label :t/keycard-recovery-intro-text)]]
     [react/view {:margin-top 33}
      [react/touchable-highlight {:on-press #(.openURL react/linking "https://keycard.status.im")}
       [react/view {:flex-direction  :row
                    :align-items     :center
                    :justify-content :center}
        [react/text {:style {:text-align :center
                             :color      colors/blue}}
         (i18n/label :t/learn-more-about-keycard)]
        [vector-icons/tiny-icon :tiny-icons/tiny-external {:color           colors/blue
                                                           :container-style {:margin-left 5}}]]]]]
    [react/view
     [react/view {:align-items     :center
                  :justify-content :center}
      [react/image {:source (resources/get-image :keycard)
                    :style  {:width  144
                             :height 114}}]]]
    [react/view {:margin-bottom 50}
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:keycard.recovery.intro.ui/begin-recovery-pressed])}
      [react/view {:background-color colors/blue-light
                   :align-items      :center
                   :justify-content  :center
                   :flex-direction   :row
                   :width            133
                   :height           44
                   :border-radius    10}
       [react/text {:style {:color colors/blue}}
        (i18n/label :t/keycard-recovery-intro-button-text)]]]]]])

(defview pin []
  (letsubs [pin [:hardwallet/pin]
            status [:hardwallet/pin-status]
            error-label [:hardwallet/pin-error-label]
            small-screen? [:dimensions/small-screen?]
            retry-counter [:hardwallet/retry-counter]]
    [react/view styles/container
     [toolbar/toolbar
      {:transparent? true}
      [toolbar/nav-text
       {:handler #(re-frame/dispatch [::hardwallet.recovery/cancel-pressed])
        :style   {:padding-left 21}}
       (i18n/label :t/cancel)]
      [react/text {:style {:color colors/gray}}
       (i18n/label :t/step-i-of-n {:number 2
                                   :step   2})]]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/enter-your-code)]]]
      [pin.views/pin-view
       {:pin           pin
        :retry-counter retry-counter
        :small-screen? small-screen?
        :status        status
        :error-label   error-label
        :step          :import-multiaccount}]]]))

(defview pair []
  (letsubs [pair-code [:hardwallet-pair-code]
            error [:hardwallet-setup-error]
            {:keys [free-pairing-slots]} [:hardwallet-application-info]]
    [react/view styles/container
     [toolbar/toolbar
      {:transparent? true}
      toolbar/default-nav-back
      [react/text {:style {:color colors/gray}}
       (i18n/label :t/step-i-of-n {:number 2
                                   :step   1})]]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/enter-pair-code)]]
       [react/view {:margin-top  16
                    :width       "85%"
                    :align-items :center}
        [react/text {:style {:color      colors/gray
                             :text-align :center}}
         (i18n/label :t/enter-pair-code-description)]]
       (when free-pairing-slots
         [react/view {:align-items :center
                      :margin-top  20}
          [react/text {:style {:text-align :center
                               :color      (if (> 3 free-pairing-slots) colors/red colors/gray)}}
           (i18n/label :t/keycard-free-pairing-slots {:n free-pairing-slots})]])]
      [react/view
       [text-input/text-input-with-label
        {:on-change-text    #(re-frame/dispatch [:keycard.onboarding.pair.ui/input-changed %])
         :auto-focus        true
         :on-submit-editing #(re-frame/dispatch [:keycard.onboarding.pair.ui/input-submitted])
         :placeholder       nil
         :container         {:background-color colors/white}
         :style             {:background-color colors/white
                             :height           24
                             :typography       :header}}]
       [react/view {:margin-top 5
                    :width      250}
        [tooltip/tooltip error]]]
      [react/view {:flex-direction  :row
                   :justify-content :space-between
                   :align-items     :center
                   :width           "100%"
                   :height          86}
       [react/view]
       [react/view {:margin-right 20}
        [components.common/bottom-button
         {:on-press  #(re-frame/dispatch [:keycard.onboarding.pair.ui/next-pressed])
          :label     (i18n/label :t/pair-card)
          :disabled? (empty? pair-code)
          :forward?  true}]]]]]))

(defview success []
  (letsubs [address [:hardwallet-multiaccount-wallet-address]
            whisper-public-key [:hardwallet-multiaccount-whisper-public-key]]
    [react/view styles/container
     [topbar/topbar {:navigation :none}]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/keycard-recovery-success-header)]]]
      [react/view {:flex-direction  :column
                   :flex            1
                   :justify-content :center
                   :align-items     :center}
       [react/view {:margin-horizontal 16
                    :flex-direction    :column}
        [react/view {:justify-content :center
                     :align-items     :center
                     :margin-bottom   11}
         [react/image {:source {:uri (identicon/identicon whisper-public-key)}
                       :style  {:width         61
                                :height        61
                                :border-radius 30
                                :border-width  1
                                :border-color  colors/black-transparent}}]]
        [react/text {:style           {:text-align  :center
                                       :color       colors/black
                                       :font-weight "500"}
                     :number-of-lines 1
                     :ellipsize-mode  :middle}
         (gfy/generate-gfy whisper-public-key)]
        [react/text {:style           {:text-align  :center
                                       :margin-top  4
                                       :color       colors/gray
                                       :font-family "monospace"}
                     :number-of-lines 1
                     :ellipsize-mode  :middle}
         (utils.core/truncate-str address 14 true)]]]
      [react/view {:margin-bottom 50}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:keycard.recovery.success/finish-pressed])}
        [react/view {:background-color colors/blue-light
                     :align-items      :center
                     :justify-content  :center
                     :flex-direction   :row
                     :width            133
                     :height           44
                     :border-radius    10}
         [react/text {:style {:color colors/blue}}
          (i18n/label :t/finish)]]]]]]))

(defview no-key []
  (letsubs [card-state [:hardwallet-card-state]]
    [react/view styles/container
     [topbar/topbar {:navigation :none}]
     [react/view {:flex            1
                  :flex-direction  :column
                  :justify-content :space-between
                  :align-items     :center}
      [react/view {:flex-direction :column
                   :align-items    :center}
       [react/view {:margin-top 16}
        [react/text {:style {:typography :header
                             :text-align :center}}
         (i18n/label :t/keycard-recovery-no-key-header)]]
       [react/view {:margin-top  16
                    :width       "85%"
                    :align-items :center}
        [react/text {:style {:color      colors/gray
                             :text-align :center}}
         (i18n/label :t/keycard-recovery-no-key-text)]]]
      [react/view {:flex-direction  :column
                   :flex            1
                   :justify-content :center
                   :align-items     :center}
       [react/view {:margin-horizontal 16
                    :flex-direction    :column}
        [react/view {:align-items     :center
                     :justify-content :center}
         (if (= card-state :init)
           [react/image {:source (resources/get-image :keycard)
                         :style  {:width  144
                                  :height 114}}]
           [react/image {:source (resources/get-image :keycard-empty)
                         :style  {:width  165
                                  :height 110}}])]]]
      [react/view {:margin-bottom 50}
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:keycard.recovery.no-key.ui/generate-key-pressed])}
        [react/view {:background-color colors/blue-light
                     :align-items      :center
                     :justify-content  :center
                     :flex-direction   :row
                     :width            190
                     :height           44
                     :border-radius    10}
         [react/text {:style {:color colors/blue}}
          (i18n/label :t/generate-new-key)]]]
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:navigate-back])}
        [react/text {:style {:text-align  :center
                             :padding-top 27
                             :color       colors/blue}}
         (i18n/label :t/cancel)]]]]]))
