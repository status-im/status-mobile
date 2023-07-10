(ns status-im2.contexts.quo-preview.community.channel-actions
  (:require [react-native.core :as rn]
            [quo2.components.community.channel-actions :as channel-actions]))

(defn preview-channel-actions
  []
  [rn/view {:flex 1}
   [rn/scroll-view {:style {:flex 1 :padding-horizontal 20}}
    [channel-actions/channel-actions
     {:actions [{:big?          true
                 :label         "Pinned Messages"
                 :color         :blue
                 :icon          :i/pin
                 :counter-value 0}]}]
    [rn/view {:height 50}]
    [channel-actions/channel-actions
     {:actions [{:label "Pinned Messages" :color :blue :icon :i/pin :counter-value 5}
                {:label "Mute chat" :color :blue :icon :i/muted}]}]
    [rn/view {:height 50}]
    [channel-actions/channel-actions
     {:actions [{:big? true :label "Pinned Messages" :color :blue :icon :i/pin :counter-value 5}
                {:label "Mute chat" :color :blue :icon :i/muted}]}]
    [rn/view {:height 50}]
    [channel-actions/channel-actions
     {:actions [{:label "Pinned Messages" :color :blue :icon :i/pin :counter-value 5}
                {:label "Mute chat" :color :blue :icon :i/muted}
                {:label "Something else" :color :blue :icon :i/placeholder}]}]]])
