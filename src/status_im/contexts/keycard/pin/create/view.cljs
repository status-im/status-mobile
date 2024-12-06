(ns status-im.contexts.keycard.pin.create.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [{:keys [on-complete]}     (rf/sub [:get-screen-params])
        [pin set-pin]             (rn/use-state "")
        [first-pin set-first-pin] (rn/use-state "")
        [error? set-error]        (rn/use-state false)
        [stage set-stage]         (rn/use-state :create)
        pin-empty?                (string/blank? pin)
        on-delete                 (rn/use-callback
                                   (fn []
                                     (set-error false)
                                     (if (= (count pin) 1)
                                       (do
                                         (set-pin "")
                                         (set-stage :create))
                                       (when (and pin (pos? (count pin)))
                                         (set-pin (.slice pin 0 -1)))))
                                   [pin])
        on-press                  (rn/use-callback
                                   (fn [new-symbol]
                                     (let [new-pin (str pin new-symbol)]
                                       (when (<= (count new-pin) constants/pincode-length)
                                         (set-pin new-pin)
                                         (when (= constants/pincode-length (count new-pin))
                                           (if (= :repeat stage)
                                             (if (= first-pin new-pin)
                                               (on-complete new-pin)
                                               (set-error true))
                                             (do
                                               (set-pin "")
                                               (set-first-pin new-pin)
                                               (set-stage :repeat)))))))
                                   [pin stage first-pin])]
    [rn/view {:style {:padding-bottom 12 :flex 1}}
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title            (if (= :create stage)
                           (i18n/label :t/create-keycard-pin)
                           (i18n/label :t/repeat-keycard-pin))
       :description      :text
       :description-text (i18n/label :t/pin-needed-login-sign)}]
     [rn/view {:style {:flex 1 :justify-content :center :align-items :center :padding-vertical 34}}
      [quo/pin-input
       {:blur?                 false
        :number-of-pins        constants/pincode-length
        :number-of-filled-pins (count pin)
        :error?                error?
        :info                  (when error? (i18n/label :t/pin-not-match))}]]
     [quo/numbered-keyboard
      {:delete-key? (not pin-empty?)
       :on-delete   on-delete
       :on-press    on-press}]]))
