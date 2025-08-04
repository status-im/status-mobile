(ns status-im.contexts.shell.home-stack.style
  (:require [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.contexts.shell.constants :as shell.constants]))

(defn stack-view
  [shared-values stack-id]
  [{:opacity (->> stack-id
                  (get shell.constants/stacks-opacity-keywords)
                  (get shared-values))
    :z-index (->> stack-id
                  (get shell.constants/stacks-z-index-keywords)
                  (get shared-values))}
   rn/stylesheet-absolute-fill])

(defn home-stack
  [theme]
  {:border-bottom-left-radius  20
   :z-index                    2
   :border-bottom-right-radius 20
   :background-color           (colors/theme-colors colors/white colors/neutral-95 theme)
   :flex                       1})
