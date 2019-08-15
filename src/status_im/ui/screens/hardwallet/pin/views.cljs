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
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]))

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
    [react/view styles/numpad-empty-button]
    [numpad-button 0 step enabled?]
    [react/touchable-highlight
     {:on-press #(when enabled?
                   (re-frame/dispatch [:hardwallet.ui/pin-numpad-delete-button-pressed step]))}
     [react/view styles/numpad-delete-button
      [vector-icons/icon :main-icons/backspace {:color colors/blue}]]]]])

(defn pin-indicator [pressed? status]
  [react/view (styles/pin-indicator pressed? status)])

(defn pin-indicators [pin status style]
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
                  [pin-indicator (number? n) status])
                (concat pin
                        (repeat (- 6 (count pin))
                                nil)))))])

(defn puk-indicators [puk status]
  [react/view
   (map-indexed
    (fn [i puk-group]
      ^{:key i}
      [pin-indicators puk-group status {:margin-top 15}])
    (partition 6
               (concat puk
                       (repeat (- 12 (count puk))
                               nil))))])

(defn pin-view [{:keys [pin title-label description-label step status error-label
                        retry-counter]}]
  (let [enabled? (not= status :verifying)]
    [react/scroll-view
     [react/view styles/pin-container
      [react/view (styles/center-container title-label)
       (when title-label
         [react/text {:style styles/center-title-text}
          (i18n/label title-label)])
       (when description-label
         [react/text {:style           styles/create-pin-text
                      :number-of-lines 2}
          (i18n/label description-label)])
       [react/view {:height 10}
        (when retry-counter
          [react/text {:style {:font-weight "700"
                               :color       colors/red}}
           (i18n/label :t/pin-retries-left {:number retry-counter})])]
       [react/view {:height 22}
        (case status
          :verifying [react/view styles/waiting-indicator-container
                      [react/activity-indicator {:animating true
                                                 :size      :small}]]
          :error [react/view styles/error-container
                  [react/text {:style styles/error-text}
                   (i18n/label error-label)]]
          nil)]
       (if (= step :puk)
         [puk-indicators pin status]
         [pin-indicators pin status nil])
       [numpad step enabled?]]]]))

(def pin-retries 3)
(def puk-retries 5)

(defview create-pin []
  (letsubs [pin [:hardwallet/pin]
            step [:hardwallet/pin-enter-step]
            status [:hardwallet/pin-status]
            error-label [:hardwallet/pin-error-label]]
    [pin-view {:pin               pin
               :title-label       (case step
                                    :confirmation :t/repeat-pin
                                    :current :t/current-pin
                                    :t/create-a-pin)
               :description-label (case step
                                    :current :t/current-pin-description
                                    :t/create-pin-description)
               :step              step
               :status            status
               :error-label       error-label}]))

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
     [toolbar/toolbar
      nil
      [toolbar/nav-button (assoc toolbar.actions/default-back
                                 :handler
                                 #(re-frame/dispatch [:hardwallet.ui/enter-pin-navigate-back-button-clicked]))]
      nil]
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
                                       :import-multiaccount :t/current-pin
                                       :original :t/create-a-pin
                                       :confirmation :t/repeat-pin
                                       :t/current-pin)
                  :description-label (case step
                                       :current :t/current-pin-description
                                       :sign :t/current-pin-description
                                       :import-multiaccount :t/current-pin-description
                                       :login :t/login-pin-description
                                       :t/new-pin-description)
                  :step              step
                  :status            status
                  :error-label       error-label}])]))
