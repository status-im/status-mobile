(ns legacy.status-im.ui.screens.wallet.components.views
  (:require
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.wallet.components.styles :as styles]))

(defn separator
  []
  [react/view (styles/separator)])

(defn separator-dark
  []
  [react/view (styles/separator-dark)])

(defn token-icon
  [{:keys [style source image-style width height]}]
  [react/view {:style style}
   [react/image
    {:source (if (fn? source) (source) source)
     :style  (merge
              {:width  (or width 40)
               :height (or height 40)}
              image-style)}]])
