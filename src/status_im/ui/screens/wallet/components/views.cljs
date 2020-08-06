(ns status-im.ui.screens.wallet.components.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.wallet.components.styles :as styles]
            [quo.core :as quo]
            [status-im.ui.components.colors :as colors]
            [status-im.utils.debounce :as debounce])
  (:require-macros [status-im.utils.views :as views]))

(defn separator []
  [react/view (styles/separator)])

(defn- recipient-topbar [content]
  [topbar/topbar {:navigation        {:label    (i18n/label :t/cancel)
                                      :on-press #(do
                                                   (re-frame/dispatch [:set-in [:wallet/prepare-transaction :modal-opened?] false])
                                                   (re-frame/dispatch [:navigate-back]))}
                  :title             (i18n/label :t/recipient)
                  :right-accessories [{:on-press #(debounce/dispatch-and-chill [:wallet.send/set-recipient content] 3000)
                                       :label    (i18n/label :t/done)
                                       :disabled (string/blank? content)}]}])

(views/defview contact-code []
  (views/letsubs [content (reagent/atom nil)]
    [react/view {:flex 1}
     [recipient-topbar @content]
     [react/view {:padding-horizontal 16
                  :padding-vertical   24
                  :flex               1}
      [quo/text-input
       {:multiline           true
        :height              98
        :placeholder         (i18n/label :t/recipient-code-placeholder)
        :text-align-vertical :top
        :on-change-text      #(reset! content %)
        :accessibility-label :recipient-address-input}]
      [react/text {:style {:color           colors/gray
                           :margin-vertical 16}}
       (i18n/label :t/enter-recipient-address-or-username)]]]))
