(ns status-im2.contexts.quo-preview.avatars.wallet-user-avatar
  (:require [quo2.components.avatars.wallet-user-avatar.view :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Full name"
    :key   :full-name
    :type  :text}
   {:key     :size
    :type    :select
    :options [{:key :small}
              {:key :medium}
              {:key :large}
              {:key   :x-large
               :value "X Large"}]}
   (preview/customization-color-option {:key :color})])

(defn view
  []
  (let [state (reagent/atom {:full-name "empty name"
                             :size      :x-large
                             :color     :blue})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/view @state]])))
