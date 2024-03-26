(ns quo.components.settings.page-setting.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [theme]
  {:flex-direction     :row
   :justify-content    :space-between
   :background-color   (colors/theme-colors colors/neutral-2_5 colors/neutral-90 theme)
   :padding-vertical   13
   :padding-horizontal 12
   :border-width       1
   :border-color       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)
   :border-radius      16})

