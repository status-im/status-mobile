(ns status-im2.contexts.quo-preview.drawers.action-drawers
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.re-frame :as rf]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Muted?"
    :key   :muted?
    :type  :boolean}
   {:label "Mark as read disabled?"
    :key   :mark-as-read-disabled?
    :type  :boolean}
   {:label "Show red options?"
    :key   :show-red-options?
    :type  :boolean}
   {:label   "Override theme"
    :key     :override-theme
    :type    :select
    :options [{:key :dark :value "Dark"}
              {:key :light :value "Light"}
              {:key nil :value "System"}]}])

(defn options-with-consequences
  [override-theme]
  [{:icon           :i/delete
    :danger?        true
    :label          "Clear history"
    :override-theme override-theme
    :add-divider?   true
    :on-press       #(js/alert "clear history")}])

(defn render-action-sheet
  [state]
  (let [override-theme (:override-theme @state)]
    [rn/view
     {:height           300
      :background-color (colors/theme-colors colors/white colors/neutral-95)}
     [quo/action-drawer
      (cond->
        [[{:icon           :i/friend
           :label          "View channel members and details"
           :override-theme override-theme
           :on-press       #(js/alert "View channel members and details")}
          {:icon           :i/communities
           :label          "Mark as read"
           :override-theme override-theme
           :disabled?      (:mark-as-read-disabled? @state)
           :on-press       #(js/alert "Mark as read")}
          {:icon           :i/muted
           :label          (if (:muted? @state) "Unmute channel" "Mute channel")
           :override-theme override-theme
           :on-press       #(js/alert (if (:muted? @state) "Unmute channel" "Mute channel"))
           :right-icon     :i/chevron-right
           :sub-label      (when (:muted? @state) "Muted for 15 min")}
          {:icon           :i/scan
           :on-press       #(js/alert "Fetch messages")
           :override-theme override-theme
           :right-icon     :i/chevron-right
           :right-text     "3"
           :label          "Fetch messages"}
          {:icon           :i/add-user
           :override-theme override-theme
           :on-press       #(js/alert "Share link to the channel")
           :label          "Share link to the channel"}]]

        (:show-red-options? @state)
        (conj (options-with-consequences override-theme)))]]))

(defn cool-preview
  []
  (let [state (reagent/atom {:muted?            true
                             :show-red-options? true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 400}
        [preview/customizer state descriptor]
        [quo/button
         {:style    {:margin-horizontal 40}
          :on-press #(rf/dispatch [:show-bottom-sheet
                                   {:content (fn [] [render-action-sheet state])}])}
         "See in bottom sheet"]
        [rn/view {:padding-vertical 60}
         [render-action-sheet state]]]])))

(defn preview-action-drawers
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :nestedScrollEnabled          true
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       (fn [_ index] (str "actions-drawers-" index))}]])
