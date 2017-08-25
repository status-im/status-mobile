(ns status-im.components.toolbar-new.actions
  (:require [re-frame.core :refer [dispatch]]
            [status-im.accessibility-ids :as id]
            [status-im.components.toolbar-new.styles :as st]))

(defn hamburger [handler]
  {:icon    :hamburger
   :handler handler})

(defn add [handler]
  {:icon    :add
   :handler handler})

(defn opts [options]
  {:icon    :options
   :options options})

(defn search [handler]
  {:icon    :search
   :handler handler})

(def search-icon
  {:icon {:source {:name :search}
          :style  {:opacity 0.4}}})

(defn back [handler]
  {:icon                :back
   :handler             handler
   :accessibility-label id/toolbar-back-button})

(def default-back
  (back #(dispatch [:navigate-back])))

(defn back-white [handler]
  {:icon    {:source {:name  :back
                      :color :white}}
   :handler handler})

(defn close [handler]
  {:icon    :close
   :handler handler})

(defn close-white [handler]
  {:icon    {:source {:name  :close
                      :color :white}}
   :handler handler})

(defn list-white [handler]
  {:image   {:source {:uri :icon_list_white}
             :style  st/action-default}
   :handler handler})
