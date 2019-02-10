(ns status-im.ui.screens.hardwallet.pin.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.hardwallet.pin.styles :as styles]
            [status-im.ui.screens.hardwallet.components :as components]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.actions :as actions]))

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
      [vector-icons/icon :main-icons/back {:color colors/blue}]]]]])

(defn pin-indicator [pressed?]
  [react/view (styles/pin-indicator pressed?)])

(defn pin-indicators [pin style]
  [react/view (merge styles/pin-indicator-container style)
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

(defn puk-indicators [puk]
  [react/view
   (map-indexed
    (fn [i puk-group]
      ^{:key i}
      [pin-indicators puk-group {:margin-top 15}])
    (partition 6
               (concat puk
                       (repeat (- 12 (count puk))
                               nil))))])

(defn pin-view [{:keys [pin title-label description-label step status error-label
                        retry-counter]}]
  (let [enabled? (not= status :verifying)]
    [react/scroll-view
     [react/view styles/pin-container
      [react/view styles/center-container
       [react/text {:style styles/center-title-text
                    :font  :bold}
        (i18n/label title-label)]
       [react/text {:style           styles/create-pin-text
                    :number-of-lines 2}
        (i18n/label description-label)]
       (when retry-counter
         [react/text {:style {:font-weight :bold
                              :padding-top 10
                              :font-size   15
                              :color       colors/red}}
          (i18n/label :t/pin-retries-left {:number retry-counter})])
       (case status
         :verifying [react/view styles/waiting-indicator-container
                     [react/activity-indicator {:animating true
                                                :size      :small}]]
         :error [react/view styles/error-container
                 [react/text {:style styles/error-text
                              :font  :medium}
                  (i18n/label error-label)]]
         (if (= step :puk)
           [puk-indicators pin]
           [pin-indicators pin]))
       [numpad step enabled?]]]]))

(def pin-retries 3)
(def puk-retries 5)

(defview enter-pin []
  (letsubs [pin [:hardwallet/pin]
            step [:hardwallet/pin-enter-step]
            status [:hardwallet/pin-status]
            pin-retry-counter [:hardwallet/pin-retry-counter]
            puk-retry-counter [:hardwallet/puk-retry-counter]
            error-label [:hardwallet/pin-error-label]]
    [react/view {:flex             1
                 :background-color colors/white}
     [status-bar/status-bar]
     [toolbar/toolbar nil toolbar/default-nav-back nil]
     (if (zero? pin-retry-counter)
       [pin-view {:pin               pin
                  :retry-counter     (when (< puk-retry-counter puk-retries) puk-retry-counter)
                  :title-label       :t/enter-puk-code
                  :description-label :t/enter-puk-code-description
                  :step              step
                  :status            status
                  :error-label       error-label}]
       [pin-view {:pin               pin
                  :retry-counter     (when (< pin-retry-counter pin-retries) pin-retry-counter)
                  :title-label       (case step
                                       :current :t/current-pin
                                       :login :t/current-pin
                                       :original :t/create-pin
                                       :confirmation :t/repeat-pin
                                       :t/current-pin)
                  :description-label (case step
                                       :current :t/current-pin-description
                                       :login :t/login-pin-description
                                       :t/new-pin-description)
                  :step              step
                  :status            status
                  :error-label       error-label}])]))
