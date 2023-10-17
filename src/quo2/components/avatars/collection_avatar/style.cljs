(ns quo2.components.avatars.collection-avatar.style
  (:require
    [quo2.foundations.colors :as colors]))

(defn collection-avatar
  [theme]
  {:width         24
   :height        24
   :border-width  1
   :border-color  (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
   :border-radius 6})
