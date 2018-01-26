(ns status-im.ui.components.toolbar.actions
  (:require [re-frame.core :as re-frame]))

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
  {:icon      :icons/search
   :icon-opts {:container-style {:opacity 0.4}}})

(defn back [handler]
  {:icon                :icons/back
   :handler             handler
   :accessibility-label :toolbar-back-button})

(def default-handler #(re-frame/dispatch [:navigate-back]))

(def default-back
  (back default-handler))

(defn back-white [handler]
  {:icon      :icons/back
   :icon-opts {:color :white}
   :handler   handler})

(defn close [handler]
  {:icon    :icons/close
   :handler handler})

(def default-close
  (close default-handler))

(defn close-white [handler]
  {:icon      :icons/close
   :icon-opts {:color :white}
   :handler   handler})

(defn list-white [handler]
  {:icon      :icons/transaction-history
   :icon-opts {:color :white}
   :handler   handler})

(defn add-wallet [handler]
  {:icon    :icons/add-wallet
   :handler handler})
