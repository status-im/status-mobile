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

(defn text
  [{:keys [theme blur? type]}]
  (let [base {:weight :medium}
        theme-with-blur (if blur? :blue theme)
        matcher [theme-with-blur type]
        color
        (case matcher
          ([:dark :main] [:light :main])     (colors/theme-colors colors/neutral-100
                                                                  colors/white
                                                                  theme)
          [:blur :main]                      colors/white-70-blur
          ([:dark :danger] [:light :danger]) (colors/theme-colors colors/danger-50
                                                                  colors/danger-60
                                                                  theme)
          [:blur :danger]                    colors/danger-60)]
    (assoc-in base [:style :color] color)))

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
  [{:keys [theme blur? type]}]
  (let [theme-with-blur (if blur? :blue theme)
        matcher         [theme-with-blur type]]
    (case matcher
      ([:dark :main] [:light :main])     (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
      [:blur :main]                      colors/white-70-blur
      ([:dark :danger] [:light :danger]) (colors/theme-colors colors/danger-50 colors/danger-60 theme)
      [:blur :danger]                    colors/danger-60)))

(defn desc
  [{:keys [theme blur?]}]
  {:color (neutral-color theme blur?)})

(defn check-color
  [{:keys [theme blur? customization-color]}]
  (if blur?
    colors/white-70-blur
    (colors/resolve-color customization-color theme)))
