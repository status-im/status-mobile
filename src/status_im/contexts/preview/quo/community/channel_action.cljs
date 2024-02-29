(ns status-im.contexts.preview.quo.community.channel-action
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :label
    :type :text}
   {:key  :big?
    :type :boolean}
   {:key  :counter-value
    :type :number}
   {:key     :icon
    :type    :select
    :options [{:key :i/muted}
              {:key :i/pin}]}
   (preview/customization-color-option {:key :color})
   {:key  :disabled?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:label         "Action"
                             :icon          :i/muted
                             :color         :blue
                             :counter-value nil
                             :disabled?     false
                             :big?          true})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/channel-action @state]])))
