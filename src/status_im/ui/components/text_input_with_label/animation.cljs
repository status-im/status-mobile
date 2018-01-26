(ns status-im.ui.components.text-input-with-label.animation
  (:require [status-im.ui.components.animation :as animation]
            [clojure.string :as string]))

(def anim-duration 200)

(defn animate-underline [underline-width to-line-width underline-height to-line-height]
  (let [anim (animation/parallel [(animation/timing underline-width {:toValue  to-line-width
                                                                     :duration anim-duration})
                                  (animation/timing underline-height {:toValue  to-line-height
                                                                      :duration anim-duration})])]
    (animation/start anim)))

(defn text-input-on-focus [{:keys [underline-width underline-max-width* underline-height underline-max-height]}]
  (animate-underline underline-width @underline-max-width* underline-height underline-max-height))

(defn text-input-on-blur [{:keys [underline-width underline-height]}]
  (animate-underline underline-width 0 underline-height 1))

(defn animate-label [text {:keys [value* label-top label-font-size
                                  label-top-top label-top-bottom label-font-size-top label-font-size-bottom]}]
  (when (or (string/blank? text) (string/blank? @value*))
    (let [was-blank? (string/blank? @value*)
          anim (animation/parallel [(animation/timing label-top {:toValue  (if was-blank?
                                                                              label-top-top
                                                                              label-top-bottom)
                                                                 :duration anim-duration})
                                    (animation/timing label-font-size {:toValue  (if was-blank?
                                                                                    label-font-size-top
                                                                                     label-font-size-bottom)
                                                                       :duration anim-duration})])]
      (animation/start anim))))