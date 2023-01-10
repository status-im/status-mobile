(ns status-im2.common.toasts.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as ra]
            [reagent.core :as reagent]
            [status-im.utils.utils :as utils.utils]
            [status-im2.common.toasts.style :as style]
            [utils.re-frame :as rf]))

;; (def ^:private slide-out-up-animation
;;   (-> ^js ra/slide-out-up-animation
;;       .springify
;;       (.damping 20)
;;       (.stiffness 300)))

;; (def ^:private slide-in-up-animation
;;   (-> ^js ra/slide-in-up-animation
;;       .springify
;;       (.damping 20)
;;       (.stiffness 300)))

;; (def ^:private linear-transition
;;   (-> ^js ra/linear-transition
;;       .springify
;;       (.damping 20)
;;       (.stiffness 300)))

(defn toast
  [id]
  (let [toast-opts (rf/sub [:toasts/toast id])]
    [quo/toast toast-opts]))

(defn container
  [id]
  (let [dismissed-locally? (reagent/atom false)
        close!             #(rf/dispatch [:toasts/close id])
        timer              (reagent/atom nil)
        clear-timer        #(utils.utils/clear-timeout @timer)]
    (fn []
      [:f>
       (fn []
         (let [duration (or (rf/sub [:toasts/toast-cursor id :duration]) 3000)
               on-dismissed #((or (rf/sub [:toasts/toast-cursor id :on-dismissed]) identity) id)
               create-timer (fn []
                              (reset! timer (utils.utils/set-timeout close! duration)))
               translate-y (ra/use-val 0)
               pan
               (->
                 (gesture/gesture-pan)
                 ;; remove timer on pan start
                 (gesture/on-start clear-timer)
                 (gesture/on-update
                  (fn [^js evt]
                    (let [evt-translation-y (.-translationY evt)]
                      (cond
                        ;; reset translate y on pan down
                        (> evt-translation-y 100)
                        (ra/animate-spring translate-y
                                           0
                                           {:mass      1
                                            :damping   20
                                            :stiffness 300})
                        ;; dismiss on pan up
                        (< evt-translation-y -30)
                        (do (ra/animate-spring
                             translate-y
                             -500
                             {:mass 1 :damping 20 :stiffness 300})
                            (reset! dismissed-locally? true)
                            (close!))
                        :else
                        (ra/set-val translate-y
                                    evt-translation-y)))))
                 (gesture/on-end (fn [_]
                                   (when-not @dismissed-locally?
                                     (ra/set-val translate-y 0)
                                     (create-timer)))))]
           ;; create auto dismiss timer, clear timer when unmount or duration changed
           (rn/use-effect (fn [] (create-timer) clear-timer) [duration])
           (rn/use-unmount on-dismissed)
           [gesture/gesture-detector {:gesture pan}
            [ra/view
             {;; TODO: this will eanble layout animation at runtime and causing flicker on android
              ;; we need to resolve this and re-enable layout animation
              ;; issue at https://github.com/status-im/status-mobile/issues/14752
              ;; :entering slide-in-up-animation
              ;; :exiting  slide-out-up-animation
              ;; :layout   ra/linear-transition
              :style (ra/apply-animations-to-style
                      {:transform [{:translateY translate-y}]}
                      style/each-toast-container)}
             [toast id]]]))])))

(defn toasts
  []
  (let [toasts-ordered (:ordered (rf/sub [:toasts]))]
    [into
     [rn/view
      {:style style/outmost-transparent-container}]
     (map (fn [id] ^{:key id} [container id]) toasts-ordered)]))
