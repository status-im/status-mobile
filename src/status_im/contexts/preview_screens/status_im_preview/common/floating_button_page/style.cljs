(ns status-im.contexts.preview-screens.status-im-preview.common.floating-button-page.style
  (:require [quo.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]))

(defn container
  []
  {:flex       1
   :margin-top (safe-area/get-top)})

(def background-image
  {:position :absolute
   :top      (- (safe-area/get-top))
   :left     0
   :right    0
   :bottom   200})

(defn page-content
  [height]
  {:flex             1
   :height           height
   :overflow         :hidden
   :background-color (colors/resolve-color :army 30)})
