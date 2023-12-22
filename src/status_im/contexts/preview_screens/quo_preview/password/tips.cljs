(ns status-im.contexts.preview-screens.quo-preview.password.tips
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :text :type :text}
   {:key :completed? :type :boolean}])

(defn view
  []
  (let [state      (reagent/atom {:text       "Lower case"
                                  :completed? false})
        text       (reagent/cursor state [:text])
        completed? (reagent/cursor state [:completed?])]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding          20
                                    :background-color colors/neutral-95}}
       [quo/tips {:completed? @completed?} @text]])))
