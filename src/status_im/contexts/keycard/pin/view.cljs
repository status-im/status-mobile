(ns status-im.contexts.keycard.pin.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
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
    (rn/use-unmount #(rf/dispatch [:keycard.pin/clear]))
    [rn/view
     {:style {:flex           1
              :gap            34
              :padding-bottom 12}}
     [rn/view {:style {:flex 1 :justify-content :center :align-items :center}}
      [quo/pin-input
       {:blur?                 false
        :number-of-pins        constants/pincode-length
        :number-of-filled-pins (count text)
        :info-error?           error?
        :info                  (when error?
                                 (if (not (string/blank? error-message))
                                   error-message
                                   (i18n/label-pluralize pin-retry-counter :t/pin-retries-left)))}]]
     [quo/numbered-keyboard
      {:delete-key? true
       :on-delete   #(rf/dispatch [:keycard.pin/delete-pressed])
       :on-press    #(rf/dispatch [:keycard.pin/number-pressed % constants/pincode-length
                                   on-complete])}]]))
