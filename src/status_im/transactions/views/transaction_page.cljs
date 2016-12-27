(ns status-im.transactions.views.transaction-page
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require [re-frame.core :refer [subscribe dispatch]]
            [status-im.components.react :refer [view
                                                text
                                                image
                                                icon
                                                scroll-view
                                                touchable-highlight
                                                touchable-opacity]]
            [status-im.components.styles :refer [icon-ok
                                                 icon-close]]
            [status-im.transactions.styles :as st]
            [status-im.i18n :refer [label label-pluralize]]))

(defn title-bar [title id]
  [view st/title-bar
   [text {:style           st/title-bar-text
          :font            :medium
          :number-of-lines 1}
    title]
   [touchable-highlight {:style    st/icon-close-container
                         :on-press #(dispatch [:deny-transaction id])}
    [view [image {:source {:uri :icon_close_gray}
                  :style  st/icon-close}]]]])

(defn transaction-info [index [name value]]
  [view {:style st/transaction-info-item
         :key   index}
   [view {:style st/transaction-info-row}
    [view st/transaction-info-column-title
     [text {:style st/transaction-info-title} name]]
    [view st/transaction-info-column-value
     [text {:style st/transaction-info-value} value]]]])

(defview transaction-page [{:keys [id from to value] :as transaction}]
  [{:keys [name] :as contact} [:contact-by-address to]]
  (let [eth-value         (.fromWei js/Web3.prototype value "ether")
        title             (str eth-value " ETH to " (or name to))
        transactions-info [[(label :t/status) (label :t/pending-confirmation)]
                           [(label :t/recipient) (or name to)]
                           [(label :t/value) (str eth-value " ETH")]]]
    [view {:style st/transaction-page
           :key   id}
     [title-bar title id]
     [view st/scroll-view-container
      [scroll-view {:style                        st/scroll-view
                    :contentContainerStyle        st/scroll-view-content
                    :showsVerticalScrollIndicator true
                    :scrollEnabled                true}
       (map-indexed transaction-info transactions-info)]]]))
