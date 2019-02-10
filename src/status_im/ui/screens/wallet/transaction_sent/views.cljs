(ns status-im.ui.screens.wallet.transaction-sent.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.icons.vector-icons :as vi]
            [status-im.react-native.resources :as resources]
            [status-im.ui.screens.wallet.transaction-sent.styles :as styles]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.colors :as colors]))

(defn- ok-circle []
  [react/view {:background-color colors/black-transparent
               :width            160
               :height           160
               :border-radius    81
               :align-items      :center
               :justify-content  :center}
   [react/view {:background-color colors/white
                :width            80
                :height           80
                :border-radius    41
                :shadow-radius    4
                :shadow-offset    {:width 0 :height 2}
                :shadow-opacity   0.8
                :shadow-color     "rgba(43, 59, 71, 0.12)"
                :align-items      :center
                :justify-content  :center}
    [vi/icon :tiny-icons/tiny-check {:color colors/blue}]]])

(defn- transaction-sent-message []
  [react/view {:align-items :center}
   [react/text {:style               styles/transaction-sent
                :font                (if platform/android? :medium :default)
                :accessibility-label :transaction-sent-text}
    (i18n/label :t/transaction-sent)]
   [react/i18n-text {:style styles/transaction-sent-description
                     :key   :transaction-description}]])

(defn- bottom-action-button [on-next]
  [react/touchable-highlight {:on-press on-next
                              :style {:border-top-width 1
                                      :border-color     colors/white-light-transparent}
                              :accessibility-label :got-it-button}
   [react/view {:align-items      :center
                :padding-vertical 18}
    [react/text {:style      {:color     colors/white
                              :font-size 15}
                 :font       (if platform/android? :medium :default)
                 :uppercase? true}
     (i18n/label :t/done)]]])

(defn- sent-screen [{:keys [on-next]}]
  {:pre [(fn? on-next)]}
  [react/view {:flex 1}
   [react/view {:flex 0.7}] ;; spacer
   [react/view {:align-items :center} (ok-circle)]
   [react/view {:flex 1}]   ;; spacer
   (transaction-sent-message)
   (bottom-action-button on-next)])

(defview transaction-sent []
  (letsubs [chat-id [:chats/current-chat-id]]
    [react/view {:flex 1 :background-color colors/blue}
     [status-bar/status-bar {:type :transparent}]
     (sent-screen {:on-next #(re-frame/dispatch [:close-transaction-sent-screen chat-id])})]))

(defview transaction-sent-modal []
  (letsubs [chat-id [:chats/current-chat-id]]
    [react/view {:flex 1 :background-color colors/blue}
     [status-bar/status-bar {:type :modal-wallet}]
     (sent-screen {:on-next #(re-frame/dispatch [:close-transaction-sent-screen chat-id])})]))
