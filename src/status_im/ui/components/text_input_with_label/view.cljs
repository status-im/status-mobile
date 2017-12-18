(ns status-im.ui.components.text-input-with-label.view
  (:require [reagent.core :as reagent]
            [status-im.ui.components.animation :as animation]
            [status-im.ui.components.text-input-with-label.animation :as label-animation]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.text-input-with-label.styles :as styles]
            [clojure.string :as string]))

(defn get-init-props [{:keys [default-value]}]
  (let [blank? (string/blank? default-value)]
    {:underline-width        (animation/create-value 0)
     :underline-height       (animation/create-value 1)
     :label-top              (animation/create-value (if blank?
                                                       styles/label-top-bottom
                                                       styles/label-top-top))
     :label-font-size        (animation/create-value (if blank?
                                                       styles/label-font-size-bottom
                                                       styles/label-font-size-top))
     :label-top-top          styles/label-top-top
     :label-top-bottom       styles/label-top-bottom
     :label-font-size-top    styles/label-font-size-top
     :label-font-size-bottom styles/label-font-size-bottom
     :underline-max-height   styles/underline-max-height
     :input-ref*             (reagent/atom nil)
     :value*                 (reagent/atom default-value)
     :underline-max-width*   (reagent/atom 0)}))

(defn get-width [event]
  (.-width (.-layout (.-nativeEvent event))))

(defn text-input-on-change-text [text props]
  (label-animation/animate-label text props)
  (reset! (:value* props) text))

(defn text-input-handlers [{:keys [on-focus on-blur on-change-text on-change
                                   on-submit-editing max-height ref]} props]
  {:ref               #(do
                         (reset! (:input-ref* props) %)
                         (when ref (ref %)))
   :on-submit-editing #(do
                         (.blur @(:input-ref* props))
                         (when on-submit-editing (on-submit-editing)))
   :on-focus          #(do
                         (label-animation/text-input-on-focus props)
                         (when on-focus (on-focus)))
   :on-blur           #(do
                         (label-animation/text-input-on-blur props)
                         (when on-blur (on-blur)))
   :on-change-text    #(do
                         (text-input-on-change-text % props)
                         (when on-change-text (on-change-text %)))})

(defn text-input-with-label [options]
  (let [props (get-init-props options)]
    (fn [{:keys [label description error hide-underline? auto-expanding multiline] :as options}]
      [react/view styles/component-container
       [react/animated-text {:style (styles/label-animated-text props)} label]
       [react/text-input (merge styles/text-input
                                (when-not (and auto-expanding multiline)
                                  styles/content-height)
                                (dissoc options :label :description :error :auto-expanding :hide-underline?)
                                (text-input-handlers options props))]
       (when-not hide-underline?
         [react/view {:style (styles/underline-blured error)
                      :on-layout #(reset! (:underline-max-width* props) (get-width %))}
          [react/animated-view {:style (styles/underline-focused (:underline-width props)
                                                                 (:underline-height props)
                                                                 error)}]])
       (cond error
             [react/text {:style styles/error-text} error]
             description
             [react/text {:style styles/description-text} description])])))
