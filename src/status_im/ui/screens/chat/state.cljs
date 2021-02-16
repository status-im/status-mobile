(ns status-im.ui.screens.chat.state)

(defonce first-not-visible-item (atom nil))

(defonce scrolling (atom nil))

(defn reset-visible-item []
  (reset! first-not-visible-item nil))
