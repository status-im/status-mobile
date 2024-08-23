(ns status-im.common.events-helper
  "Provides highly reusable dispatch functions to ensure consistency and avoid duplication in managing various events,
  including navigation and other actions that affect application behavior."
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
