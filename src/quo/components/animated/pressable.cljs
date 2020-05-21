(ns quo.components.animated.pressable
  (:require [quo.animated :as animated]
            [quo.gesture-handler :as gesture-handler]))

(def long-press-duration 500)
(def scale-down-small 0.95)
(def scale-down-large 0.9)
(def opactiy 0.75)
(def time-in 100)
(def time-out 200)

(defmulti type->animation :type)

(defmethod type->animation :primary
  [{:keys [animation]}]
  {:background {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(defmethod type->animation :secondary
  [{:keys [animation]}]
  {:background {:transform [{:scale (animated/mix animation scale-down-small 1)}]
                :opacity   (animated/mix animation 0 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(defmethod type->animation :icon
  [{:keys [animation]}]
  {:background {:transform [{:scale (animated/mix animation scale-down-large 1)}]
                :opacity   (animated/mix animation 0 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-large)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(defmethod type->animation :list-item
  [{:keys [animation]}]
  {:background {:opacity (animated/mix animation 0 opactiy)}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(def absolute-fill
  {:top      0
   :bottom   0
   :left     0
   :right    0
   :position :absolute})

(defn pressable []
  (let [state           (animated/value (:undetermined gesture-handler/states))
        active          (animated/eq state (:began gesture-handler/states))
        gesture-handler (animated/on-gesture {:state state})
        duration        (animated/cond* active time-in time-out)
        long-pressed    (animated/value 0)
        long-duration   (animated/cond* active long-press-duration 0)
        long-timing     (animated/with-timing-transition active
                          {:duration long-duration})
        animation       (animated/with-timing-transition active
                          {:duration duration
                           :easing   (:ease-in animated/easings)})]
    (fn [{:keys [background-color border-radius type disabled
                 on-press on-long-press on-press-start
                 accessibility-label]
          :or   {border-radius 0
                 type          :primary}}
         & children]
      (let [{:keys [background foreground]}
            (type->animation {:type      type
                              :animation animation})
            handle-press       (fn [] (when on-press (on-press)))
            handle-press-start (fn [] (when on-press-start (on-press-start)))
            handle-long-press  (fn [] (when on-long-press (on-long-press)))]
        [:<>
         (when on-long-press
           [animated/code
            {:exec (animated/block
                    [(animated/cond* (animated/eq long-timing 1)
                                     (animated/set long-pressed 1))
                     (animated/cond* long-pressed
                                     [(animated/set long-pressed 0)
                                      (animated/call* [] handle-long-press)
                                      (animated/set state (:undetermined gesture-handler/states))])])}])
         [animated/code
          {:key  (str on-press on-long-press on-press-start)
           :exec (animated/on-change state
                                     [(animated/cond* (animated/eq state (:began gesture-handler/states))
                                                      (animated/call* [] handle-press-start))
                                      (animated/cond* (animated/and* (animated/eq state (:end gesture-handler/states))
                                                                     (animated/not* long-pressed))
                                                      [(animated/call* [] handle-press)
                                                       (animated/set state (:undetermined gesture-handler/states))])])}]
         [gesture-handler/tap-gesture-handler
          (merge gesture-handler
                 {:shouldCancelWhenOutside true
                  :enabled                 (not disabled)})
          [animated/view {:accessible          true
                          :accessibility-label accessibility-label}
           [animated/view {:style (merge absolute-fill
                                         background
                                         {:background-color background-color
                                          :border-radius    border-radius})}]
           (into [animated/view {:style foreground}]
                 children)]]]))))
