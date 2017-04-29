(ns status-im.chat.views.input.animations.expandable
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [reagent.core :as r]
            [reagent.impl.component :as rc]
            [re-frame.core :refer [dispatch subscribe]]
            [status-im.components.animation :as anim]
            [status-im.components.drag-drop :as drag]
            [status-im.components.react :refer [view
                                                animated-view]]
            [status-im.chat.views.input.animations.responder :as resp]
            [status-im.chat.views.input.utils :as input-utils]
            [status-im.chat.styles.animations :as style]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as p]))

(defn header [key container-height custom-header]
  (let [set-container-height (subscribe [:chat-animations key :height])
        max-container-height (subscribe [:get-max-container-area-height])
        pan-responder        (resp/pan-responder container-height
                                                 max-container-height
                                                 :fix-expandable-height
                                                 key)]
    (fn [_]
      [view (merge (drag/pan-handlers pan-responder)
                   {:style style/header-container})
       [view style/header-icon]
       (when (and custom-header
                  (or (= @set-container-height :max)
                      (> @set-container-height (:min-height style/header-container))))
         [custom-header])])))

(defn expandable-view-on-update [{:keys [anim-value to-changed-height max-height chat-input-margin height]}]
  (let [to-default-height (subscribe [:get-default-container-area-height])
        layout-height     (subscribe [:get :layout-height])]
    (fn [_]
      (let [to-change-height (if (= @to-changed-height :max)
                               (input-utils/max-container-area-height @chat-input-margin @layout-height)
                               @to-changed-height)
            to-value         (min (or @to-changed-height (or height @to-default-height))
                                  @max-height)]
        (dispatch [:set :expandable-view-height-to-value to-value])
        (anim/start
          (anim/spring anim-value {:toValue  to-value
                                   :friction 10
                                   :tension  60}))))))

(defview overlay-view []
  [max-height     (subscribe [:get-max-container-area-height])
   layout-height  (subscribe [:get :layout-height])
   view-height-to (subscribe [:get :expandable-view-height-to-value])]
  (let [related-height (/ @view-height-to @max-height)]
    (when (> related-height 0.6)
      [animated-view {:style (style/result-box-overlay @layout-height (- related-height (/ 0.4 related-height)))}])))

(defn expandable-view [{:keys [key height hide-overlay?]} & _]
  (let [anim-value         (anim/create-value 0)
        input-height       (subscribe [:chat-ui-props :input-height])
        max-height         (subscribe [:get-max-container-area-height])
        fullscreen?        (subscribe [:chat-ui-props :fullscreen?])
        chat-input-margin  (subscribe [:chat-input-margin])
        to-changed-height  (subscribe [:chat-animations key :height])
        changes-counter    (subscribe [:chat-animations key :changes-counter])
        on-update          (expandable-view-on-update {:anim-value        anim-value
                                                       :to-changed-height to-changed-height
                                                       :max-height        max-height
                                                       :chat-input-margin chat-input-margin
                                                       :height            height})]
    (r/create-class
      {:component-did-mount
       on-update
       :component-did-update
       on-update
       :component-will-unmount
       (fn []
         (dispatch [:set-chat-ui-props {:fullscreen? false}])
         (if height
           (dispatch [:set-expandable-height key height])
           (dispatch [:choose-predefined-expandable-height key :default])))
       :reagent-render
       (fn [{:keys [draggable? custom-header]} & elements]
         @to-changed-height @changes-counter @max-height
         (let [{expandable-offset :expandable-offset
                status-bar-height :height} (get-in p/platform-specific [:component-styles :status-bar :main])
               bottom (+ @input-height @chat-input-margin)
               height (if @fullscreen?
                        (+ @max-height status-bar-height expandable-offset)
                        anim-value)]
           [view style/overlap-container
            (when (and (not hide-overlay?)
                       (not @fullscreen?))
              [overlay-view])
            (into [animated-view {:style (style/expandable-container height bottom)}
                   (when (and draggable?
                              (not @fullscreen?))
                     [header key anim-value custom-header])]
                  elements)]))})))
