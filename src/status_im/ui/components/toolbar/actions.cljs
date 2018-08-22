(ns status-im.ui.components.toolbar.actions
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.styles :as styles]))

(defn add [illuminated? handler]
  {:icon      :icons/add
   :icon-opts (if illuminated? styles/icon-add-illuminated styles/icon-add)
   :handler   handler})

(defn opts [options]
  {:icon    :icons/options
   :options options})

(defn back [handler]
  {:icon                :icons/back
   :handler             handler
   :accessibility-label :back-button})

(def default-handler #(re-frame/dispatch [:navigate-back]))

(def home-handler #(re-frame/dispatch [:navigate-to :home]))

(def default-back
  (back default-handler))

(def home-back
  (back home-handler))

(defn back-white [handler]
  {:icon                :icons/back
   :icon-opts           {:color :white}
   :handler             handler
   :accessibility-label :back-button})

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
