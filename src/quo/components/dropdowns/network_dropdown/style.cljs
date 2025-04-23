(ns quo.components.dropdowns.network-dropdown.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container-border-color
  [{:keys [state blur? theme pressed?]}]
  (let [default-color (if blur?
                        (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme)
                        (colors/theme-colors colors/neutral-30 colors/neutral-70 theme))
        active-color  (if blur?
                        (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
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

(defn dropdown-text
  [theme]
  {:color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :padding-left 8})

(def dropdown-icon-container
  {:padding-left 8})

(defn dropdown-icon-colors
  [theme]
  {:background (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :foreground (colors/theme-colors colors/neutral-100 colors/white theme)})

(def new-chain-indicator
  {:position :absolute
   :right    0})
