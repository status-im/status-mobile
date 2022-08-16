(ns status-im.ui.screens.chat.components.edit
  (:require [quo.core :as quo]
            [quo.react :as quo.react]
            [quo.react-native :as rn]
            [quo.design-system.colors :as quo.colors]
            [status-im.i18n.i18n :as i18n]
            [quo.components.animated.pressable :as pressable]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.chat.components.style :as styles]
            [re-frame.core :as re-frame]))

(defn input-focus [text-input-ref]
  (some-> ^js (quo.react/current-ref text-input-ref) .focus))

(defn edit-message []
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

(defn focus-input-on-edit [edit had-edit text-input-ref]
  ;;when we show edit we focus input
  (when-not (= edit @had-edit)
    (reset! had-edit edit)
    (when edit
      (js/setTimeout #(input-focus text-input-ref) 250))))

(defn edit-message-wrapper [edit]
  [rn/view {:style {:padding-horizontal 15
                    :border-top-width 1
                    :border-top-color (:ui-01 @quo.colors/theme)
                    :padding-vertical 8}}
   [edit-message edit]])

(defn edit-message-auto-focus-wrapper [text-input-ref]
  (let [had-edit (atom nil)]
    (fn []
      (let [edit @(re-frame/subscribe [:chats/edit-message])]
        (focus-input-on-edit edit had-edit text-input-ref)
        (when edit
          [edit-message-wrapper])))))
