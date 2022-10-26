(ns status-im.ui2.screens.chat.components.reaction-drawer
  (:require [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [quo2.components.buttons.button :as quo2.button]
            [quo2.foundations.colors :as colors]
            [quo2.components.list-items.menu-item :as quo2.menu-item]
            [quo2.components.separator :as quo2.separator]))

(defn message-options [actions own-reactions send-emoji]
  (fn []
    (let [main-actions (filter #(= (:type %) :main) actions)
          danger-actions (filter #(= (:type %) :danger) actions)
          admin-actions (filter #(= (:type %) :admin) actions)]
      [rn/view
       [rn/view {:style {:width "100%"
                         :flex-direction :row
                         :justify-content :space-between
                         :padding-horizontal 30
                         :padding-top 5
                         :padding-bottom 15}}
        (doall
         (for [[id icon] constants/reactions
               :let      [active (own-reactions id)]]
           ;;TODO reactions selector should be used https://www.figma.com/file/WQZcp6S0EnzxdTL4taoKDv/Design-System?node-id=9961%3A166549
           ;; not implemented yet
           ^{:key id}
           [quo2.button/button (merge
                                {:size                40
                                 :type                :grey
                                 :icon                true
                                 :icon-no-color       true
                                 :accessibility-label (str "emoji-picker-" id)
                                 :on-press            #(do
                                                         (send-emoji id)
                                                         (re-frame/dispatch [:bottom-sheet/hide]))}
                                (when active {:style {:background-color colors/neutral-10}}))
            icon]))]
       [rn/view {:style {:padding-horizontal 8}}
        (for [action main-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo2.menu-item/menu-item
             {:type                :main
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            #(do
                                      (when on-press (on-press))
                                      (re-frame/dispatch [:bottom-sheet/hide]))}]))
        (when-not (empty? danger-actions)
          [quo2.separator/separator])
        (for [action danger-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo2.menu-item/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            #(do
                                      (when on-press (on-press))
                                      (re-frame/dispatch [:bottom-sheet/hide]))}]))
        (when-not (empty? admin-actions)
          [quo2.separator/separator])
        (for [action admin-actions]
          (let [on-press (:on-press action)]
            ^{:key (:id action)}
            [quo2.menu-item/menu-item
             {:type                :danger
              :title               (:label action)
              :accessibility-label (:label action)
              :icon                (:icon action)
              :on-press            #(do
                                      (when on-press (on-press))
                                      (re-frame/dispatch [:bottom-sheet/hide]))}]))]])))
