(ns status-im.ui.screens.chat.components.edit
  (:require [quo.core :as quo]
            [quo.react :as quo.react]
            [quo.react-native :as rn]
            [quo.design-system.colors :as quo.colors]
            [status-im.i18n.i18n :as i18n]
            [quo.components.animated.pressable :as pressable]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.chat.components.style :as styles]
            [re-frame.core :as re-frame]
            [quo2.foundations.colors :as quo2.colors :refer [theme-colors]]
            [quo2.components.button :as quo2.button]
            [quo2.components.text :as quo2.text]))

(defn input-focus [text-input-ref]
  (some-> ^js (quo.react/current-ref text-input-ref) .focus))

(defn edit-message-old []
  [rn/view {:style {:flex-direction :row}}
   [rn/view {}
    [icons/icon :tiny-icons/tiny-edit {:container-style {:margin-top 5}}]]
   [rn/view {:style (styles/reply-content-old)}
    [quo/text {:weight          :medium
               :number-of-lines 1}
     (i18n/label :t/editing-message)]]
   [rn/view
    [pressable/pressable {:on-press            #(re-frame/dispatch [:chat.ui/cancel-message-edit])
                          :accessibility-label :cancel-message-reply}
     [icons/icon :main-icons/close-circle {:container-style (styles/close-button)
                                           :color (:icon-02 @quo.colors/theme)}]]]])

(defn edit-message []
  [rn/view {:style {:flex-direction :row :height 24}}
   [rn/view {:style (styles/reply-content)}
    [icons/icon :main-icons/edit-connector {:color (theme-colors quo2.colors/neutral-40 quo2.colors/neutral-60)
                                            :container-style {:position :absolute :left 10 :bottom -4 :width 16 :height 16}}]
    [rn/view {:style {:position :absolute :left 36 :right 54 :top 3 :flex-direction :row :align-items :center}}
     [quo2.text/text {:weight          :medium
                      :size            :paragraph-2}
      (i18n/label :t/editing-message)]]]
   [quo2.button/button {:width               24
                        :size                24
                        :type                :outline
                        :accessibility-label :reply-cancel-button
                        :on-press            #(re-frame/dispatch [:chat.ui/cancel-message-edit])}
    [icons/icon :main-icons/close {:width 16
                                   :height 16
                                   :color (theme-colors quo2.colors/black quo2.colors/neutral-40)}]]])

(defn focus-input-on-edit [edit had-edit text-input-ref]
  ;;when we show edit we focus input
  (when-not (= edit @had-edit)
    (reset! had-edit edit)
    (when edit
      (js/setTimeout #(input-focus text-input-ref) 250))))

(defn edit-message-wrapper-old [edit]
  [rn/view {:style {:padding-horizontal 15
                    :border-top-width 1
                    :border-top-color (:ui-01 @quo.colors/theme)
                    :padding-vertical 8}}
   [edit-message-old edit]])

(defn edit-message-wrapper [edit]
  [rn/view {:style {:padding-horizontal 15
                    :border-top-width 1
                    :border-top-color (:ui-01 @quo.colors/theme)
                    :padding-vertical 8}}
   [edit-message edit]])

(defn edit-message-auto-focus-wrapper-old [text-input-ref]
  (let [had-edit (atom nil)]
    (fn []
      (let [edit @(re-frame/subscribe [:chats/edit-message])]
        (focus-input-on-edit edit had-edit text-input-ref)
        (when edit
          [edit-message-wrapper-old])))))

(defn edit-message-auto-focus-wrapper [text-input-ref]
  (let [had-edit (atom nil)]
    (fn []
      (let [edit @(re-frame/subscribe [:chats/edit-message])]
        (focus-input-on-edit edit had-edit text-input-ref)
        (when edit
          [edit-message-wrapper])))))
