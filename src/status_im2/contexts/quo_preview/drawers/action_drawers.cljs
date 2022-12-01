(ns status-im2.contexts.quo-preview.drawers.action-drawers
  (:require [status-im2.contexts.quo-preview.preview :as preview]
            [react-native.core :as rn]
            [quo2.components.drawers.action-drawers :as quo2]
            [quo2.components.buttons.button :as button]
            [quo2.foundations.colors :as colors]
            [status-im.utils.handlers :refer [>evt]]
            [reagent.core :as reagent]))

(def descriptor [{:label   "Muted?"
                  :key     :muted?
                  :type    :boolean}
                 {:label   "Show red options?"
                  :key     :show-red-options?
                  :type    :boolean}])

(def options-with-consequences [{:icon    :i/delete
                                 :danger? true
                                 :label   "Clear history"
                                 :add-divider? true
                                 :on-press #(js/alert "clear history")}])

(defn render-action-sheet [state]
  [rn/view {:height 300
            :background-color (colors/theme-colors colors/white colors/neutral-95)}
   [quo2/action-drawer (cond-> [[{:icon     :i/friend
                                  :label    "View channel members and details"
                                  :on-press #(js/alert "View channel members and details")}
                                 {:icon     :i/communities
                                  :label    "Mark as read"
                                  :on-press #(js/alert "Mark as read")}
                                 {:icon       :i/muted
                                  :label      (if (:muted? @state) "Unmute channel" "Mute channel")
                                  :on-press   #(js/alert (if (:muted? @state) "Unmute channel" "Mute channel"))
                                  :right-icon :i/chevron-right
                                  :sub-label  (when (:muted? @state) "Muted for 15 min")}
                                 {:icon       :i/scan
                                  :on-press   #(js/alert "Fetch messages")
                                  :right-icon :i/chevron-right
                                  :label      "Fetch messages"}
                                 {:icon     :i/add-user
                                  :on-press #(js/alert "Share link to the channel")
                                  :label    "Share link to the channel"}]]

                         (:show-red-options? @state)
                         (conj options-with-consequences))]])

(defn cool-preview []
  (let [state         (reagent/atom {:muted?         true
                                     :show-red-options? true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 400}
        [preview/customizer state descriptor]
        [button/button
         {:style {:margin-horizontal 40}
          :on-press  #(>evt [:bottom-sheet/show-sheet
                             {:content (constantly  (render-action-sheet state))
                              :content-height 300}])}
         "See in bottom sheet"]
        [rn/view {:padding-vertical 60}
         (render-action-sheet state)]]])))

(defn preview-action-drawers []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :nestedScrollEnabled       true
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn (fn [_ index] (str "actions-drawers-" index))}]])
