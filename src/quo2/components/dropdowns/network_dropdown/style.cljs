(ns quo2.components.dropdowns.network-dropdown.style
  (:require [quo2.foundations.colors :as colors]))

(defn container-border-color
  [{:keys [state blur? theme pressed?]}]
  (let [default-color (if blur?
                        colors/neutral-80-opa-5
                        (colors/theme-colors colors/neutral-30 colors/neutral-70 theme))
        active-color  (if blur?
                        colors/neutral-80-opa-10
                        (colors/theme-colors colors/neutral-40 colors/neutral-60 theme))]
    (condp = state
      :disabled default-color
      :default  (if pressed? active-color default-color))))

(defn dropdown-container
  [{:keys [state] :as props}]
  {:border-width       1
   :border-radius      10
   :padding-horizontal 8
   :padding-vertical   6
   :opacity            (if (= state :disabled) 0.3 1)
   :border-color       (container-border-color props)
   :align-items        :center})
