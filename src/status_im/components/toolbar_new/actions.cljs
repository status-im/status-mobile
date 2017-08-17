(ns status-im.components.toolbar-new.actions
  (:require [re-frame.core :refer [dispatch]]
            [status-im.accessibility-ids :as id]
            [status-im.components.toolbar-new.styles :as st]))

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

(defn opts [options]
  {:image   {:source {:uri :icon_options_dark}
             :style  st/action-default}
   :options options})

(defn search [handler]
  {:image   {:source {:uri :icon_search_dark}
             :style  st/action-default}
   :handler handler})

(def search-icon
  {:image   {:source {:uri :icon_search_dark}
             :style  (merge st/action-default
                            {:opacity 0.4})}})

(defn back [handler]
  {:image   {:source {:uri :icon_back_dark}
             :style  st/action-default}
   :handler handler
   :accessibility-label id/toolbar-back-button})

(def default-back
  (back #(dispatch [:navigate-back])))

(defn back-white [handler]
  {:image   {:source {:uri :icon_back_white}
             :style  st/action-default}
   :handler handler})

(defn close [handler]
  {:image   {:source {:uri :icon_close_dark}
             :style  st/action-default}
   :handler handler})

(defn close-white [handler]
  {:image   {:source {:uri :icon_close_white}
             :style  st/action-default}
   :handler handler})

(defn list-white [handler]
  {:image   {:source {:uri :icon_list_white}
             :style  st/action-default}
   :handler handler})

(defn add-wallet [handler]
  {:image   {:source {:uri :icon_add_wallet_dark}
             :style  st/action-default}
   :handler handler})
