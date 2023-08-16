(ns quo2.components.wallet.progress-bar.style
  (:require [quo2.foundations.colors :as colors]))

(defn- border-and-background-color
  [customization-color]
  {:light {:pending   {:border-color     colors/neutral-80-opa-5
                       :background-color colors/neutral-5}
           :confirmed {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/custom-color :success 50)}
           :finalized {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/custom-color customization-color 50)}
           :error     {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/custom-color :danger 50)}}
   :dark  {:pending   {:border-color     colors/neutral-70
                       :background-color colors/neutral-80}
           :confirmed {:border-color     colors/white-opa-5
                       :background-color (colors/custom-color :success 60)}
           :finalized {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/custom-color customization-color 60)}
           :error     {:border-color     colors/white-opa-5
                       :background-color (colors/custom-color :danger 60)}}})

(defn root-container
  [{:keys [customization-color state theme]}]
  (let [{:keys [background-color border-color]} (get-in (border-and-background-color customization-color)
                                                        [theme state])]
    {:height           12
     :width            8
     :border-radius    3
     :border-width     1
     :border-color     border-color
     :background-color background-color}))
