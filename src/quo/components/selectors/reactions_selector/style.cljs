(ns quo.components.selectors.reactions-selector.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn container
  [pressed? theme]
  (merge
   {:border-radius    12
    :border-width     1
    :border-color     (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
    :background-color (when pressed?
                        (colors/theme-colors colors/neutral-10 colors/neutral-80-opa-40 theme))}
   (if platform/ios?
     {:padding            9.5
      :padding-horizontal 8}
     {:padding 8})))

(def emoji-text-style
  (merge
   {:line-height 20
    :text-align  :center}
   (if platform/ios?
     {:line-height 20
      :font-size   17
      :text-align  :center}
     {:font-size           18
      :height              24
      :width               24
      :text-align          :center
      :text-align-vertical :center})))
