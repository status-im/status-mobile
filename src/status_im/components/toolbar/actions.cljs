(ns status-im.components.toolbar.actions
  (:require [status-im.components.toolbar.styles :as st]))

(def nothing
  {:image   {:source nil
             :style  st/action-default}})

(defn hamburger [handler]
  {:image   {:source {:uri :icon_hamburger_dark}
             :style  st/action-default}
   :handler handler})

(defn add [handler]
  {:image   {:source {:uri :icon_add}
             :style  st/action-default}
   :handler handler})

(defn opts [handler]
  {:image   {:source {:uri :icon_options_dark}
             :style  st/action-default}
   :handler handler})

(defn search [handler]
  {:image   {:source {:uri :icon_search_dark}
             :style  st/action-default}
   :handler handler})

(defn back [handler]
  {:image   {:source {:uri :icon_back_dark}
             :style  st/action-default}
   :handler handler})

(defn back-white [handler]
  {:image   {:source {:uri :icon_back_white}
             :style  st/action-default}
   :handler handler})
