(ns messenger.models.navigation
  (:require [messenger.state :as state]))

(defn set-current-screen-class [class]
  (swap! state/app-state assoc-in [:current-screen-class] class))

(defn current-screen-class []
  (get-in @state/app-state [:current-screen-class]))
