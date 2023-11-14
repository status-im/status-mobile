(ns quo.components.wallet.address-text.style
  (:require [quo.foundations.colors :as colors]))

(defn address-text
  [format blur? theme]
  (when (= format :short)
    {:color (if blur?
              colors/white-opa-40
              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}))
