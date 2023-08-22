(ns status-im2.contexts.quo-preview.drawers.action-drawers
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.re-frame :as rf]))

(def descriptor
  [{:key  :muted?
    :type :boolean}
   {:key  :mark-as-read-disabled?
    :type :boolean}
   {:key  :show-red-options?
    :type :boolean}
   {:key     :theme
    :type    :select
    :options [{:key :dark}
              {:key :light}
              {:key nil :value "System"}]}])

(def options-with-consequences
  [{:icon         :i/delete
    :danger?      true
    :label        "Clear history"
    :add-divider? true
    :on-press     #(js/alert "clear history")}])

(defn action-sheet
  [state]
  [quo/action-drawer
   (cond->
     [[{:icon     :i/friend
        :label    "View channel members and details"
        :on-press #(js/alert "View channel members and details")}
       {:icon      :i/communities
        :label     "Mark as read"
        :disabled? (:mark-as-read-disabled? @state)
        :on-press  #(js/alert "Mark as read")}
       {:icon       :i/muted
        :label      (if (:muted? @state) "Unmute channel" "Mute channel")
        :on-press   #(js/alert (if (:muted? @state) "Unmute channel" "Mute channel"))
        :right-icon :i/chevron-right
        :sub-label  (when (:muted? @state) "Muted for 15 min")}
       {:icon       :i/scan
        :on-press   #(js/alert "Fetch messages")
        :right-icon :i/chevron-right
        :right-text "3"
        :label      "Fetch messages"}
       {:icon     :i/add-user
        :on-press #(js/alert "Share link to the channel")
        :label    "Share link to the channel"}]]

     (:show-red-options? @state)
     (conj options-with-consequences))])

(defn view
  []
  (let [state (reagent/atom {:muted?            true
                             :show-red-options? true})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/button
        {:container-style {:margin-horizontal 40}
         :on-press        #(rf/dispatch [:show-bottom-sheet
                                         {:content (fn [] [action-sheet state])
                                          :theme   (:theme @state)}])}
        "See in bottom sheet"]
       [rn/view {:padding-vertical 60}
        [action-sheet state]]])))
