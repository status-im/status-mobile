(ns status-im.ui.screens.wallet.send.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.screens.wallet.send.styles :as styles]
            [status-im.ui.components.bottom-panel.views :as bottom-panel]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.wallet.utils :as wallet.utils]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.screens.wallet.send.sheets :as sheets]
            [status-im.ui.screens.wallet.components.views :as components]
            [status-im.utils.utils :as utils]
            [status-im.ui.components.button :as button]))

(defn header [small-screen?]
  [react/view (styles/header small-screen?)
   [react/view {:flex 1}
    [react/text {:style (merge {:typography :title-bold} (when small-screen? {:font-size 15}))}
     (i18n/label :t/send-transaction)]]
   [button/button {:type            :secondary
                   :container-style {:padding-horizontal 24}
                   :label           (i18n/label :t/cancel)
                   :on-press        #(re-frame/dispatch [:set :wallet/prepare-transaction nil])}]])

(defn asset-selector [{:keys [token from]}]
  (let [{:keys [name icon color]} token]
    [react/touchable-highlight
     {:on-press #(do
                   (re-frame/dispatch [:dismiss-keyboard])
                   (re-frame/dispatch [:bottom-sheet/show-sheet
                                       {:content        (fn [] [sheets/assets (:address from)])
                                        :content-height 300}]))}
     [react/view {:style               {:flex-direction :row
                                        :align-items    :center
                                        :margin-left    16}
                  :accessibility-label :choose-asset-button}
      (if icon
        [list/item-image (assoc icon :style {:background-color colors/gray-lighter
                                             :border-radius    50} :image-style {:width 32 :height 32})]
        [chat-icon/custom-icon-view-list name color 32])
      [react/text {:style {:margin-left 8}}
       (wallet.utils/display-symbol token)]
      [icons/icon :main-icons/dropdown {:color colors/gray}]]]))

(defn render-account [account {:keys [amount decimals] :as token}]
  [list-item/list-item
   {:icon        [chat-icon/custom-icon-view-list (:name account) (:color account)]
    :title       (:name account)
    :subtitle    (str (wallet.utils/format-amount amount decimals)
                      " "
                      (wallet.utils/display-symbol token))
    :accessories [:chevron]
    :on-press    #(do
                    (re-frame/dispatch [:dismiss-keyboard])
                    (re-frame/dispatch [:bottom-sheet/show-sheet
                                        {:content        (fn [] [sheets/accounts-list :from])
                                         :content-height 300}]))}])

(defn render-contact [contact from-chat?]
  (if from-chat?
    [list-item/list-item {:title (multiaccounts/displayed-name contact)
                          :icon  (multiaccounts/displayed-photo contact)}]
    [list-item/list-item
     {:title       (utils/get-shortened-checksum-address
                    (if (string? contact) contact (:address contact)))
      :subtitle    (when-not contact (i18n/label :t/wallet-choose-recipient))
      :accessibility-label :choose-recipient-button
      :on-press    #(do
                      (re-frame/dispatch [:dismiss-keyboard])
                      (re-frame/dispatch [:bottom-sheet/show-sheet
                                          {:content        sheets/choose-recipient
                                           :content-height 200}]))
      :accessories [:chevron]}]))

(views/defview sheet [_]
  (views/letsubs [{:keys [amount-error amount-text from token to sign-enabled? from-chat?] :as tx}
                  [:wallet.send/prepare-transaction-with-balance]
                  window-height [:dimensions/window-height]
                  keyboard-height [:keyboard-height]]
    (let [small-screen? (< (- window-height keyboard-height) 450)]
      [react/view {:style (styles/sheet small-screen?)}
       [header small-screen?]
       [react/view {:flex-direction :row :padding-horizontal 24 :align-items :center
                    :margin-vertical (if small-screen? 8 16)}
        [react/text-input
         {:style               {:font-size (if small-screen? 24 38)
                                :color (when amount-error colors/red)
                                :flex-shrink 1}
          :keyboard-type       :numeric
          :accessibility-label :amount-input
          :default-value       amount-text
          :auto-focus          true
          :on-change-text      #(re-frame/dispatch [:wallet.send/set-amount-text %])
          :placeholder         "0.0 "}]
        [asset-selector tx]
        (when amount-error
          [tooltip/tooltip amount-error {:bottom-value 2
                                         :font-size    12}])]
       [components/separator]
       (when-not small-screen?
         [list-item/list-item {:type :section-header :title :t/from}])
       [react/view {:flex-direction :row :flex 1 :align-items :center}
        (when small-screen?
          [react/i18n-text {:style {:width 50 :text-align :right :color colors/gray} :key :t/from}])
        [react/view {:flex 1}
         [render-account from token]]]
       (when-not small-screen?
         [list-item/list-item {:type :section-header :title :t/to}])
       [react/view {:flex-direction :row :flex 1 :align-items :center}
        (when small-screen?
          [react/i18n-text {:style {:width 50 :text-align :right :color colors/gray} :key :t/to}])
        [react/view {:flex 1}
         [render-contact to from-chat?]]]
       [toolbar/toolbar
        {:center {:label               :t/wallet-send
                  :accessibility-label :send-transaction-bottom-sheet
                  :disabled?           (not sign-enabled?)
                  :on-press            #(re-frame/dispatch [:wallet.ui/sign-transaction-button-clicked tx])}}]])))

(defview prepare-transaction []
  (letsubs [tx [:wallet/prepare-transaction]]
    [bottom-panel/animated-bottom-panel
     ;;we use select-keys here because we don't want to update view if other keys in map are changed
     ;; and because modal screen (qr code scanner) can't be opened over bottom sheet we have to use :modal-opened?
     ;; to hide our transaction panel
     (when (and tx (not (:modal-opened? tx)))
       (select-keys tx [:from-chat?]))
     sheet]))