(ns status-im.ui.screens.chat.message.pinned-message
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.chat.models.pin-message :as models.pin-message]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.radio :as radio]
            [status-im.utils.handlers :refer [<sub]]
            [status-im.ui.screens.chat.message.message :as message]))

(def selected-unpin (reagent/atom nil))

(defn render-pin-fn [{:keys [message-id outgoing] :as message}
                     _
                     _
                     {:keys [group-chat public? current-public-key space-keeper]}]
  [react/touchable-without-feedback {:style {:width "100%"}
                                     :on-press #(reset! selected-unpin message-id)}
   [react/view {:style {:flex-direction  :row
                        :align-items     :center
                        :justify-content :space-between
                        :flex            1
                        :padding-right   20}}
    [message/chat-message
     (assoc message
            :group-chat group-chat
            :public? public?
            :current-public-key current-public-key
            :show-input? false
            :pinned false
            :display-username? (not outgoing)
            :display-photo? false
            :last-in-group? false
            :in-popover? true)
     space-keeper]
    [react/view {:style {:position    :absolute
                         :right       18
                         :padding-top 4}}
     [radio/radio (= @selected-unpin message-id)]]]])

(def list-key-fn #(or (:message-id %) (:value %)))

(defn pinned-messages-limit-list [chat-id]
  (let [pinned-messages @(re-frame/subscribe [:chats/pinned-sorted-list chat-id])]
    [list/flat-list
     {:key-fn                       list-key-fn
      :data                         (reverse pinned-messages)
      :render-data                  {:chat-id chat-id}
      :render-fn                    render-pin-fn
      :on-scroll-to-index-failed    identity
      :style                        {:flex-grow 0
                                     :border-top-width 1
                                     :border-bottom-width 1
                                     :border-top-color colors/gray-lighter
                                     :border-bottom-color colors/gray-lighter}
      :content-container-style      {:padding-bottom 10
                                     :padding-top 10}}]))

(defn pin-limit-popover []
  (let [{:keys [message]} (<sub [:popover/popover])]
    [react/view
     [react/view {:style {:height 60
                          :justify-content :center}}
      [react/text {:style {:padding-horizontal 40
                           :text-align :center}}
       (i18n/label :t/pin-limit-reached)]]
     [pinned-messages-limit-list (message :chat-id)]
     [react/view {:flex-direction :row :padding-horizontal 16 :height 60 :justify-content :space-between :align-items :center}
      [quo/button
       {:on-press #(do
                     (reset! selected-unpin nil)
                     (re-frame/dispatch [:hide-popover]))
        :type :secondary}
       (i18n/label :t/cancel)]
      [quo/button
       {:on-press #(do
                     (re-frame/dispatch [::models.pin-message/send-pin-message {:chat-id    (message :chat-id)
                                                                                :message-id @selected-unpin
                                                                                :pinned    false}])
                     (re-frame/dispatch [::models.pin-message/send-pin-message (assoc message :pinned true)])
                     (re-frame/dispatch [:hide-popover])
                     (reset! selected-unpin nil))
        :type :secondary
        :disabled (nil? @selected-unpin)
        :theme (if (nil? @selected-unpin) :disabled :negative)}
       (i18n/label :t/unpin)]]]))