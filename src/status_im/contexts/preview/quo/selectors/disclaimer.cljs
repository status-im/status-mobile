(ns status-im.contexts.preview.quo.selectors.disclaimer
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :checked? :type :boolean}
   {:key :blur? :type :boolean}
   {:key :text :type :text}
   {:key :icon :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:checked? false
                             :blur?    true
                             :text     "I agree with the community rules"
                             :icon     false})]
    (fn []
      (let [{:keys [blur? checked? text icon]} @state]
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
            :icon      (when icon
                         (if checked?
                           :i/locked
                           :i/unlocked))
            :on-change #(swap! state update :checked? not)}
           text]]
         [quo/button {:disabled? (not checked?)}
          "submit"]]))))
