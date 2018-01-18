(ns status-im.ui.components.bottom-buttons.view
  (:require [status-im.ui.components.bottom-buttons.styles :as styles]
            [status-im.ui.components.react :as react]))

(defn bottom-button
  ([button] (bottom-button nil button))
  ([style button]
   [react/view styles/wrapper
    [react/view styles/border]
    [react/view (merge styles/container-single style)
     button]]))

(defn bottom-buttons
  ([left right] (bottom-buttons nil left right))
  ([style left right]
   [react/view styles/wrapper
    [react/view styles/border]
    [react/view (merge styles/container style)
     left
     [react/view {:flex 1}]
     right]]))
