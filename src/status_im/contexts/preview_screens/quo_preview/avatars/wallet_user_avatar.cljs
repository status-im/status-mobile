(ns status-im.contexts.preview-screens.quo-preview.avatars.wallet-user-avatar
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :full-name
    :type :text}
   {:key     :size
    :type    :select
    :options [{:key :size-20}
              {:key :size-24}
              {:key :size-32}
              {:key :size-48}
              {:key :size-64}
              {:key :size-80}]}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:full-name           "empty name"
                             :size                :size-80
                             :customization-color :blue})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/wallet-user-avatar @state]])))
