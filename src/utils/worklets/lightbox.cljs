(ns utils.worklets.lightbox)

(def ^:private layout-worklets (js/require "../src/js/worklets/lightbox.js"))

(defn info-layout
  [input top?]
  (.infoLayout ^js layout-worklets input top?))
