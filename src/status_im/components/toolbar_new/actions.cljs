(ns status-im.components.toolbar-new.actions
  (:require [re-frame.core :refer [dispatch]]
            [status-im.accessibility-ids :as id]
            [status-im.components.toolbar-new.styles :as st]))

(defn hamburger [handler]
  {:icon    :icons/hamburger
   :handler handler})

(defn add [handler]
  {:icon    :icons/add
   :handler handler})

(defn opts [options]
  {:icon    :icons/options
   :options options})

(defn search [handler]
  {:icon    :icons/search
   :handler handler})

(def search-icon
  {:icon [:icons/search
          {:container-style {:opacity 0.4}}]})

(defn back [handler]
  {:icon                :icons/back
   :handler             handler
   :accessibility-label id/toolbar-back-button})

(def default-back
  (back #(dispatch [:navigate-back])))

(defn back-white [handler]
  {:icon    [:icons/back
             {:color :white}]
   :handler handler})

(defn close [handler]
  {:icon    :icons/close
   :handler handler})

(defn close-white [handler]
  {:icon    [:icons/close
             {:color :white}]
   :handler handler})

(defn list-white [handler]
  {:icon    [:icons/transaction_history
             {:color :white :style {:viewBox "-108 65.9 24 24"}}]
   :handler handler})

(defn add-wallet [handler]
  {:image   {:source {:uri :icon_add_wallet_dark}
             :style  st/action-default}
   :handler handler})
