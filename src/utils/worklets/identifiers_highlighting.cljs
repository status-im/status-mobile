(ns utils.worklets.identifiers-highlighting)

(def worklets (js/require "../src/js/worklets/identifiers_highlighting.js"))

(defn background
  [color progress]
  (.background ^js worklets color progress))

(defn opacity
  [progress]
  (.opacity ^js worklets progress))

(defn avatar-opacity
  [progress]
  (.avatarOpacity ^js worklets progress))

(defn ring-opacity
  [progress]
  (.ringOpacity ^js worklets progress))

(defn user-hash-color
  [progress]
  (.userHashColor ^js worklets progress))

(defn user-hash-opacity
  [progress]
  (.userHashOpacity ^js worklets progress))

(defn emoji-hash-style
  [progress]
  (.emojiHashStyle ^js worklets progress))
