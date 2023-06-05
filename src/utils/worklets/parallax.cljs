(ns utils.worklets.parallax)

(def ^:private parallax-worklets (js/require "../src/js/worklets/parallax.js"))

(defn sensor-animated-image
  [order offset]
  (.sensorAnimatedImage ^js parallax-worklets order offset))
