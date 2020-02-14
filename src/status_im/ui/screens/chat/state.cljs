(ns status-im.ui.screens.chat.state)

(defonce viewable-item (atom nil))

(defn reset []
  (reset! viewable-item nil))
