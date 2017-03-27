(ns status-im.chat.views.input.animations.expandable
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.components.animation :as anim]
            [status-im.components.drag-drop :as drag]
            [status-im.components.react :refer [view
                                                animated-view]]
            [status-im.chat.views.input.animations.responder :as resp]
            [status-im.chat.styles.animations :as style]
            [taoensso.timbre :as log]))

(defn header [key container-height]
  (let [max-container-height (subscribe [:get-max-container-area-height])
        pan-responder        (resp/pan-responder container-height
                                                 max-container-height
                                                 :fix-expandable-height
                                                 key)]
    (fn [_]
      [view (merge (drag/pan-handlers pan-responder)
                   {:style style/header-container})
       [view style/header-icon]])))

(defn expandable-view [key & _]
  (let [anim-value         (anim/create-value 0)
        input-height       (subscribe [:chat-ui-props :input-height])
        chat-input-margin  (subscribe [:chat-input-margin])
        to-default-height  (subscribe [:get-default-container-area-height])
        to-changed-height  (subscribe [:chat-animations key :height])
        changes-counter    (subscribe [:chat-animations key :changes-counter])
        on-update          (fn [_]
                             (anim/start
                               (anim/spring anim-value {:toValue  (or @to-changed-height
                                                                      @to-default-height)
                                                        :friction 10
                                                        :tension  60})))]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :component-will-unmount
       (fn [_]
         (dispatch [:set-default-expandable-height key]))
       :reagent-render
       (fn [_ & elements]
         @to-changed-height @changes-counter
         (let [bottom (+ @input-height @chat-input-margin)]
           (into [animated-view {:style (style/expandable-container anim-value bottom)}
                  [header key anim-value]]
                 elements)))})))