(ns status-im.common.floating-button-page.floating-container.style
  (:require [quo.foundations.colors :as colors]
            [react-native.safe-area :as safe-area]))

(defn content-container
  [blur? keyboard-shown? {:keys [padding-vertical padding-horizontal]}]
  (let [margin-bottom (if keyboard-shown? 0 safe-area/bottom)]
    (cond-> {:margin-top         :auto
             :overflow           :hidden
             :margin-bottom      margin-bottom
             :padding-vertical   (or padding-vertical 12)
             :padding-horizontal (or padding-horizontal 20)}
      blur? (dissoc :padding-vertical :padding-horizontal))))

(defn blur-inner-container
  [{:keys [theme shell-overlay? padding-vertical padding-horizontal background-color]}]
  {:background-color   (or background-color
                           (colors/theme-colors
                            colors/white-70-blur
                            (if shell-overlay?
                              colors/neutral-80-opa-80-blur
                              colors/neutral-95-opa-70-blur)
                            theme))
   :padding-vertical   (or padding-vertical 12)
   :padding-horizontal (or padding-horizontal 20)})
