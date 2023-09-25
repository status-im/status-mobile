(ns quo.previews.tooltip
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn])
  (:require-macros [quo.previews.preview :as preview]))

(def all-props
  (preview/list-comp
    [child [[quo/text {:size :small} "Simple text"]
            [quo/text
             {:color :negative
              :size  :small}
             "Error text"]
            [rn/view {:width 100 :height 20 :background-color :red}]
            [quo/text
             "Just text, but long. Officia autem est repellendus ad quia exercitationem veniam."]]]
    child))

(defn render-item
  [children]
  [rn/view {:margin-vertical 50}
   [rn/view
    {:height           20
     :background-color "rgba(0,0,0,0.1)"}]
   [quo/tooltip {}
    children]])

(defn preview-tooltip
  []
  [rn/view
   {:background-color (:ui-background @colors/theme)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :data                         all-props
     :render-fn                    render-item
     :key-fn                       str}]])
