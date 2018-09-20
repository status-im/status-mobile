(ns status-im.ui.screens.hardwallet.setup.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.common.common :as components.common]
            [status-im.react-native.resources :as resources]
            [status-im.ui.screens.hardwallet.setup.styles :as styles]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]))

(defn begin []
  [react/view styles/container
   [react/view components.styles/flex
    [react/view styles/inner-container
     [react/view styles/maintain-card-container
      [vector-icons/icon :icons/hardwallet {:color colors/blue}]
      [react/text {:style styles/maintain-card-text}
       (i18n/label :t/maintain-card-to-phone-contact)]]
     [react/view styles/hardwallet-card-image-container
      [react/image {:source (:hardwallet-card resources/ui)
                    :style  styles/hardwallet-card-image}]
      [react/view styles/card-is-empty-text-container
       [react/text {:style styles/card-is-empty-text}
        (i18n/label :t/card-is-empty)]]]
     [react/touchable-highlight
      {:on-press #(re-frame/dispatch [:hardwallet.ui/begin-setup-button-pressed])}
      [react/view styles/bottom-action-container
       [react/text {:style      styles/begin-set-up-text
                    :font       :medium
                    :uppercase? true}
        (i18n/label :t/begin-set-up)]]]]]])

(defn prepare []
  [react/view styles/container
   [react/view components.styles/flex
    [react/view styles/inner-container
     [react/view styles/maintain-card-container
      [vector-icons/icon :icons/hardwallet {:color colors/blue}]
      [react/text {:style styles/maintain-card-text}
       (i18n/label :t/maintain-card-to-phone-contact)]]
     [react/view styles/center-container
      [react/text {:style styles/center-title-text
                   :font  :bold}
       (i18n/label :t/preparing-card)]
      [react/text {:style           styles/generating-codes-for-pairing-text
                   :number-of-lines 2}
       (i18n/label :t/generating-codes-for-pairing)]
      [react/text {:style styles/estimated-time-text}
       (i18n/label :t/estimated-time
                   {:time "~20 seconds"})]]
     [react/view styles/waiting-indicator-container
      [react/activity-indicator {:animating true
                                 :size      :large}]]]]])

(defn secret-keys []
  [react/view styles/container
   [react/view components.styles/flex
    [react/view styles/inner-container
     [react/view styles/maintain-card-container
      [vector-icons/icon :icons/hardwallet {:color colors/blue}]
      [react/text {:style styles/maintain-card-text}
       (i18n/label :t/maintain-card-to-phone-contact)]]
     [react/view styles/secret-keys-container
      [react/view styles/secret-keys-title-container
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
       [react/text {:style styles/puk-code-text
                    :font  :bold}
        "1234 5678 9123"]]
      [react/text {:style styles/pair-code-title-text
                   :font  :bold}
       (i18n/label :t/pair-code)]
      [react/text {:style           styles/pair-code-explanation-text
                   :number-of-lines 2}
       (i18n/label :t/pair-code-explanation)]
      [react/view styles/pair-code-text-container
       [react/text {:style styles/pair-code-text
                    :font  :bold}
        "a12k52kh0x"]]]
     [react/view styles/next-button-container
      [react/view components.styles/flex]
      [components.common/bottom-button
       {:on-press #(re-frame/dispatch [:hardwallet.ui/secret-keys-next-button-pressed])
        :forward? true}]]]]])

(defn complete []
  [react/view styles/container
   [react/view components.styles/flex
    [react/view styles/inner-container
     [react/view styles/maintain-card-container
      [vector-icons/icon :icons/hardwallet {:color colors/blue}]
      [react/text {:style styles/maintain-card-text}
       (i18n/label :t/maintain-card-to-phone-contact)]]
     [react/view styles/center-container
      [react/text {:style styles/center-title-text
                   :font  :bold}
       (i18n/label :t/completing-card-setup)]
      [react/text {:style styles/estimated-time-text}
       (i18n/label :t/estimated-time {:time "~30 seconds"})]]
     [react/view styles/waiting-indicator-container
      [react/activity-indicator {:animating true
                                 :size      :large}]]]]])

(defview hardwallet-setup []
  (letsubs [step [:hardwallet-setup-step]]
    (case step
      :begin [begin]
      :prepare [prepare]
      :secret-keys [secret-keys]
      :complete [complete])))