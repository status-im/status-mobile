(ns status-im2.contexts.quo-preview.dividers.new-messages
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :label
    :type :text}
   {:key     :color
    :type    :select
    :options [{:key :primary}
              {:key :purple}
              {:key :indigo}
              {:key :turquoise}
              {:key :blue}
              {:key :green}
              {:key :yellow}
              {:key :orange}
              {:key :red}
              {:key :pink}
              {:key :brown}
              {:key :beige}]}])

(defn view
  []
  (let [state (reagent/atom {:label "New messages"
                             :color :primary})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/new-messages @state]])))
