(ns status-im.contexts.preview.quo.community.channel-actions
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(defn view
  []
  [preview/preview-container
   [quo/channel-actions
    {:actions [{:big?                true
                :label               "Pinned Messages"
                :customization-color :blue
                :icon                :i/pin
                :counter-value       0}]}]
   [rn/view {:height 50}]
   [quo/channel-actions
    {:actions [{:label               "Pinned Messages"
                :customization-color :blue
                :icon                :i/pin
                :counter-value       5}
               {:label               "Mute chat"
                :customization-color :blue
                :icon                :i/muted}]}]
   [rn/view {:height 50}]
   [quo/channel-actions
    {:actions
     [{:big?                true
       :label               "Pinned Messages"
       :customization-color :blue
       :icon                :i/pin
       :counter-value       5}
      {:label               "Mute chat"
       :customization-color :blue
       :icon                :i/muted}]}]
   [rn/view {:height 50}]
   [quo/channel-actions
    {:actions [{:label               "Pinned Messages"
                :customization-color :blue
                :icon                :i/pin
                :counter-value       5}
               {:label               "Mute chat"
                :customization-color :blue
                :icon                :i/muted}
               {:label               "Something else"
                :customization-color :blue
                :icon                :i/placeholder}]}]])
