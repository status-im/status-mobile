(ns quo.components.gradient.gradient-cover.view
  (:require
    [quo.components.gradient.gradient-cover.style :as style]
    [quo.foundations.colors :as colors]
    [react-native.linear-gradient :as linear-gradient]))

(defn view
  [{:keys [customization-color opacity container-style height]
    :or   {customization-color :blue}}]
  ;; `when` added for safety, `linear-gradient` will break if `nil` is passed, the `:or`
  ;; destructuring won't work because it's only applied when the `:customization-color` key is
  ;; non-existent. While deleting an account the key exists and has a `nil` value.
  (let [color-top    (colors/resolve-color customization-color 50 20)
        color-bottom (colors/resolve-color customization-color 50 0)]
    (when (and color-top color-bottom)
      [linear-gradient/linear-gradient
       {:accessibility-label :gradient-cover
        :colors              [color-top color-bottom]
        :start               {:x 0 :y 0}
        :end                 {:x 0 :y 1}
        :style               (merge (style/root-container opacity height)
                                    container-style)}])))
