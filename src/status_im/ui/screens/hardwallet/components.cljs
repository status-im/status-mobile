(ns status-im.ui.screens.hardwallet.components
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as reagent]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.screens.hardwallet.setup.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [re-frame.core :as re-frame]))

(defview application-info [visible?]
  (letsubs [info [:hardwallet-application-info]
            error [:hardwallet-application-info-error]]
    [react/modal {:visible          @visible?
                  :transparent      false
                  :on-request-close #()}
     [react/view {:padding         20
                  :justify-content :center}
      [react/text {:style {:font-size   18
                           :font-weight "700"}}
       "Application info"]
      [react/view {:margin-top 20}
       (if-not error
         (for [[k v] info]
           ^{:key k} [react/text (str k " " v)])
         [react/text "Applet is not installed"])]
      [react/touchable-highlight
       {:on-press #(reset! visible? false)}
       [react/view {:align-items :center
                    :text-style  :underline
                    :margin-top  48}
        [react/text {:style {:font-size 18}}
         "Close window"]]]]]))

(def step-name
  {:pin                           {:label     :t/pin-code
                                   :number    1
                                   :next-step :preparing}
   :preparing                     {:label     :t/initialization
                                   :number    2
                                   :next-step :secret-keys}
   :secret-keys                   {:label     :t/puk-and-pair-codes
                                   :number    3
                                   :next-step :pairing}
   :pairing                       {:label     :t/pairing
                                   :number    4
                                   :next-step :recovery-phrase}
   :card-ready                    {:label     :t/pairing
                                   :number    4
                                   :next-step :recovery-phrase}
   :generating-mnemonic           {:label  :t/recovery-phrase
                                   :number 5}
   :recovery-phrase-confirm-word1 {:label  :t/recovery-phrase
                                   :number 5}
   :recovery-phrase-confirm-word2 {:label  :t/recovery-phrase
                                   :number 5}
   :loading-keys                  {:label  :t/recovery-phrase
                                   :number 5}
   :recovery-phrase               {:label  :t/recovery-phrase
                                   :number 5}})

(defn- setup-steps [step]
  (let [current-step (step-name step)
        {current-label :label current-number :number second-step :next-step} current-step
        {second-label :label second-number :number third-step :next-step} (step-name second-step)
        {third-label :label third-number :number} (step-name third-step)]
    (if current-label
      [react/view styles/setup-steps-container
       [react/text {:style styles/maintain-card-current-step-text}
        (str current-number ". " (i18n/label current-label))]
       (when second-label
         [react/text {:style styles/maintain-card-second-step-text}
          (str second-number ". " (i18n/label second-label))])
       (when third-label
         [react/view {:flex 1}
          [react/text {:style           styles/maintain-card-third-step-text
                       :number-of-lines 1}
           (str third-number ". " (i18n/label third-label))]])]
      [react/text {:style           styles/maintain-card-text
                   :number-of-lines 2}
       (i18n/label :t/maintain-card-to-phone-contact)])))

(defn maintain-card [step]
  (let [modal-visible? (reagent/atom false)
        animation-value (animation/create-value 0)
                                        ;TODO(dmitryn): make animation smoother
        interpolate-fn (fn [output-range]
                         (animation/interpolate animation-value
                                                {:inputRange  [0 0.25 0.5 0.75 1]
                                                 :outputRange output-range}))]
    (reagent/create-class
     {:component-did-mount (fn []
                             (-> animation-value
                                 (animation/timing {:toValue         1
                                                    :duration        1000
                                                    :useNativeDriver true})
                                 (animation/anim-loop)
                                 (animation/start)))
      :display-name        "maintain-card"
      :reagent-render      (fn [step] [react/view styles/maintain-card-container
                                       [react/view styles/hardwallet-icon-container
                                        [vector-icons/icon :main-icons/keycard {:color colors/blue}]
                                        [vector-icons/icon :icons/indicator-small {:color           colors/blue
                                                                                   :container-style (styles/hardwallet-icon-indicator-small-container
                                                                                                     (interpolate-fn [0 0.5 1 0.5 0]))}]
                                        [vector-icons/icon :icons/indicator-middle {:color           colors/blue
                                                                                    :container-style (styles/hardwallet-icon-indicator-middle-container
                                                                                                      (interpolate-fn [1 0.4 0 0.4 0.8]))}]
                                        [vector-icons/icon :icons/indicator-big {:color           colors/blue
                                                                                 :container-style (styles/hardwallet-icon-indicator-big-container
                                                                                                   (interpolate-fn [0.5 0.8 0.5 0.8 0.4]))}]]
                                       [setup-steps step]
                                       [application-info modal-visible?]])})))

(defn wizard-step [step-number]
  (when step-number
    [react/text {:style styles/wizard-step-text}
     (i18n/label :wizard-step {:current step-number
                               :total   5})]))
