(ns status-im.contexts.preview-screens.quo-preview.selectors.reactions-selector
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as r]
    [status-im.constants :as constants]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

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

