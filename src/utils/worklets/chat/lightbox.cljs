(ns utils.worklets.chat.lightbox)

(def ^:private layout-worklets (js/require "../src/js/worklets/chat/lightbox.js"))

(defn info-layout
  [input top?]
  (.infoLayout ^js layout-worklets input top?))
