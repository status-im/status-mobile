(ns status-im.ui.screens.chat.message.reactions
  (:require [status-im.ui.screens.chat.message.reactions-picker :as reaction-picker]
            [status-im.ui.screens.chat.message.reactions-row :as reaction-row]
            [reagent.core :as reagent]
            [quo.react-native :as rn]
            [quo.react :as react]
            [quo.animated :as animated]))

(defn measure-in-window [ref cb]
  (.measureInWindow ^js ref cb))

(defn get-picker-position [^js ref cb]
  (some-> ref
          react/current-ref
          (measure-in-window
           (fn [x y width height]
             (cb {:top    y
                  :left   x
                  :width  width
                  :height height})))))

(defn- extract-id [reactions id]
  (->> reactions
       (filter (fn [{:keys [emoji-id]}] (= emoji-id id)))
       first
       :emoji-reaction-id))

(defn with-reaction-picker []
  (let [ref              (react/create-ref)
        animated-state   (animated/value 0)
        spring-animation (animated/with-spring-transition
                           animated-state
                           (:jump animated/springs))
        animation        (animated/with-timing-transition
                           animated-state
                           {:duration reaction-picker/animation-duration
                            :easing   (:ease-in-out animated/easings)})
        visible          (reagent/atom false)
        actions          (reagent/atom nil)
        position         (reagent/atom {})]
    (fn [{:keys [message reactions outgoing outgoing-status render send-emoji retract-emoji picker-on-open
                 picker-on-close timeline]}]
      (let [own-reactions  (reduce (fn [acc {:keys [emoji-id own]}]
                                     (if own (conj acc emoji-id) acc))
                                   [] reactions)
            on-emoji-press (fn [emoji-id]
                             (let [active ((set own-reactions) emoji-id)]
                               (if active
                                 (retract-emoji {:emoji-id          emoji-id
                                                 :emoji-reaction-id (extract-id reactions emoji-id)})
                                 (send-emoji {:emoji-id emoji-id}))))
            on-close       (fn []
                             (animated/set-value animated-state 0)
                             (js/setTimeout
                              (fn []
                                (reset! actions nil)
                                (reset! visible false)
                                (picker-on-close))))
            on-open        (fn [pos]
                             (picker-on-open)
                             (reset! position pos)
                             (reset! visible true))]
        [:<>
         [rn/view {:ref         ref
                   :collapsable false}
          [render message {:modal         false
                           :on-long-press (fn [act]
                                            (when (or (not outgoing)
                                                      (and outgoing (= outgoing-status :sent)))
                                              (reset! actions act)
                                              (get-picker-position ref on-open)))}]
          [reaction-row/message-reactions message reactions timeline]]
         (when @visible
           [rn/modal {:on-request-close on-close
                      :on-show          (fn []
                                          (js/requestAnimationFrame
                                           #(animated/set-value animated-state 1)))
                      :transparent      true}
            [reaction-picker/modal {:outgoing       (:outgoing message)
                                    :display-photo  (:display-photo? message)
                                    :animation      animation
                                    :spring         spring-animation
                                    :top            (:top @position)
                                    :message-height (:height @position)
                                    :on-close       on-close
                                    :actions        @actions
                                    :own-reactions  own-reactions
                                    :timeline       timeline
                                    :send-emoji     (fn [emoji]
                                                      (on-close)
                                                      (js/setTimeout #(on-emoji-press emoji)
                                                                     reaction-picker/animation-duration))}
             [render message {:modal         true
                              :on-long-press #()
                              :close-modal   on-close}]]])]))))
