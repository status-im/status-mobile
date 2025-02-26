(ns quo.components.list-items.saved-contact-address.style
  (:require
    [quo.foundations.colors :as colors]))

(defn- background-color
  [{:keys [state customization-color]}]
  (cond (or (= state :pressed) (= state :selected))
        (colors/resolve-color customization-color :light 5)
        (= state :active)
        (colors/resolve-color customization-color :light 10)
        :else :transparent))

(defn container
  [props]
  {:height             56
   :border-radius      12
   :background-color   (background-color props)
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 12
   :padding-vertical   6
   :justify-content    :space-between})

(def left-container
  {:flex-direction :row
   :align-items    :center})

(defn dot-divider
  [theme]
  {:width             2
   :height            2
   :border-radius     2
   :margin-horizontal 4
   :background-color  (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def saved-contact-container
  {:margin-left 8})

(def account-container
  {:flex-direction :row
   :align-items    :center})

(def account-title-container
  {:flex-direction :row
   :height         22
   :align-items    :center})

(defn account-name
  [theme]
  {:margin-left 4
   :color       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn accounts-count
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(defn account-address
  [theme]
  {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)})

(def contact-icon-container
  {:margin-left 4
   :margin-top  1.5})
