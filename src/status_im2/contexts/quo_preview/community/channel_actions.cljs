(ns status-im2.contexts.quo-preview.community.channel-actions
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [status-im2.contexts.quo-preview.preview :as preview]))

(defn view
  []
  [preview/preview-container
   [quo/channel-actions
    {:actions [{:big?          true
                :label         "Pinned Messages"
                :color         :blue
                :icon          :i/pin
                :counter-value 0}]}]
   [rn/view {:height 50}]
   [quo/channel-actions
    {:actions [{:label "Pinned Messages" :color :blue :icon :i/pin :counter-value 5}
               {:label "Mute chat" :color :blue :icon :i/muted}]}]
   [rn/view {:height 50}]
   [quo/channel-actions
    {:actions [{:big? true :label "Pinned Messages" :color :blue :icon :i/pin :counter-value 5}
               {:label "Mute chat" :color :blue :icon :i/muted}]}]
   [rn/view {:height 50}]
   [quo/channel-actions
    {:actions [{:label "Pinned Messages" :color :blue :icon :i/pin :counter-value 5}
               {:label "Mute chat" :color :blue :icon :i/muted}
               {:label "Something else" :color :blue :icon :i/placeholder}]}]])
