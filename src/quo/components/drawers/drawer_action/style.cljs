(ns quo.components.drawers.drawer-action.style
  (:require
    [quo.foundations.colors :as colors]))

(defn- background-color
  [{:keys [state action customization-color theme pressed? blur?]}]
  (let [checked? (and (= :selected state) (nil? action))]
    (cond
      (and (or checked? pressed?) blur?)
      colors/white-opa-5

      (or pressed? checked?)
      (colors/resolve-color customization-color theme 5)

      :else :transparent)))

(defn container
  [{:keys [description?] :as props}]
  {:flex-direction     :row
   :align-items        :center
   :padding-vertical   (if description? 8 13)
   :padding-horizontal 13
   :border-radius      12
   :background-color   (background-color props)})

(defn text-container
  []
  {:flex         1
   :margin-right 12})

(defn- neutral-color
  [theme blur?]
  (if blur?
    colors/white-70-blur
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn left-icon
  []
  {:align-self   :flex-start
   :margin-right 13
   :margin-top   1})

(defn icon-color
  [{:keys [theme blur?]}]
  (neutral-color theme blur?))

(defn desc
  [{:keys [theme blur?]}]
  {:color (neutral-color theme blur?)})

(defn check-color
  [{:keys [theme blur? customization-color]}]
  (if blur?
    colors/white-70-blur
    (colors/resolve-color customization-color theme)))
