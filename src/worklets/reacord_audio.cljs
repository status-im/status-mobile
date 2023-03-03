(ns worklets.reacord-audio)

(def ^:private record-audio-worklets (js/require "../src/worklets/js/record_audio.js"))

(defn ring-scale
  [scale substract]
  (.ringScale ^js record-audio-worklets scale substract))
