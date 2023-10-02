(ns status-im2.contexts.quo-preview.reactions.react
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Count"
    :key   :clicks
    :type  :text}
   {:key     :emoji
    :type    :select
    :options (for [reaction (vals constants/reactions)]
               {:key   reaction
                :value (string/capitalize (name reaction))})}
   {:key :neutral? :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:emoji :reaction/love})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-bottom  50
                                    :flex-direction  :row
                                    :align-items     :center
                                    :justify-content :center}}
       [quo/reaction @state]
       [quo/add-reaction]])))
