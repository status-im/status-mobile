(ns status-im2.common.toasts.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [react-native.gesture :as gesture]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]
            [status-im.utils.utils :as utils.utils]
            [status-im2.common.toasts.style :as style]
            [utils.re-frame :as rf]))

;; (def ^:private slide-out-up-animation
;;   (-> ^js reanimated/slide-out-up-animation
;;       .springify
;;       (.damping 20)
;;       (.stiffness 300)))

;; (def ^:private slide-in-up-animation
;;   (-> ^js reanimated/slide-in-up-animation
;;       .springify
;;       (.damping 20)
;;       (.stiffness 300)))

;; (def ^:private linear-transition
;;   (-> ^js reanimated/linear-transition
;;       .springify
;;       (.damping 20)
;;       (.stiffness 300)))

(defn toast
  [id]
  (let [{:keys [type] :as toast-opts} (rf/sub [:toasts/toast id])]
    (if (= type :notification)
      [quo/notification toast-opts]
      [quo/toast toast-opts])))

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
               on-dismissed (or (rf/sub [:toasts/toast-cursor id :on-dismissed]) identity)
               create-timer (fn []
                              (reset! timer (utils.utils/set-timeout close! duration)))
               translate-y (reanimated/use-shared-value 0)
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
                        (reanimated/animate-shared-value-with-spring translate-y
                                                                     0
                                                                     {:mass      1
                                                                      :damping   20
                                                                      :stiffness 300})
                        ;; dismiss on pan up
                        (< evt-translation-y -30)
                        (do (reanimated/animate-shared-value-with-spring
                             translate-y
                             -500
                             {:mass 1 :damping 20 :stiffness 300})
                            (reset! dismissed-locally? true)
                            (close!))
                        :else
                        (reanimated/set-shared-value translate-y
                                                     evt-translation-y)))))
                 (gesture/on-end (fn [_]
                                   (when-not @dismissed-locally?
                                     (reanimated/set-shared-value translate-y 0)
                                     (create-timer)))))]
           ;; create auto dismiss timer, clear timer when unmount or duration changed
           (rn/use-effect (fn [] (create-timer) clear-timer) [duration])
           (rn/use-unmount #(on-dismissed id))
           [gesture/gesture-detector {:gesture pan}
            [reanimated/view
             {;; TODO: this will enable layout animation at runtime and causing flicker on android
              ;; we need to resolve this and re-enable layout animation
              ;; issue at https://github.com/status-im/status-mobile/issues/14752
              ;; :entering slide-in-up-animation
              ;; :exiting  slide-out-up-animation
              ;; :layout   reanimated/linear-transition
              :style (reanimated/apply-animations-to-style
                      {:transform [{:translateY translate-y}]}
                      style/each-toast-container)}
             [toast id]]]))])))

(defn toasts
  []
  (let [toasts-ordered (:ordered (rf/sub [:toasts]))]
    [into
     [rn/view
      {:style style/outmost-transparent-container}]
     (doall
      (map (fn [id] ^{:key id} [container id]) toasts-ordered))]))
