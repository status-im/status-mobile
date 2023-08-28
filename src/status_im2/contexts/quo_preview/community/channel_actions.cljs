(ns status-im2.contexts.quo-preview.community.channel-actions
  (:require [quo2.core :as quo]
            [react-native.core :as rn]))

(defn view
  []
  [rn/view {:flex 1}
   [rn/scroll-view {:style {:flex 1 :padding-horizontal 20}}
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
                {:label "Something else" :color :blue :icon :i/placeholder}]}]]])
