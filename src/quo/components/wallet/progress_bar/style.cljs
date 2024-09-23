(ns quo.components.wallet.progress-bar.style
  (:require
    [quo.foundations.colors :as colors]))

(defn- border-and-background-color
  [customization-color theme]
  {:light {:pending   {:border-color     colors/neutral-80-opa-5
                       :background-color colors/neutral-5}
           :confirmed {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color :success theme)}
           :finalized {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color customization-color theme)}
           :error     {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color :danger theme)}}
   :dark  {:pending   {:border-color     colors/neutral-70
                       :background-color colors/neutral-80}
           :confirmed {:border-color     colors/white-opa-5
                       :background-color (colors/resolve-color :success theme)}
           :finalized {:border-color     colors/neutral-80-opa-5
                       :background-color (colors/resolve-color customization-color theme)}
           :error     {:border-color     colors/white-opa-5
                       :background-color (colors/resolve-color :danger theme)}}})

(def max-value 100)

(defn root-container
  [{:keys [customization-color state full-width?]} theme]
  (let [{:keys [background-color border-color]} (get-in (border-and-background-color customization-color
                                                                                     theme)
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
  [{:keys [customization-color state progressed-value]} theme]
  (let [{:keys [background-color]} (get-in (border-and-background-color customization-color theme)
                                           [theme state])
        progress                   (if (> progressed-value max-value) max-value progressed-value)]
    {:height           12
     :margin-top       -1
     :width            (str progress "%")
     :border-radius    3
     :background-color background-color}))
