(ns status-im.contexts.keycard.pin.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn auth
  [{:keys [on-complete error]}]
  (let [{:keys [error? error-message]} error
        {:keys [text status]}          (rf/sub [:keycard/pin])
        pin-retry-counter              (rf/sub [:keycard/pin-retry-counter])
        error?                         (or error? (= status :error))]
    [rn/view {:padding-bottom 12 :flex 1}
     [rn/view {:flex 1 :justify-content :center :align-items :center :padding 34}
      [quo/pin-input
       {:blur?                 false
        :number-of-pins        constants/pincode-length
        :number-of-filled-pins (count text)
        :error?                error?
        :info                  (when error?
                                 (if error-message
                                   error-message
                                   (i18n/label :t/pin-retries-left {:number pin-retry-counter})))}]]
     [quo/numbered-keyboard
      {:delete-key? true
       :on-delete   #(rf/dispatch [:keycard.pin/delete-pressed])
       :on-press    #(rf/dispatch [:keycard.pin/number-pressed % constants/pincode-length
                                   on-complete])}]]))
