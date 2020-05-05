(ns status-im.ui.components.toolbar.actions
  (:require [re-frame.core :as re-frame]))

(defn opts [options]
  {:icon    :main-icons/more
   :options options})

(defn back [handler]
  {:icon                :main-icons/back
   :handler             handler
   :accessibility-label :back-button})

(def default-handler #(re-frame/dispatch [:navigate-back]))

(def default-back
  (back default-handler))

(defn close [handler]
  {:icon    :main-icons/close
   :handler handler
   :accessibility-label :done-button})

(def default-close
  (close default-handler))
