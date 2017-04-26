(ns status-im.components.common.common
  (:require [status-im.components.react :refer [view text icon linear-gradient]]
            [status-im.components.context-menu :refer [context-menu]]
            [status-im.utils.platform :as p]
            [status-im.components.common.styles :as st]))

(defn top-shadow []
  (when p/android?
    [linear-gradient
     {:style  st/gradient-bottom
      :colors st/gradient-top-colors}]))

(defn bottom-shadow []
  (when p/android?
    [linear-gradient
     {:style  st/gradient-top
      :colors st/gradient-bottom-colors}]))

(defn separator [style & [wrapper-style]]
  [view (merge st/separator-wrapper wrapper-style)
   [view (merge st/separator style)]])

(defn form-spacer []
  [view
   [bottom-shadow]
   [view st/form-spacer]
   [top-shadow]])

(defn list-separator []
  [separator st/list-separator])

(defn list-footer []
  [view st/list-header-footer-spacing])

(defn list-header []
 [view st/list-header-footer-spacing])

(defn form-title [label & [{:keys [count-value extended? options]}]]
  [view
   [view st/form-title-container
    [view st/form-title-inner-container
     [text {:style st/form-title
            :font  :medium}
      label]
     (when-not (nil? count-value)
       [text {:style st/form-title-count
              :font  :medium}
        count-value])]
    (when extended?
      [view
       [view {:flex 1}]])
    (when extended?
      [view st/form-title-extend-container
       [context-menu
        [icon :options_gray]
        options
        nil
        st/form-title-extend-button]])]
   [top-shadow]])
