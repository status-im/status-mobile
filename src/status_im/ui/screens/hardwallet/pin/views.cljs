(ns status-im.ui.screens.hardwallet.pin.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.hardwallet.pin.styles :as styles]
            [status-im.ui.screens.hardwallet.components :as components]))

(defn numpad-button [n step enabled?]
  [react/touchable-highlight
   {:on-press #(when enabled?
                 (re-frame/dispatch [:hardwallet.ui/pin-numpad-button-pressed n step]))}
   [react/view styles/numpad-button
    [react/text {:style styles/numpad-button-text}
     n]]])

(defn numpad-row [[a b c] step enabled?]
  [react/view styles/numpad-row-container
   [numpad-button a step enabled?]
   [numpad-button b step enabled?]
   [numpad-button c step enabled?]])

(defn numpad [step enabled?]
  [react/view styles/numpad-container
   [numpad-row [1 2 3] step enabled?]
   [numpad-row [4 5 6] step enabled?]
   [numpad-row [7 8 9] step enabled?]
   [react/view styles/numpad-row-container
    [react/view styles/numpad-empty-button
     [react/text {:style styles/numpad-empty-button-text}]]
    [numpad-button 0 step enabled?]
    [react/touchable-highlight
     {:on-press #(when enabled?
                   (re-frame/dispatch [:hardwallet.ui/pin-numpad-delete-button-pressed step]))}
     [react/view styles/numpad-delete-button
      [vector-icons/icon :icons/back {:color colors/blue}]]]]])

(defn pin-indicator [pressed?]
  [react/view (styles/pin-indicator pressed?)])

(defn pin-indicators [pin]
  [react/view styles/pin-indicator-container
   (map-indexed
    (fn [i group]
      ^{:key i}
      [react/view styles/pin-indicator-group-container
       group])
    (partition 3
               (map-indexed
                (fn [i n]
                  ^{:key i}
                  [pin-indicator (number? n)])
                (concat pin
                        (repeat (- 6 (count pin))
                                nil)))))])

(defn pin-view [{:keys [pin title step status error]}]
  (let [enabled? (not= status :validating)]
    [react/view styles/pin-container
     [react/view styles/center-container
      [components/wizard-step 4]
      [react/text {:style styles/center-title-text
                   :font  :bold}
       (i18n/label title)]
      [react/text {:style           styles/create-pin-text
                   :number-of-lines 2}
       (i18n/label :t/create-pin-description)]
      (case status
        :validating [react/view styles/waiting-indicator-container
                     [react/activity-indicator {:animating true
                                                :size      :small}]]
        :error [react/view styles/error-container
                [react/text {:style styles/error-text
                             :font  :medium}
                 (i18n/label error)]]
        [pin-indicators pin])
      [numpad step enabled?]]]))

(defview main []
  (letsubs [original [:hardwallet/pin]
            confirmation [:hardwallet/pin-confirmation]
            enter-step [:hardwallet/pin-enter-step]
            status [:hardwallet/pin-status]
            error [:hardwallet/pin-error]]
    (case enter-step
      :original [pin-view {:pin    original
                           :title  :t/create-pin
                           :step   :original
                           :status status
                           :error  error}]
      :confirmation [pin-view {:pin    confirmation
                               :title  :t/repeat-pin
                               :step   :confirmation
                               :status status
                               :error  error}])))