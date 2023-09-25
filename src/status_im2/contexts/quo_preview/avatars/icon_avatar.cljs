(ns status-im2.contexts.quo-preview.avatars.icon-avatar
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :size-20}
              {:key :size-24}
              {:key :size-32}
              {:key :size-48}]}
   {:key     :icon
    :type    :select
    :options [{:key   :i/placeholder20
               :value "Placeholder"}
              {:key :i/wallet}
              {:key :i/play}]}
   (preview/customization-color-option {:key :color})])

(defn view
  []
  (let [state (reagent/atom {:size  :size-48
                             :icon  :i/placeholder20
                             :color :primary})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/icon-avatar @state]])))
