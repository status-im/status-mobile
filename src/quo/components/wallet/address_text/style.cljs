(ns quo.components.wallet.address-text.style
  (:require [quo.foundations.colors :as colors]))

(defn address-text
  [format blur? theme]
  (if (and (= format :long) blur?)
    {:color colors/white}
    (when (= format :short)
      {:color (if blur?
                colors/white-opa-40
                (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))})))
