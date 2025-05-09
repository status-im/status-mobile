(ns status-im.contexts.chat.messenger.messages.list.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.safe-area :as safe-area]
    [status-im.contexts.chat.messenger.messages.constants :as messages.constants]))

(def permission-context-height 46)
(def distance-from-last-message 4)

(defn keyboard-avoiding-container
  [theme]
  {:flex             1
   :background-color (colors/theme-colors colors/white colors/neutral-95 theme)
   :z-index          2})

(def permission-context-sheet {:flex 3}) ; Pushes composer to bottom

(defn list-paddings
  [add-padding-bottom?]
  (let [{:keys [top bottom]} safe-area/insets]
    ;; WARNING: the flat-list is reversed, so the paddings are applied inverted
    {:padding-top    (if add-padding-bottom?
                       (+ distance-from-last-message permission-context-height bottom)
                       distance-from-last-message)
     :padding-bottom (+ top messages.constants/top-bar-height)}))
