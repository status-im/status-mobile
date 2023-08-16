(ns quo2.components.counter.step.view
  (:require
    [quo2.components.counter.step.style :as style]
    [quo2.components.markdown.text :as text]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [utils.number]))

(defn- view-internal
  [{:keys [type accessibility-label theme in-blur-view? customization-color]} value]
  (let [type  (or type :neutral)
        value (utils.number/parse-int value)
        label (str value)
        size  (count label)]
    [rn/view
     {:accessible          true
      :accessibility-label (or accessibility-label :step-counter)
      :style               (style/container {:size                size
                                             :type                type
                                             :in-blur-view?       in-blur-view?
                                             :theme               theme
                                             :customization-color customization-color})}
     [text/text
      {:weight :medium
       :size   :label
       :style  {:color (style/text-color type theme)}} label]]))

(def view (theme/with-theme view-internal))
