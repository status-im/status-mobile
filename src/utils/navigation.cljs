(ns utils.navigation
  "Prefer to declare in this namespace only reusable navigation functions that
  aren't better suited in a particular bounded context."
  (:require [utils.re-frame :as rf]))

(defn navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn hide-bottom-sheet
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn dismiss-keyboard
  []
  (rf/dispatch [:dismiss-keyboard]))
