(ns status-im.ui2.screens.quo2-preview.drawers.action-drawers
  (:require [status-im.ui2.screens.quo2-preview.preview :as preview]
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
                                 :label   "Clear history"}])

(defn render-action-sheet [state]
  [quo2/action-drawer (cond-> [[{:icon  :i/friend
                                 :label "View channel members and details"}
                                {:icon  :i/communities
                                 :label "Mark as read"}
                                {:icon       :i/muted
                                 :label      (if (:muted? @state) "Unmute channel" "Mute channel")
                                 :right-icon :i/chevron-right
                                 :sub-label  (when (:muted? @state) "Muted for 15 min")}
                                {:icon       :i/scan
                                 :right-icon :i/chevron-right
                                 :label      "Fetch messages"}
                                {:icon  :i/add-user
                                 :label "Share link to the channel"}]]

                        (:show-red-options? @state)
                        (conj options-with-consequences))])

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
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}

   [rn/flat-list {:flex                      1
                  :nestedScrollEnabled       true
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn (fn [_ index] (str "actions-drawers-" index))}]])
