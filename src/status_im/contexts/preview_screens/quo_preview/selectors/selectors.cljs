(ns status-im.contexts.preview-screens.quo-preview.selectors.selectors
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :disabled? :type :boolean}
   {:key :blur? :type :boolean}
   (preview/customization-color-option)])

(defn selector-preview
  [text type {:keys [disabled? blur? customization-color]}]
  [rn/view
   {:style {:margin      6
            :align-items :center}}
   [quo/text {:size :paragraph-1} text]
   [quo/selectors
    {:type                type
     :container-style     {:margin 4}
     :disabled?           disabled?
     :blur?               blur?
     :customization-color customization-color}]])

(defn view
  []
  (let [state (reagent/atom {:disabled?           false
                             :blur?               false
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-height           300}
       [selector-preview "Toggle" :toggle @state]
       [selector-preview "Radio" :radio @state]
       [selector-preview "Checkbox" :checkbox @state]
       [selector-preview "Filled Checkbox" :filled-checkbox @state]])))
