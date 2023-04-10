(ns status-im2.contexts.chat.messages.bottom-sheet-composer.actions.style
  (:require [status-im2.contexts.chat.messages.bottom-sheet-composer.constants :as c]))


(defn actions-container
  []
  {:height          c/actions-container-height
   :justify-content :space-between
   :align-items     :center
   :z-index         2
   :flex-direction  :row})
