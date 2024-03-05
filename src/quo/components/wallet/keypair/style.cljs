(ns quo.components.wallet.keypair.style
  (:require
    [quo.foundations.colors :as colors]
    [react-native.platform :as platform]))

(defn container
  [{:keys [blur? customization-color theme selected?]}]
  {:border-radius  16
   :border-width   1
   :border-color   (if selected?
                     (if blur?
                       colors/white
                       (colors/theme-colors (colors/custom-color customization-color 50)
                                            (colors/custom-color customization-color 60)
                                            theme))
                     (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-10 colors/neutral-80 theme)))
   :padding-bottom 8})

(def header-container
  {:padding-horizontal 12
   :padding-top        8
   :padding-bottom     12
   :flex-direction     :row
   :align-items        :center})

(def title-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :space-between})

(defn subtitle
  [blur? theme]
  {:color (if blur?
            colors/white-opa-40
            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})

(defn dot
  [blur? theme]
  (merge (subtitle blur? theme)
         {:bottom (if platform/ios? 2 -2)}))
