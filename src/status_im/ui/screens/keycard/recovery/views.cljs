(ns status-im.ui.screens.keycard.recovery.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [status-im.keycard.recovery :as keycard.recovery]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as bottom-toolbar]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.keycard.pin.views :as pin.views]
            [status-im.ui.screens.keycard.styles :as styles]
            [status-im.ui.screens.keycard.views :as keycard.views]
            [status-im.utils.core :as utils.core]
            [status-im.utils.gfycat.core :as gfy]
            [status-im.utils.identicon :as identicon])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn intro
  []
  [:<>
   [react/view
    {:flex            1
     :flex-direction  :column
     :justify-content :space-between
     :align-items     :center}
    [react/view
     {:flex-direction :column
      :align-items    :center}

     [react/view
      {:margin-top 16
       :width      311}
      [react/text
       {:style {:typography :header
                :text-align :center}}
       (i18n/label :t/keycard-recovery-intro-header)]]

     [react/view
      {:margin-top 16
       :width      311}
      [react/text
       {:style {:font-size   15
                :line-height 22
                :color       colors/gray
                :text-align  :center}}
       (i18n/label :t/keycard-recovery-intro-text)]]

     [react/view {:margin-top 33}
      [react/touchable-highlight
       {:on-press #(.openURL ^js react/linking
                             constants/keycard-integration-link)}
       [react/view
        {:flex-direction  :row
         :align-items     :center
         :justify-content :center}
        [react/text
         {:style {:text-align :center
                  :color      colors/blue}}
         (i18n/label :t/learn-more-about-keycard)]
        [icons/tiny-icon :tiny-icons/tiny-external
         {:color           colors/blue
          :container-style {:margin-left 5}}]]]]]

    [react/view
     {:align-items     :center
      :justify-content :center}
     [react/image
      {:source (resources/get-image :keycard)
       :style  {:width  144
                :height 114}}]]

    [react/view {:margin-bottom 50}
     [quo/button
      {:on-press #(re-frame/dispatch [:keycard.recovery.intro.ui/begin-recovery-pressed])}
      (i18n/label :t/keycard-recovery-intro-button-text)]]]])

(defview pin
  []
  (letsubs [card-pin      [:keycard/pin]
            status        [:keycard/pin-status]
            error-label   [:keycard/pin-error-label]
            small-screen? [:dimensions/small-screen?]
            retry-counter [:keycard/retry-counter]]
    [react/view styles/container
     [topbar/topbar
      {:navigation {:on-press #(re-frame/dispatch [::keycard.recovery/cancel-pressed])
                    :label    (i18n/label :t/cancel)}
       :title      (when-not (#{:frozen-card :blocked-card} status)
                     (i18n/label :t/step-i-of-n
                                 {:number 2
                                  :step   2}))}]
     (case status
       :frozen-card
       [keycard.views/frozen-card]

       :blocked-card
       [keycard.views/blocked-card]

       [react/view
        {:flex            1
         :flex-direction  :column
         :justify-content :space-between
         :align-items     :center}
        [react/view
         {:flex-direction :column
          :align-items    :center}
         [react/view {:margin-top 16}
          [react/text
           {:style {:typography :header
                    :text-align :center}}
           (i18n/label :t/enter-your-code)]]]
        [pin.views/pin-view
         {:pin           card-pin
          :retry-counter retry-counter
          :small-screen? small-screen?
          :status        status
          :error-label   error-label
          :step          :import-multiaccount}]])]))

(defview pair
  []
  (letsubs [pair-code                    [:keycard-pair-code]
            error                        [:keycard-setup-error]
            {:keys [free-pairing-slots]} [:keycard-application-info]]
    [react/view
     {:flex            1
      :flex-direction  :column
      :justify-content :space-between
      :align-items     :center}
     [react/view
      {:flex-direction :column
       :align-items    :center}
      [react/view {:margin-top 16}
       [react/text
        {:style {:typography :header
                 :text-align :center}}
        (i18n/label :t/enter-pair-code)]]
      [react/view
       {:margin-top  16
        :width       "85%"
        :align-items :center}
       [react/text
        {:style {:color      colors/gray
                 :text-align :center}}
        (i18n/label :t/enter-pair-code-description)]]
      (when free-pairing-slots
        [react/view
         {:align-items :center
          :margin-top  20}
         [react/text
          {:style {:text-align :center
                   :color      (if (> 3 free-pairing-slots) colors/red colors/gray)}}
          (i18n/label :t/keycard-free-pairing-slots {:n free-pairing-slots})]])]
     [react/view
      [react/view
       {:padding         16
        :justify-content :center
        :margin-bottom   100}
       [quo/text-input
        {:on-change-text    #(re-frame/dispatch [:keycard.onboarding.pair.ui/input-changed %])
         :auto-focus        true
         :on-submit-editing #(re-frame/dispatch [:keycard.onboarding.pair.ui/input-submitted])
         :placeholder       (i18n/label :t/pair-code-placeholder)
         :monospace         true}]]
      [react/view
       {:margin-top 5
        :width      250}
       [tooltip/tooltip error]]]
     [bottom-toolbar/toolbar
      {:right
       [quo/button
        {:on-press #(re-frame/dispatch [:keycard.onboarding.pair.ui/next-pressed])
         :disabled (empty? pair-code)
         :type     :secondary
         :after    :main-icon/next}
        (i18n/label :t/pair-card)]}]]))

(defview success
  []
  (letsubs [address            [:keycard-multiaccount-wallet-address]
            whisper-public-key [:keycard-multiaccount-whisper-public-key]]
    [react/view styles/container
     [topbar/topbar {:navigation :none}]
     [react/view
      {:flex            1
       :flex-direction  :column
       :justify-content :space-between
       :align-items     :center}
      [react/view
       {:flex-direction :column
        :align-items    :center}
       [react/view {:margin-top 16}
        [react/text
         {:style {:typography :header
                  :text-align :center}}
         (i18n/label :t/keycard-recovery-success-header)]]]
      [react/view
       {:flex-direction  :column
        :flex            1
        :justify-content :center
        :align-items     :center}
       [react/view
        {:margin-horizontal 16
         :flex-direction    :column}
        [react/view
         {:justify-content :center
          :align-items     :center
          :margin-bottom   11}
         [react/image
          {:source {:uri (identicon/identicon whisper-public-key)}
           :style  {:width         61
                    :height        61
                    :border-radius 30
                    :border-width  1
                    :border-color  colors/black-transparent}}]]
        [react/text
         {:style           {:text-align  :center
                            :color       colors/black
                            :font-weight "500"}
          :number-of-lines 1
          :ellipsize-mode  :middle}
         (gfy/generate-gfy whisper-public-key)]
        [quo/text
         {:style           {:margin-top 4}
          :monospace       true
          :align           :center
          :color           :secondary
          :number-of-lines 1
          :ellipsize-mode  :middle}
         (utils.core/truncate-str address 14 true)]]]
      [react/view {:margin-bottom 50}
       [quo/button {:on-press #(re-frame/dispatch [:keycard.recovery.success/finish-pressed])}
        (i18n/label :t/finish)]]]]))

(defview no-key
  []
  (letsubs [card-state [:keycard-card-state]]
    [react/view styles/container
     [topbar/topbar {:navigation :none}]
     [react/view
      {:flex            1
       :flex-direction  :column
       :justify-content :space-between
       :align-items     :center}
      [react/view
       {:flex-direction :column
        :align-items    :center}
       [react/view {:margin-top 16}
        [react/text
         {:style {:typography :header
                  :text-align :center}}
         (i18n/label :t/keycard-recovery-no-key-header)]]
       [react/view
        {:margin-top  16
         :width       "85%"
         :align-items :center}
        [react/text
         {:style {:color      colors/gray
                  :text-align :center}}
         (i18n/label :t/keycard-recovery-no-key-text)]]]
      [react/view
       {:flex-direction  :column
        :flex            1
        :justify-content :center
        :align-items     :center}
       [react/view
        {:margin-horizontal 16
         :flex-direction    :column}
        [react/view
         {:align-items     :center
          :justify-content :center}
         (if (= card-state :init)
           [react/image
            {:source (resources/get-image :keycard)
             :style  {:width  144
                      :height 114}}]
           [react/image
            {:source (resources/get-image :keycard-empty)
             :style  {:width  165
                      :height 110}}])]]]
      [react/view {:margin-bottom 50}
       [quo/button
        {:test-id  :generate-new-key
         :on-press #(re-frame/dispatch [:keycard.recovery.no-key.ui/generate-key-pressed])}
        (i18n/label :t/generate-new-key)]
       [quo/button
        {:type     :secondary
         :on-press #(re-frame/dispatch [:navigate-back])}
        (i18n/label :t/cancel)]]]]))
