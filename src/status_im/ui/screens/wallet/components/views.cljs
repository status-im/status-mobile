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
            [status-im.ui.components.colors :as colors])
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
     :handler   #(re-frame/dispatch [:wallet.send/set-recipient content])}
    (i18n/label :t/done)]])

(views/defview contact-code []
  (views/letsubs [content (reagent/atom nil)]
    [react/view {:flex 1}
     [recipient-topbar @content]
     [react/view
      [text-input/text-input-with-label
       {:multiline           true
        :container           {:margin 16 :padding-vertical 16 :height 72}
        :style               {:text-align-vertical :top :height 42}
        :placeholder         (i18n/label :t/recipient-code-placeholder)
        :on-change-text      #(reset! content %)
        :accessibility-label :recipient-address-input}]
      [react/text {:style {:color colors/gray :margin-horizontal 16}}
       (i18n/label :t/enter-recipient-address-or-username)]]]))
