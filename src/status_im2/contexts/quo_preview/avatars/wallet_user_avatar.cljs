(ns status-im2.contexts.quo-preview.avatars.wallet-user-avatar
  (:require
    [quo2.core :as quo]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "First name"
    :key   :f-name
    :type  :text}
   {:label "Last name"
    :key   :l-name
    :type  :text}
   {:key     :size
    :type    :select
    :options [{:key :small}
              {:key :medium}
              {:key :large}
              {:key :size-64}
              {:key   :x-large
               :value "X Large"}]}
   (preview/customization-color-option {:key :customization-color})])

(defn view
  []
  (let [state (reagent/atom {:first-name          "empty"
                             :last-name           "name"
                             :size                :x-large
                             :customization-color :indigo})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/wallet-user-avatar @state]])))
