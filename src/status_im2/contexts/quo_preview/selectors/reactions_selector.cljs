(ns status-im2.contexts.quo-preview.selectors.reactions-selector
  (:require [quo2.core :as quo]
            [clojure.string :as string]
            [status-im2.contexts.quo-preview.preview :as preview]
            [reagent.core :as r]
            [react-native.core :as rn]
            [status-im2.constants :as constants]))

(def descriptor
  [{:key     :emoji
    :type    :select
    :options (for [reaction (vals constants/reactions)]
               {:key   reaction
                :value (string/capitalize (name reaction))})}])

(defn preview
  []
  (let [state (r/atom {:emoji :reaction/love})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:margin-top  40
                 :align-items :center}}
        [quo/reactions-selector {:emoji (:emoji @state)}]]])))

