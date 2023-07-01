(ns quo2.components.counter.step.view
  (:require
    [quo2.components.counter.step.style :as style]
    [quo2.components.markdown.text :as text]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [utils.number]))

(defn themed-step
  [{:keys [type accessibility-label theme in-blur-view?]} value]
  (let [type  (or type :neutral)
        value (utils.number/parse-int value)
        label (str value)
        size  (count label)]
    [rn/view
     {:accessible          true
      :accessibility-label (or accessibility-label :step-counter)
      :style               (style/container size type in-blur-view? theme)}
     [text/text
      {:weight :medium
       :size   :label
       :style  {:color (style/text-color type theme)}} label]]))

(def step (theme/with-theme themed-step))
