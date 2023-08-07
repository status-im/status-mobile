(ns quo2.components.wallet.progress-bar.style
  (:require [quo2.foundations.colors :as colors]))

(defn- border-and-background-color
  [customization-color]
  {:light {:pending   {:background-color colors/neutral-5
                       :border-color     colors/neutral-80-opa-5}
           :confirmed {:background-color colors/success-50
                       :border-color     colors/neutral-80-opa-5}
           :finalized {:background-color (colors/custom-color customization-color 50)
                       :border-color     colors/neutral-80-opa-5}
           :error     {:background-color colors/danger-50
                       :border-color     colors/neutral-80-opa-5}}
   :dark  {:pending   {:background-color colors/neutral-80
                       :border-color     colors/neutral-70}
           :confirmed {:background-color colors/success-60
                       :border-color     colors/white-opa-5}
           :finalized {:background-color (colors/custom-color customization-color 60)
                       :border-color     colors/neutral-80-opa-5}
           :error     {:background-color colors/danger-60
                       :border-color     colors/white-opa-5}}})

(defn root-container
  [{:keys [customization-color state theme]
    :or   {customization-color :blue
           state               :pending
           theme               :light}}]
  (let [{:keys [background-color border-color]} (get-in (border-and-background-color customization-color)
                                                        [theme state])]
    {:height           12
     :width            8
     :border-radius    3
     :border-width     1
     :border-color     border-color
     :background-color background-color}))
