(ns quo2.screens.drawers.action-drawers
  (:require [quo.previews.preview :as preview]
            [quo.react-native :as rn]
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

(def options-with-consequences [{:icon    :main-icons2/delete
                                 :danger? true
                                 :label   "Clear history"}])

(defn render-action-sheet [state]
  [quo2/action-drawer (cond-> [[{:icon  :main-icons2/friend
                                 :label "View channel members and details"}
                                {:icon  :main-icons2/communities
                                 :label "Mark as read"}
                                {:icon       :main-icons2/muted
                                 :label      (if (:muted? @state) "Unmute channel" "Mute channel")
                                 :right-icon :main-icons2/chevron-right
                                 :sub-label  (when (:muted? @state) "Muted for 15 min")}
                                {:icon       :main-icons2/scan
                                 :right-icon :main-icons2/chevron-right
                                 :label      "Fetch messages"}
                                {:icon  :main-icons2/add-user
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
