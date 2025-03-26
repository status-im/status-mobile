(ns status-im.contexts.preview.quo.selectors.disclaimer
  (:require
    [quo.context :as quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :checked? :type :boolean}
   {:key :text :type :text}
   {:key :icon :type :boolean}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:checked?            false
                             :blur?               false
                             :text                "I agree with the community rules"
                             :icon                false
                             :customization-color :blue})]
    (fn []
      (let [{:keys [blur? checked? text icon customization-color]} @state
            theme                                                  (quo.context/use-theme)
            blur?                                                  (if (= :light theme) false blur?)]
        [preview/preview-container
         {:state                     state
          :descriptor                (if (= :light theme)
                                       descriptor
                                       (vec (conj descriptor {:key :blur? :type :boolean})))
          :blur?                     blur?
          :show-blur-background?     true
          :component-container-style {:padding 20}}
         [rn/view {:style {:margin-bottom 20}}
          [quo/disclaimer
           {:blur?               blur?
            :checked?            checked?
            :icon                (when icon
                                   (if checked?
                                     :i/locked
                                     :i/unlocked))
            :on-change           #(swap! state update :checked? not)
            :customization-color customization-color}
           text]]
         [quo/button
          {:disabled?           (not checked?)
           :customization-color customization-color}
          "submit"]]))))
