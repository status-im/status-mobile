(ns status-im.ui.screens.keycard.onboarding.views
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im2.constants :as constants]
            [utils.i18n :as i18n]
            [status-im.keycard.onboarding :as keycard.onboarding]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.checkbox.view :as checkbox]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as bottom-toolbar]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.keycard.pin.views :as pin.views]
            [status-im.ui.screens.keycard.styles :as styles]
            [utils.re-frame :as rf])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defview intro
  []
  (letsubs [flow                                                          [:keycard-flow]
            {:keys [from-key-storage-and-migration? factory-reset-card?]} [:keycard]]
    [react/view
     {:flex        1
      :align-items :center
      :margin-top  (when from-key-storage-and-migration? 80)}
     [react/view {:align-items :center}
      [react/view
       [react/view
        {:align-items     :center
         :justify-content :center
         :margin-top      16}
        [react/image
         {:source (resources/get-image :keycard)
          :style  {:width  120
                   :height 95}}]]]
      [react/view {:margin-top 16}
       [react/text {:style {:typography :header}}
        (i18n/label :t/keycard-onboarding-intro-header)]]
      [react/view
       {:margin-top 16
        :width      311}
       [react/text
        {:style {:font-size   15
                 :line-height 22
                 :color       colors/gray
                 :text-align  :center}}
        (i18n/label :t/keycard-onboarding-intro-text)]]
      [react/view
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
      {:margin-top   16
       :margin-left  24
       :margin-right 24}
      [react/text
       {:style {:typography  :main-medium
                :line-height 22
                :text-align  :left}}
       (i18n/label :t/keycard-onboarding-pin-text)]
      (when (not= flow :recovery)
        [react/text
         {:style {:typography  :main-medium
                  :margin-top  16
                  :line-height 22
                  :text-align  :left}}
         (i18n/label :t/keycard-onboarding-mnemonic-text)])]
     [react/view
      {:style {:flex-direction :row
               :margin-top     24}}
      [checkbox/checkbox
       {:checked?        factory-reset-card?
        :style           {:margin-right 10}
        :on-value-change #(re-frame/dispatch [:keycard.onboarding.intro.ui/factory-reset-card-toggle
                                              %])}]
      [react/text (i18n/label :t/keycard-factory-reset)]]
     [react/view {:margin-top 40}
      [quo/button {:on-press #(re-frame/dispatch [:keycard.onboarding.intro.ui/begin-setup-pressed])}
       (i18n/label :t/begin-set-up)]]]))

(defview puk-code
  []
  (letsubs [secrets [:keycard-secrets]
            steps   [:keycard-flow-steps]
            puk     [:keycard-puk-code]]
    [react/view styles/container
     [topbar/topbar
      {:navigation {:on-press #(re-frame/dispatch [::keycard.onboarding/cancel-pressed])
                    :label    (i18n/label :t/cancel)}
       :title      (i18n/label :t/step-i-of-n
                               {:step   "2"
                                :number steps})}]
     [react/scroll-view
      {:content-container-style {:flex-grow       1
                                 :justify-content :space-between}}
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
          (i18n/label :t/keycard-onboarding-puk-code-header)]]
        [react/view
         {:margin-top 32
          :width      "85%"}
         [react/view
          {:justify-content :center
           :flex-direction  :row}
          [react/view
           {:width             "100%"
            :margin-horizontal 16
            :height            108
            :align-items       :center
            :justify-content   :space-between
            :flex-direction    :column
            :background-color  colors/gray-lighter
            :border-radius     8}
           [react/view
            {:justify-content :center
             :flex            1
             :margin-top      10}
            [react/text
             {:style {:color      colors/gray
                      :text-align :center}}
             (i18n/label :t/puk-code)]]
           [react/view
            {:justify-content :flex-start
             :flex            1}
            [quo/text
             {:color               :link
              :align               :center
              :size                :large
              :monospace           true
              :accessibility-label :puk-code}
             puk]]]]
         [react/view {:margin-top 16}
          [react/text {:style {:color colors/gray}}
           (i18n/label :t/puk-code-explanation)]]
         [react/view
          {:justify-content :center
           :margin-top      32
           :flex-direction  :row}
          [react/view
           {:width             "100%"
            :margin-horizontal 16
            :height            108
            :align-items       :center
            :justify-content   :space-between
            :flex-direction    :column
            :background-color  colors/gray-lighter
            :border-radius     8}
           [react/view
            {:justify-content :center
             :flex            1
             :margin-top      10}
            [react/text
             {:style {:color      colors/gray
                      :text-align :center}}
             (i18n/label :t/pair-code)]]
           [react/view
            {:justify-content :flex-start
             :flex            1}
            [quo/text
             {:color               :link
              :align               :center
              :size                :large
              :monospace           true
              :accessibility-label :pair-code}
             (:password secrets)]]]]
         [react/view {:margin-top 16}
          [react/text {:style {:color colors/gray}}
           (i18n/label :t/pair-code-explanation)]]]]
       [bottom-toolbar/toolbar
        {:right
         [quo/button
          {:type     :secondary
           :after    :main-icon/next
           :on-press #(re-frame/dispatch [:keycard.onboarding.puk-code.ui/next-pressed])}
          (i18n/label :t/next)]}]]]]))

(defview pin
  []
  (letsubs [card-pin      [:keycard/pin]
            enter-step    [:keycard/pin-enter-step]
            status        [:keycard/pin-status]
            error-label   [:keycard/pin-error-label]
            steps         [:keycard-flow-steps]
            small-screen? [:dimensions/small-screen?]
            setup-step    [:keycard-setup-step]]
    [react/view styles/container
     [topbar/topbar
      {:navigation {:on-press #(re-frame/dispatch [::keycard.onboarding/cancel-pressed])
                    :label    (i18n/label :t/cancel)}
       :title      (when-not (= setup-step :loading-keys)
                     (i18n/label :t/step-i-of-n
                                 {:number steps
                                  :step   1}))}]
     [react/view
      {:flex            1
       :flex-direction  :column
       :justify-content :space-between
       :align-items     :center}
      [react/view
       {:flex-direction :column
        :align-items    :center}
       [react/view {:margin-top (if small-screen? 4 16)}
        [react/text
         {:style {:typography :header
                  :text-align :center}}
         (i18n/label (if (= :original enter-step)
                       :t/intro-wizard-title4
                       :t/intro-wizard-title5))]]
       [react/view
        {:margin-top (if small-screen? 8 16)
         :height     (if small-screen? 16 22)}
        (when (= :original enter-step)
          [react/text {:style {:color colors/gray}}
           (i18n/label :t/intro-wizard-text4)])]]
      [pin.views/pin-view
       {:pin           card-pin
        :status        status
        :small-screen? small-screen?
        :error-label   error-label
        :step          enter-step}]
      (when-not (= setup-step :loading-keys)
        [react/view
         {:align-items     :center
          :flex-direction  :column
          :justify-content :center
          :margin-bottom   15}
         [react/text
          {:style {:color              colors/gray
                   :padding-horizontal 40
                   :text-align         :center}}
          (i18n/label :t/you-will-need-this-code)]])]]))

(defview recovery-phrase
  []
  (letsubs [mnemonic [:keycard-mnemonic]]
    [react/view styles/container
     [topbar/topbar
      {:navigation {:on-press #(re-frame/dispatch [::keycard.onboarding/cancel-pressed])
                    :label    (i18n/label :t/cancel)}
       :title      (i18n/label :t/step-i-of-n
                               {:step   "3"
                                :number "3"})}]
     [react/scroll-view
      {:content-container-style {:flex-grow       1
                                 :justify-content :space-between}}
      [react/view
       {:flex-direction :column
        :align-items    :center}
       [react/view {:margin-top 16}
        [react/text
         {:style {:typography :header
                  :text-align :center}}
         (i18n/label :t/keycard-onboarding-recovery-phrase-header)]]
       [react/view
        {:margin-top     16
         :width          "85%"
         :flex-direction :column
         :align-items    :center}
        [react/text
         {:style {:text-align :center
                  :color      colors/gray}}
         (i18n/label :t/keycard-onboarding-recovery-phrase-text)]
        [react/view
         [react/touchable-highlight
          {:on-press #(re-frame/dispatch [:keycard.onboarding.recovery-phrase.ui/learn-more-pressed])}
          [react/text {:style {:color colors/blue}}
           (i18n/label :t/learn-more)]]]]]

      [react/view {:padding-horizontal 24}
       [react/view
        (for [[i row] mnemonic]
          ^{:key (str "row" i)}
          [react/view
           {:flex-direction  :row
            :justify-content :center
            :margin-top      12}
           (for [[i word] row]
             ^{:key (str "word" i)}
             [react/view
              {:flex-direction     :row
               :background-color   colors/gray-lighter
               :padding-horizontal 14
               :padding-vertical   7
               :border-radius      48
               :margin-left        12}
              [react/text {:style {:color colors/gray}}
               (str (inc i) ". ")]
              [react/text {:accessibility-label (str "word" i)}
               word]])])]
       [react/view {:margin-top 24}
        [react/text {:style {:text-align :center}}
         (i18n/label :t/keycard-onboarding-recovery-phrase-description)]]]
      [bottom-toolbar/toolbar
       {:right
        [quo/button
         {:on-press #(re-frame/dispatch [:keycard.onboarding.recovery-phrase.ui/next-pressed])
          :type     :secondary
          :after    :main-icon/next}
         (i18n/label :t/confirm)]}]]]))

(defn recovery-phrase-confirm-word
  []
  (let [word (rf/sub [:keycard-recovery-phrase-word])]
    (fn []
      (let [input-word    (rf/sub [:keycard-recovery-phrase-input-word])
            error         (rf/sub [:keycard-recovery-phrase-confirm-error])
            {:keys [idx]} word]
        [react/keyboard-avoiding-view
         {:style         styles/container
          :ignore-offset true}
         [topbar/topbar
          {:navigation {:on-press            #(re-frame/dispatch [::keycard.onboarding/cancel-pressed])
                        :accessibility-label :cancel-keycard-setup
                        :label               (i18n/label :t/cancel)}
           :title      (i18n/label :t/step-i-of-n
                                   {:step   "3"
                                    :number "3"})}]
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
             (i18n/label :t/keycard-recovery-phrase-confirm-header)]]
           [react/view
            {:margin-top  16
             :align-items :center}
            [react/text
             {:style               {:typography :header
                                    :color      colors/gray
                                    :text-align :center}
              :accessibility-label :word-number}

             (i18n/label :t/word-n {:number (inc idx)})]]]
          [react/view
           {:flex            1
            :padding         16
            :justify-content :center}
           [quo/text-input
            {:on-change-text #(re-frame/dispatch
                               [:keycard.onboarding.recovery-phrase-confirm-word.ui/input-changed %])
             :auto-focus true
             :on-submit-editing #(re-frame/dispatch
                                  [:keycard.onboarding.recovery-phrase-confirm-word.ui/input-submitted])
             :placeholder (i18n/label :t/word-n {:number (inc idx)})
             :auto-correct false
             :accessibility-label :enter-word
             :monospace true}]
           [react/view
            {:margin-top 5
             :width      250}
            [tooltip/tooltip error]]]
          [bottom-toolbar/toolbar
           {:right
            [quo/button
             {:on-press            #(re-frame/dispatch
                                     [:keycard.onboarding.recovery-phrase-confirm-word.ui/next-pressed])
              :accessibility-label :next
              :disabled            (empty? input-word)
              :type                :secondary
              :after               :main-icon/next}
             (i18n/label :t/next)]}]]]))))
