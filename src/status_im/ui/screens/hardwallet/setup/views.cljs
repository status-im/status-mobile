(ns status-im.ui.screens.hardwallet.setup.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ui.screens.hardwallet.components :as components]
            [status-im.ui.screens.hardwallet.pin.views :as pin.views]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.common.common :as components.common]
            [status-im.react-native.resources :as resources]
            [status-im.ui.screens.hardwallet.setup.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(defn secret-keys []
  [react/view styles/secret-keys-container
   [react/view styles/secret-keys-inner-container
    [react/view styles/secret-keys-title-container
     [components/wizard-step 2]
     [react/text {:style           styles/secret-keys-title-text
                  :number-of-lines 2
                  :font            :bold}
      (i18n/label :t/write-down-and-store-securely)]]
    [react/text {:style styles/puk-code-title-text
                 :font  :bold}
     (i18n/label :t/puk-code)]
    [react/text {:style styles/puk-code-explanation-text}
     (i18n/label :t/puk-code-explanation)]
    [react/view styles/puk-code-numbers-container
     [react/view styles/puk-code-numbers-inner-container
      [react/text {:style styles/puk-code-text
                   :font  :bold}
       "1234 5678 9123"]]]
    [react/text {:style styles/puk-code-title-text
                 :font  :bold}
     (i18n/label :t/pair-code)]
    [react/text {:style           styles/puk-code-explanation-text
                 :number-of-lines 2}
     (i18n/label :t/pair-code-explanation)]
    [react/view styles/puk-code-numbers-container
     [react/view styles/puk-code-numbers-inner-container
      [react/text {:style styles/puk-code-text
                   :font  :bold}
       "a12k52kh0x"]]]]
   [react/view styles/next-button-container
    [react/view components.styles/flex]
    [components.common/bottom-button
     {:on-press #(re-frame/dispatch [:hardwallet.ui/secret-keys-next-button-pressed])
      :forward? true}]]])

(defn card-ready []
  [react/view styles/card-ready-container
   [react/view styles/card-ready-inner-container
    [components/wizard-step 3]
    [react/text {:style           styles/center-title-text
                 :number-of-lines 2
                 :font            :bold}
     (i18n/label :t/card-is-ready)]]
   [react/view styles/next-button-container
    [react/view components.styles/flex]
    [components.common/bottom-button
     {:on-press #(re-frame/dispatch [:hardwallet.ui/create-pin-button-pressed])
      :label    (i18n/label :t/create-pin)
      :forward? true}]]])

(defview enter-pair-code []
  (letsubs [pair-code [:hardwallet-pair-code]
            width [:dimensions/window-width]]
    [react/view styles/enter-pair-code-container
     [react/view styles/enter-pair-code-title-container
      [react/view
       [react/text {:style styles/enter-pair-code-title-text
                    :font  :bold}
        (i18n/label :t/enter-pair-code)]
       [react/text {:style styles/enter-pair-code-explanation-text}
        (i18n/label :t/enter-pair-code-description)]]
      [react/view (styles/enter-pair-code-input-container width)
       [text-input/text-input-with-label
        {:on-change-text    #(re-frame/dispatch [:hardwallet.ui/pair-code-input-changed %])
         :secure-text-entry true
         :placeholder       ""}]]]
     [react/view styles/next-button-container
      [react/view components.styles/flex]
      [components.common/bottom-button
       {:on-press  #(re-frame/dispatch [:hardwallet.ui/pair-code-next-button-pressed])
        :disabled? (empty? pair-code)
        :forward?  true}]]]))

(defn- card-with-button-view
  [{:keys [text-label button-label button-container-style on-press-event]}]
  "Generic view with centered card image and button at the bottom.
  Used by 'Prepare', 'Pair', 'No slots', 'Card is linked' screens"
  [react/view styles/card-with-button-view-container
   [react/view styles/hardwallet-card-image-container
    [react/image {:source (:hardwallet-card resources/ui)
                  :style  styles/hardwallet-card-image}]
    [react/view styles/center-text-container
     [react/text {:style styles/center-text}
      (i18n/label text-label)]]]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [on-press-event])}
    [react/view (merge styles/bottom-button-container button-container-style)
     [react/text {:style      styles/bottom-button-text
                  :font       :medium
                  :uppercase? true}
      (i18n/label button-label)]]]])

(defn begin []
  [card-with-button-view {:text-label     :t/card-is-empty
                          :button-label   :t/begin-set-up
                          :on-press-event :hardwallet.ui/begin-setup-button-pressed}])

(defn pair []
  [card-with-button-view {:text-label     :t/pair-card-question
                          :button-label   :t/pair-card
                          :on-press-event :hardwallet.ui/pair-card-button-pressed}])

(defn no-slots []
  [card-with-button-view {:text-label             :t/no-pairing-slots-available
                          :button-label           :t/help
                          :button-container-style {:background-color colors/white}
                          :on-press-event         :hardwallet.ui/no-pairing-slots-help-button-pressed}])

(defn card-already-linked []
  [card-with-button-view {:text-label             :t/card-already-linked
                          :button-label           :t/help
                          :button-container-style {:background-color colors/white}
                          :on-press-event         :hardwallet.ui/card-already-linked-help-button-pressed}])

(defn- loading-view [{:keys [title-label text-label estimated-time-seconds step-number]}]
  "Generic view with waiting time estimate and loading indicator.
  Used by 'Prepare', 'Pairing', 'Completing' screens"
  [react/view styles/loading-view-container
   [react/view styles/center-container
    [components/wizard-step step-number]
    [react/text {:style styles/center-title-text
                 :font  :bold}
     (i18n/label title-label)]
    (when text-label
      [react/text {:style           styles/generating-codes-for-pairing-text
                   :number-of-lines 2}
       (i18n/label text-label)])
    [react/text {:style styles/estimated-time-text}
     (str
      (i18n/label :t/estimated-time)
      " ~"
      estimated-time-seconds
      " "
      (i18n/label-pluralize estimated-time-seconds
                            :t/datetime-second))]]
   [react/view styles/waiting-indicator-container
    [react/activity-indicator {:animating true
                               :size      :large}]]])

(defn preparing []
  [loading-view {:title-label            :t/preparing-card
                 :text-label             :t/generating-codes-for-pairing
                 :estimated-time-seconds 20
                 :step-number            1}])

(defn pairing []
  [loading-view {:title-label            :t/pairing-card
                 :estimated-time-seconds 30}])

(defn complete []
  [loading-view {:title-label            :t/completing-card-setup
                 :estimated-time-seconds 30
                 :step-number            3}])

(defn- content [step]
  (case step
    :begin [begin]
    :preparing [preparing]
    :secret-keys [secret-keys]
    :card-ready [card-ready]
    :complete [complete]
    :pair [pair]
    :enter-pair-code [enter-pair-code]
    :no-slots [no-slots]
    :card-already-linked [card-already-linked]
    :pairing [pairing]
    :pin [pin.views/main]
    [begin]))

(defview hardwallet-setup []
  (letsubs [step [:hardwallet-setup-step]]
    [react/keyboard-avoiding-view components.styles/flex
     [react/view styles/container
      [react/view styles/inner-container
       [components/maintain-card]
       [content step]]]]))