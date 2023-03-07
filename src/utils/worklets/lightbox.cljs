(ns utils.worklets.lightbox)

(def ^:private layout-worklets (js/require "../src/js/worklets/lightbox.js"))

(defn info-layout
  [input isTop]
  (.infoLayout ^js layout-worklets input isTop))
