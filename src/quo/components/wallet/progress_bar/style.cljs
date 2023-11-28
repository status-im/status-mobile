(ns quo.components.wallet.progress-bar.style
  (:require
    [quo.foundations.colors :as colors]))

(defn- border-and-background-color
  [customization-color theme]
  {:light {:pending   {:border-color     colors/neutral-80-opa-5
                       :background-color colors/neutral-5}
           :confirmed {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color :success theme 50)}
           :finalized {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color customization-color theme 50)}
           :error     {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color :danger theme 50)}}
   :dark  {:pending   {:border-color     colors/neutral-70
                       :background-color colors/neutral-80}
           :confirmed {:border-color     colors/white-opa-5
                       :background-color (colors/resolve-color :success theme 60)}
           :finalized {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color customization-color theme 60)}
           :error     {:border-color     colors/white-opa-5
                       :background-color (colors/resolve-color :danger theme 60)}}})

(defn root-container
  [{:keys [customization-color state theme full-width?]}]
  (let [{:keys [background-color border-color]} (get-in (border-and-background-color customization-color theme)
                                                        [theme (if full-width? :pending state)])]
    {:height            12
     :flex              (when full-width? 1)
     :width             (when (not full-width?) 8)
     :border-radius     3
     :border-width      1
     :border-color      border-color
     :background-color  background-color
     :margin-horizontal 1
     :margin-vertical   2}))

(defn progressed-bar
  [{:keys [customization-color state theme progressed-value]}]
  (let [{:keys [background-color]} (get-in (border-and-background-color customization-color theme)
                                           [theme state])
        progress                   (if (> progressed-value 100) 100 progressed-value)]
    {:height           12
     :margin-top       -1
     :width            (str progress "%")
     :border-radius    3
     :background-color background-color}))
