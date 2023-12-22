(ns status-im.contexts.preview-screens.quo-preview.selectors.react-selector
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :clicks
    :type :text}
   {:key     :emoji
    :type    :select
    :options (for [reaction (vals constants/reactions)]
               {:key   reaction
                :value (string/capitalize (name reaction))})}
   {:key     :state
    :type    :select
    :options [{:key   :not-pressed
               :value "Not pressed by me"}
              {:key   :pressed
               :value "Pressed by me"}
              {:key   :add-reaction
               :value "Add reaction"}]}
   {:key     :use-case
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :pinned
               :value "Pinned"}]}])

(defn preview-react-selector
  []
  (let [state (reagent/atom {:emoji    :reaction/love
                             :state    :not-pressed
                             :use-case :default})]
    (fn []
      (println @state)
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:padding-bottom 150
         :align-items    :center}
        [rn/view
         {:width            100
          :padding-vertical 60
          :border-radius    16
          :background-color (when (= :pinned (:use-case @state))
                              (colors/custom-color
                               :blue
                               50
                               10))
          :justify-content  :space-evenly
          :flex-direction   :row
          :align-items      :center}
         [quo/react-selector @state]]]])))
