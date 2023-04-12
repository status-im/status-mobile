(ns status-im2.contexts.chat.messages.content.style
  (:require [quo2.foundations.colors :as colors]))

(defn message-container
  [in-pinned-view? pinned-by mentioned last-in-group?]
  (merge (when (and (not in-pinned-view?) (or mentioned pinned-by))
           {:background-color colors/primary-50-opa-5
            :margin-bottom    4})
         (when (or mentioned pinned-by last-in-group?)
           {:margin-top 8})
         {:border-radius 16}))
