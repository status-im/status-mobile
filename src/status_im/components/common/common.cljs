(ns status-im.components.common.common
  (:require [status-im.components.react :refer [view text linear-gradient]]
            [status-im.utils.platform :as p]
            [status-im.components.common.styles :as st]))

(defn top-shaddow []
  (when p/android?
    [linear-gradient
     {:style  st/gradient-bottom
      :colors st/gradient-bottom-colors}]))

(defn bottom-shaddow []
  (when p/android?
    [linear-gradient
     {:style  st/gradient-top
      :colors st/gradient-top-colors}]))

(defn separator [style]
  [view st/separator-wrapper
   [view (merge st/separator style)]])

(defn list-separator []
  [separator st/list-separator])

(defn form-title [label & [count-value]]
  [view
   [view st/form-title-container
    [text {:style st/form-title
           :font  :medium}
     label]
    (when-not (nil? count-value)
      [text {:style st/form-title-count
             :font  :medium}
       count-value])]
   [top-shaddow]])
