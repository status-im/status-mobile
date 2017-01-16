(ns status-im.utils.homoglyph)

(def homoglyph-finder (js/require "homoglyph-finder"))

(defn matches [s1 s2]
  (.isMatches homoglyph-finder s1 s2))
