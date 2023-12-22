(ns status-im.contexts.preview-screens.quo-preview.selectors.disclaimer
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :checked? :type :boolean}
   {:key :blur? :type :boolean}
   {:key :text :type :text}])

(defn view
  []
  (let [state (reagent/atom {:checked? false
                             :blur?    true
                             :text     "I agree with the community rules"})]
    (fn []
      (let [{:keys [blur? checked? text]} @state]
        [preview/preview-container
         {:state                     state
          :descriptor                descriptor
          :blur?                     blur?
          :show-blur-background?     true
          :component-container-style {:padding 20}}
         [rn/view {:style {:margin-bottom 20}}
          [quo/disclaimer
           {:blur?     blur?
            :checked?  checked?
            :on-change #(swap! state update :checked? not)}
           text]]
         [quo/button {:disabled? (not checked?)}
          "submit"]]))))
