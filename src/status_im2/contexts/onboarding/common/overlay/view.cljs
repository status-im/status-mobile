(ns status-im2.contexts.onboarding.common.overlay.view
  (:require [react-native.reanimated :as reanimated]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [react-native.platform :as platform]
            [status-im2.contexts.onboarding.common.overlay.style :as style]
            [status-im2.constants :as constants]))

(def max-blur-amount 30)

(defonce timer-interval (atom nil))
(defonce blur-dismiss-fn-atom (atom nil))
(defonce blur-show-fn-atom (atom nil))

(defn blur-show
  [opacity blur-amount]
  (reanimated/animate opacity
                      1
                      (/ constants/onboarding-modal-animation-duration 2))
  (js/clearInterval @timer-interval)
  (reset! timer-interval
    (js/setInterval
     (fn []
       (if (< @blur-amount max-blur-amount)
         (swap! blur-amount + 1)
         (js/clearInterval @timer-interval)))
     (/ constants/onboarding-modal-animation-duration
        max-blur-amount
        2))))

(defn blur-dismiss
  [opacity blur-amount]
  (reanimated/animate-delay opacity
                            0
                            (/ constants/onboarding-modal-animation-duration 2)
                            (/ constants/onboarding-modal-animation-duration 2))
  (js/clearInterval @timer-interval)
  (reset! timer-interval
    (js/setInterval
     (fn []
       (if (> @blur-amount 0)
         (swap! blur-amount dec)
         (js/clearInterval @timer-interval)))
     (/ constants/onboarding-modal-animation-duration
        max-blur-amount
        2))))

;; we had to register it here, because of hotreload, overwise on hotreload it will be reseted
(defonce blur-amount (reagent/atom 0))

(defn f-view
  []
  (let [opacity         (reanimated/use-shared-value (if (zero? @blur-amount) 0 1))
        blur-show-fn    #(blur-show opacity blur-amount)
        blur-dismiss-fn #(blur-dismiss opacity blur-amount)]
    (rn/use-effect
     (fn []
       (reset! blur-show-fn-atom blur-show-fn)
       (reset! blur-dismiss-fn-atom blur-dismiss-fn)
       (fn []
         (reset! blur-show-fn-atom nil)
         (reset! blur-dismiss-fn-atom nil))))
    [reanimated/view
     {:pointer-events :none
      :style          (style/blur-container opacity)}
     [blur/view
      {:blur-amount   @blur-amount
       :blur-radius   (if platform/android? 25 10)
       :blur-type     :transparent
       :overlay-color :transparent
       :style         style/blur-style}]]))

(defn view
  []
  [:f> f-view])
