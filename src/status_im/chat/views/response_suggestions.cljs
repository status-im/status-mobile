(ns status-im.chat.views.response-suggestions
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]))

(defview response-suggestions-view []
  [suggestions [:get-content-suggestions]]
  (when (seq suggestions) suggestions))
