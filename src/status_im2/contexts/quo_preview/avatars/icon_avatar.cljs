(ns status-im2.contexts.quo-preview.avatars.icon-avatar
  (:require [quo2.components.avatars.icon-avatar :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :small}
              {:key :medium}
              {:key :big}]}
   {:key     :icon
    :type    :select
    :options [{:key   :i/placeholder20
               :value "Placeholder"}
              {:key :i/wallet}
              {:key :i/play}]}
   (preview/customization-color-option {:key :color})])

(defn view
  []
  (let [state (reagent/atom {:size  :big
                             :icon  :i/placeholder20
                             :color :primary})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/icon-avatar @state]])))
