(ns status-im.transactions.views.list-item
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :as rf]
            [status-im.components.chat-icon.screen :as chat-icon]
            [status-im.components.react :as rn]
            [status-im.i18n :as i18n]
            [status-im.transactions.styles.list-item :as st]))

(defview item-image [contact]
  [rn/view {:style st/item-photo}
   (if-not (empty? contact)
     [chat-icon/chat-icon (:photo-path contact) {:size st/photo-size}]
     [rn/view {:style st/photo-placeholder}])
   [rn/image {:source {:uri :icon_arrow_left_white}
              :style  st/item-photo-icon}]])

(defn item-info [recipient-name value]
  [rn/view {:style st/item-info}
   [rn/text {:style           st/item-info-recipient
             :number-of-lines 1}
    recipient-name]
   [rn/text {:style st/item-info-amount} value]])

(defn deny-btn [transaction-id on-deny]
  [rn/touchable-highlight {:on-press #(do (rf/dispatch [:deny-transaction transaction-id])
                                          (when on-deny (on-deny)))}
   [rn/view {:style st/item-deny-btn}
    [rn/image {:source {:uri :icon_close_white}
               :style st/item-deny-btn-icon}]]])

(defview view [{:keys [to value id] :as transaction} on-deny]
  [recipient [:contact-by-address to]]
  (let [eth-value      (.fromWei js/Web3.prototype value "ether")
        value          (str (i18n/label-number eth-value) " ETH")
        recipient-name (or (:name recipient) to)]
    [rn/view {:style st/item}
     [item-image recipient]
     [item-info recipient-name value]
     [deny-btn id on-deny]]))
