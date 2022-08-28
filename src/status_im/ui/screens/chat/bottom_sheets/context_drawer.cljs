(ns status-im.ui.screens.chat.bottom-sheets.context-drawer
  (:require [quo.react-native :as rn]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.constants :as constants]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.foundations.colors :as quo2.colors]
            [quo2.components.list-items.menu-item :as quo2.menu-item]))

(defn message-options [actions own-reactions send-emoji]
  (fn []
    (let [main-actions (filter #(= (:type %) :main) actions)
          danger-actions (filter #(= (:type %) :danger) actions)
          admin-actions (filter #(= (:type %) :admin) actions)]
      [react/view {:flex 1}
       [react/view {:style {:width "100%"
                            :flex-direction :row
                            :justify-content :space-between
                            :padding-horizontal 30
                            :padding-top 5
                            :padding-bottom 15}}
        (doall
         (for [[id resource] constants/reactions-old
               :let          [active (own-reactions id)]]
           ^{:key id}
           [quo2.button/button (merge
                                {:width               40
                                 :size                40
                                 :type                :grey
                                 :accessibility-label :reply-cancel-button
                                 :on-press            #(do
                                                         (send-emoji id)
                                                         (re-frame/dispatch [:bottom-sheet/hide]))}
                                (when active {:style {:background-color quo2.colors/neutral-70}}))
            [rn/image {:source resource
                       :style  {:height 20
                                :width  20}}]]))]
       [rn/view {:style {:padding-horizontal 8}}
        (for [action main-actions]
          ^{:key (:id action)}
          (let [on-press (:on-press action)]
            [quo2.menu-item/menu-item
             {:type                :main
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            #(do
                                      (when on-press (on-press))
                                      (re-frame/dispatch [:bottom-sheet/hide]))}]))
        (when-not (empty? danger-actions)
          [rn/view {:style {:padding-vertical 8}}
           [components/separator]])
        (for [action danger-actions]
          ^{:key (:id action)}
          (let [on-press (:on-press action)]
            [quo2.menu-item/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            #(do
                                      (when on-press (on-press))
                                      (re-frame/dispatch [:bottom-sheet/hide]))}]))
        (when-not (empty? admin-actions)
          [rn/view {:style {:padding-vertical 8}}
           [components/separator]])
        (for [action admin-actions]
          ^{:key (:id action)}
          (let [on-press (:on-press action)]
            [quo2.menu-item/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            #(do
                                      (when on-press (on-press))
                                      (re-frame/dispatch [:bottom-sheet/hide]))}]))]])))