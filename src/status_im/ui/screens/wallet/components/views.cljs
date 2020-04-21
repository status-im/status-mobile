(ns status-im.ui.screens.wallet.components.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as topbar]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [status-im.ui.components.text-input.view :as text-input]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.debounce :as debounce])
  (:require-macros [status-im.utils.views :as views]))

(defn separator []
  [react/view (styles/separator)])

(defn- recipient-topbar [content]
  [topbar/toolbar {:transparent? true}
   [topbar/nav-text
    {:style   {:margin-left 16}
     :handler #(do
                 (re-frame/dispatch [:set-in [:wallet/prepare-transaction :modal-opened?] false])
                 (re-frame/dispatch [:navigate-back]))}
    (i18n/label :t/cancel)]
   [topbar/content-title {}
    (i18n/label :t/recipient)]
   [topbar/text-action
    {:disabled? (string/blank? content)
     :style     {:margin-right 16}
     :handler   #(debounce/dispatch-and-chill [:wallet.send/set-recipient content] 3000)}
    (i18n/label :t/done)]])

(views/defview contact-code []
  (views/letsubs [content (reagent/atom nil)]
    [react/view {:flex 1}
     [recipient-topbar @content]
     [react/view {:padding-horizontal 16
                  :padding-vertical   24
                  :flex               1}
      [text-input/text-input-with-label
       {:multiline           true
        :container           {:height           98
                              :padding-vertical 8}
        :placeholder         (i18n/label :t/recipient-code-placeholder)
        :on-change-text      #(reset! content %)
        :accessibility-label :recipient-address-input}]
      [react/text {:style {:color           colors/gray
                           :margin-vertical 16}}
       (i18n/label :t/enter-recipient-address-or-username)]]]))
