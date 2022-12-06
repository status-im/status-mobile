(ns status-im2.common.toasts.view
  (:require
   [quo2.core               :as quo2]
   [react-native.core       :as rn]
   [react-native.gesture    :as gesture]
   [react-native.reanimated :as reanimated]
   [reagent.core            :as reagent]
   [status-im.utils.utils   :as utils.utils]
   [utils.re-frame          :as rf]))

(def ^:private slide-out-up-animation
  (-> reanimated/slide-out-up-animation
      .springify
      (.damping 20)
      (.stiffness 300)))

(def ^:private slide-in-up-animation
  (-> reanimated/slide-in-up-animation
      .springify
      (.damping 20)
      (.stiffness 300)))

(def ^:private linear-transition
  (-> reanimated/linear-transition
      .springify
      (.damping 20)
      (.stiffness 300)))

(defn container
  [id]
  (let [toast-opts         (rf/sub [:toasts/toast id])
        duration           (get toast-opts :duration 3000)
        on-dismissed       (get toast-opts :on-dismissed identity)
        dismissed-locally? (reagent/atom false)
        close!             #(rf/dispatch [:toasts/close id])
        new-timer          #(utils.utils/set-timeout close! duration)
        timer              (reagent/atom (new-timer))
        clear-timer        #(utils.utils/clear-timeout @timer)]
    (fn []
      [:f>
       (fn []
         (rn/use-unmount #(do (clear-timer) (on-dismissed id)))
         (let [translate-y (reanimated/use-shared-value 0)
               pan
               (->
                (gesture/gesture-pan)
                 ;; remove timer on pan start
                (gesture/on-start clear-timer)
                (gesture/on-update
                 (fn [evt]
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
                       (< evt-translation-y -50)
                       (do (reanimated/animate-shared-value-with-spring
                            translate-y
                            -1000
                            {:mass 1 :damping 20 :stiffness 300})
                           (reset! dismissed-locally? true)
                           (close!))
                       :else
                       (reanimated/set-shared-value translate-y
                                                    evt-translation-y)))))
                (gesture/on-end (fn [_]
                                  (when-not dismissed-locally?
                                    (reanimated/set-shared-value translate-y 0)
                                    (reset! timer (new-timer))))))]
           [gesture/gesture-detector {:gesture pan}
            [reanimated/view
             {:entering slide-in-up-animation
              :exiting  slide-out-up-animation
              :layout   reanimated/linear-transition
              :style    (reanimated/apply-animations-to-style
                         {:transform [{:translateY translate-y}]}
                         {:width         "100%"
                          :margin-bottom 5})}
             [quo2/toast toast-opts]]]))])))

(defn toasts
  []
  (let [toasts-index (rf/sub [:toasts/index])]
    [into
     [rn/view
      {:style {:elevation        2
               :pointer-events   :box-none
               :padding-top      52
               :flex-direction   :column
               :justify-content  :center
               :align-items      :center
               :background-color :transparent}}]
     (->> toasts-index
          reverse
          (map (fn [id] ^{:key id} [container id])))]))
