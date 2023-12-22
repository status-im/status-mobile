(ns status-im.contexts.preview-screens.quo-preview.colors.color-picker
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [default-selected :blue
        selected         (reagent/atom default-selected)
        on-change        #(reset! selected %)
        state            (reagent/atom {:customization-color :blue
                                        :blur?               false
                                        :feng-shui?          true})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [rn/view {:style {:padding-bottom 20}}
        [quo/text (str "Selected color: " (name @selected))]]
       [quo/color-picker
        (assoc @state
               :default-selected default-selected
               :on-change        on-change
               :color            (:customization-color @state))]])))
