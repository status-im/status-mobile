(ns status-im.ui.screens.chat.state)

(defonce first-not-visible-item (atom nil))

(defn reset []
  (reset! first-not-visible-item nil))
