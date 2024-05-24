(ns status-im.contexts.preview.quo.avatars.community-avatar
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:type    :select
    :key     :size
    :options [{:key :size-32}
              {:key :size-24}]}])

(defn view
  []
  (let [[state set-state] (rn/use-state {:size :size-32})]
    [preview/preview-container
     {:state      state
      :set-state  set-state
      :descriptor descriptor}
     [quo/community-avatar
      (assoc state
             :image
             (resources/get-mock-image :community-logo))]]))
