(ns status-im.ui.components.toolbar.actions
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.toolbar.styles :as styles]
            [status-im.ui.components.colors :as colors]))

(defn add [illuminated? handler]
  {:icon      :main-icons/add
   :icon-opts (if illuminated? styles/icon-add-illuminated styles/icon-add)
   :handler   handler})

(defn opts [options]
  {:icon    :main-icons/more
   :options options})

(defn back [handler]
  {:icon                :main-icons/back
   :handler             handler
   :accessibility-label :back-button})

(def default-handler #(re-frame/dispatch [:navigate-back]))

(def home-handler #(re-frame/dispatch [:navigate-to :home]))

(def default-back
  (back default-handler))

(def home-back
  (back home-handler))

(defn back-white [handler]
  {:icon                :main-icons/back
   :icon-opts           {:color colors/white}
   :handler             handler
   :accessibility-label :back-button})

(defn close [handler]
  {:icon    :main-icons/close
   :handler handler
   :accessibility-label :done-button})

(def default-close
  (close default-handler))

(defn close-white [handler]
  {:icon      :main-icons/close
   :icon-opts {:color colors/white}
   :handler   handler})

(defn list-white [handler]
  {:icon      :main-icons/two-arrows
   :icon-opts {:color colors/white}
   :handler   handler})
