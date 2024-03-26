(ns status-im.contexts.preview.quo.community.community-detail-token-gating
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :role
    :type    :select
    :options [{:key :one-role}
              {:key :two-roles}]}])

(defn view
  []
  (let [state (reagent/atom {:role :one-role})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/community-detail-token-gating @state]])))
