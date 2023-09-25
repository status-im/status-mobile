(ns status-im2.contexts.quo-preview.reactions.react
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Count"
    :key   :clicks
    :type  :text}
   {:label   "Emoji"
    :key     :emoji
    :type    :select
    :options (for [reaction (vals constants/reactions)]
               {:key   reaction
                :value (string/capitalize (name reaction))})}
   {:label "Neutral"
    :key   :neutral?
    :type  :boolean}])

(defn preview-react
  []
  (let [state (reagent/atom {:emoji :reaction/love})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:padding-bottom 150
         :align-items    :center}
        [rn/view
         {:width            100
          :padding-vertical 60
          :justify-content  :space-evenly
          :flex-direction   :row
          :align-items      :center}
         [quo/reaction @state]
         [quo/add-reaction]]]])))
