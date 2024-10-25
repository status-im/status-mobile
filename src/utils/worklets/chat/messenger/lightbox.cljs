(ns utils.worklets.chat.messenger.lightbox)

(def ^:private layout-worklets (js/require "../src/js/worklets/chat/messenger/lightbox.js"))

(defn info-layout
  [input top?]
  (.infoLayout ^js layout-worklets input top?))
