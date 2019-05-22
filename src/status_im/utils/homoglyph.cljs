(ns status-im.utils.homoglyph
  (:require [status-im.js-dependencies :as dependencies]))

(defn matches [s1 s2]
  (.isMatches (dependencies/homoglyph-finder) s1 s2))
