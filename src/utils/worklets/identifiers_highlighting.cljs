(ns utils.worklets.identifiers-highlighting)

(def worklets (js/require "../src/js/worklets/identifiers_highlighting.js"))

(defn background-style
  [color progress]
  (.backgroundStyle ^js worklets color progress))

(defn opacity
  [progress]
  (.opacity ^js worklets progress))

(defn ring-style
  [progress]
  (.ringStyle ^js worklets progress))

(defn user-hash-style
  [color progress]
  (.userHashStyle ^js worklets color progress))

(defn emoji-hash-style
  [progress]
  (.emojiHashStyle ^js worklets progress))