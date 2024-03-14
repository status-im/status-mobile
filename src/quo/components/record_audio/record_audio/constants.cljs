(ns quo.components.record-audio.record-audio.constants
  (:require [react-native.audio-toolkit :as audio]
            [react-native.platform :as platform]
            [utils.datetime :as datetime]))

(def min-audio-duration-ms 1000)
(def max-audio-duration-ms (if platform/ios? 120800 120500))
(def metering-interval 25)
(def base-filename "am")
(def default-format ".aac")

(def min-touch-duration 150)

(def record-button-area-big
  {:width  56
   :height 56
   :x      64
   :y      64})

(def record-button-area
  {:width  48
   :height 48
   :x      68
   :y      68})

(defn delete-button-area
  [{:keys [active? reviewing-audio?]}]
  {:width  (cond
             active?          72
             reviewing-audio? 32
             :else            82)
   :height (if reviewing-audio? 32 56)
   :x      (cond
             active?          -16
             reviewing-audio? 36
             :else            -32)
   :y      (cond
             active?          64
             reviewing-audio? 76
             :else            70)})

(defn lock-button-area
  [{:keys [active?]}]
  {:width  (if active? 72 100)
   :height (if active? 72 102)
   :x      -32
   :y      -32})

(defn send-button-area
  [{:keys [active? reviewing-audio?]}]
  {:width  (if reviewing-audio? 32 56)
   :height (cond
             active?          72
             reviewing-audio? 47
             :else            92)
   :x      (if reviewing-audio? 76 32)
   :y      (cond
             active?          -16
             reviewing-audio? 76
             :else            -32)})

(defn touch-inside-area?
  [{:keys [location-x location-y ignore-min-y? ignore-max-y? ignore-min-x? ignore-max-x?]}
   {:keys [width height x y]}]
  (let [max-x (+ x width)
        max-y (+ y height)]
    (and
     (and
      (or ignore-min-x? (>= location-x x))
      (or ignore-max-x? (<= location-x max-x)))
     (and
      (or ignore-min-y? (>= location-y y))
      (or ignore-max-y? (<= location-y max-y))))))

(def rec-options
  (merge
   audio/default-recorder-options
   {:filename         (str base-filename
                           (datetime/timestamp)
                           default-format)
    :meteringInterval metering-interval}))
