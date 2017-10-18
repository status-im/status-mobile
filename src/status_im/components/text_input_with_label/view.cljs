(ns status-im.components.text-input-with-label.view
  (:require [reagent.core :as r]
            [status-im.components.animation :as animation]
            [status-im.components.text-input-with-label.animation :refer [animate-label
                                                                          text-input-on-focus
                                                                          text-input-on-blur]]
            [status-im.components.react :refer [view
                                                text
                                                animated-text
                                                animated-view
                                                text-input]]
            [status-im.components.text-input-with-label.styles :as st]
            [clojure.string :as str]))

(defn get-init-props [{:keys [default-value]}]
  (let [blank? (str/blank? default-value)]
     {:underline-width        (animation/create-value 0)
      :underline-height       (animation/create-value 1)
      :label-top              (animation/create-value (if blank?
                                                        st/label-top-bottom
                                                        st/label-top-top))
      :label-font-size        (animation/create-value (if blank?
                                                        st/label-font-size-bottom
                                                        st/label-font-size-top))
      :label-top-top          st/label-top-top
      :label-top-bottom       st/label-top-bottom
      :label-font-size-top    st/label-font-size-top
      :label-font-size-bottom st/label-font-size-bottom
      :underline-max-height   st/underline-max-height
      :content-height*        (r/atom nil)
      :input-ref*             (r/atom nil)
      :value*                 (r/atom default-value)
      :underline-max-width*   (r/atom 0)}))

(defn get-width [event]
  (.-width (.-layout (.-nativeEvent event))))

(defn text-input-on-change-text [text props]
  (animate-label text props)
  (reset! (:value* props) text))

(defn text-input-handlers [{:keys [on-focus on-blur on-change-text on-change on-submit-editing
                                   auto-expanding multiline max-height ref]} props]
  {:ref               #(do
                         (reset! (:input-ref* props) %)
                         (when ref (ref %)))
   :on-change         #(do
                         (when (and auto-expanding multiline)
                           (reset! (:content-height* props) (min (.-height (.-contentSize (.-nativeEvent %)))
                                                                 max-height)))
                         (when on-change (on-change %)))
   :on-submit-editing #(do
                         (.blur @(:input-ref* props))
                         (when on-submit-editing (on-submit-editing)))
   :on-focus          #(do
                         (text-input-on-focus props)
                         (when on-focus (on-focus)))
   :on-blur           #(do
                         (text-input-on-blur props)
                         (when on-blur (on-blur)))
   :on-change-text    #(do
                         (text-input-on-change-text % props)
                         (when on-change-text (on-change-text %)))})

(defn text-input-with-label [options]
  (let [props (get-init-props options)]
    (fn [{:keys [label description error hide-underline?] :as options}]
      [view st/component-container
       [animated-text {:style (st/label-animated-text props)} label]
       [text-input (merge (st/text-input @(:content-height* props))
                          (dissoc options :label :description :error :hide-underline?)
                          (text-input-handlers options props))]
       (when-not hide-underline?
         [view {:style (st/underline-blured error)
                :on-layout #(reset! (:underline-max-width* props) (get-width %))}
          [animated-view {:style (st/underline-focused
                                   (:underline-width props)
                                   (:underline-height props)
                                   error)}]])
       (cond error
             [text {:style st/error-text} error]
             description
             [text {:style st/description-text} description])])))
