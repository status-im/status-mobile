(ns status-im2.common.floating-button-page.floating-container.style
  (:require [react-native.safe-area :as safe-area]))

(defn content-container
  [blur? keyboard-shown?]
  (let [margin-bottom (if keyboard-shown? 0 (safe-area/get-bottom))]
    (cond-> {:margin-top         :auto
             :overflow           :hidden
             :margin-bottom      margin-bottom
             :padding-vertical   12
             :padding-horizontal 20}
      blur? (dissoc :padding-vertical :padding-horizontal))))

(def blur-inner-container
  {:background-color   :transparent ; required, otherwise blur-view will shrink
   :padding-vertical   12
   :padding-horizontal 20})
