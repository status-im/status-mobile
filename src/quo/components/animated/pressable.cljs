(ns quo.components.animated.pressable
  (:require [cljs-bean.core :as bean]
            [quo.animated :as animated]
            [quo.gesture-handler :as gesture-handler]
            [quo.react :as react]
            [reagent.core :as reagent]))

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

(defmethod type->animation :scale
  [{:keys [animation]}]
  {:background {:opacity 0}
   :foreground {:transform [{:scale (animated/mix animation 1 scale-down-small)}]
                :opacity   (animated/mix animation 1 opactiy)}})

(def absolute-fill
  {:top      0
   :bottom   0
   :left     0
   :right    0
   :position :absolute})

(defn pressable-hooks
  [props]
  (let [{background-color    :bgColor
         border-radius       :borderRadius
         border-color        :borderColor
         border-width        :borderWidth
         type                :type
         disabled            :disabled
         on-press            :onPress
         on-long-press       :onLongPress
         on-press-start      :onPressStart
         accessibility-label :accessibilityLabel
         children            :children
         :or                 {border-radius 0
                              type          "primary"}}
        (bean/bean props)
        long-press-ref (react/create-ref)
        state (animated/use-value (:undetermined gesture-handler/states))
        active (animated/eq state (:began gesture-handler/states))
        gesture-handler (animated/use-gesture {:state state})
        animation (react/use-memo
                   (fn []
                     (animated/with-timing-transition active
                                                      {:duration (animated/cond* active time-in time-out)
                                                       :easing   (:ease-in animated/easings)}))
                   [])
        {:keys [background
                foreground]}
        (react/use-memo
         (fn []
           (type->animation {:type      (keyword type)
                             :animation animation}))
         [type])
        handle-press (fn [] (when on-press (on-press)))
        long-gesture-handler (react/callback
                              (fn [^js evt]
                                (let [gesture-state (-> evt .-nativeEvent .-state)]
                                  (when (and on-press-start
                                             (= gesture-state (:began gesture-handler/states)))
                                    (on-press-start))
                                  (when (and on-long-press
                                             (= gesture-state (:active gesture-handler/states)))
                                    (on-long-press)
                                    (animated/set-value state (:undetermined gesture-handler/states)))))
                              [on-long-press on-press-start])]
    (animated/code!
     (fn []
       (when on-press
         (animated/cond* (animated/eq state (:end gesture-handler/states))
                         [(animated/set state (:undetermined gesture-handler/states))
                          (animated/call* [] handle-press)])))
     [on-press])
    (reagent/as-element
     [gesture-handler/long-press-gesture-handler
      {:enabled                 (boolean (and on-long-press (not disabled)))
       :on-handler-state-change long-gesture-handler
       :min-duration-ms         long-press-duration
       :max-dist                22
       :ref                     long-press-ref}
      [animated/view
       {:accessibility-label accessibility-label}
       [gesture-handler/tap-gesture-handler
        (merge gesture-handler
               {:shouldCancelWhenOutside true
                :wait-for                long-press-ref
                :enabled                 (boolean (and (or on-press on-long-press on-press-start)
                                                       (not disabled)))})
        [animated/view
         [animated/view
          {:style (merge absolute-fill
                         background
                         {:background-color background-color
                          :border-radius    border-radius
                          :border-color     border-color
                          :border-width     border-width})}]
         (into [animated/view {:style foreground}]
               (react/get-children children))]]]])))

(def pressable (reagent/adapt-react-class (react/memo pressable-hooks)))
