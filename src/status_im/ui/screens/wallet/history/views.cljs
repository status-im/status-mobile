(ns status-im.ui.screens.wallet.history.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as r]
            [status-im.components.button.view :as btn]
            [status-im.components.react :as rn]
            [status-im.components.list.styles :as list-st]
            [status-im.components.list.views :as list]
            [status-im.components.tabs.styles :as tst]
            [status-im.components.tabs.views :as tabs]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.ui.screens.wallet.history.styles :as st]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(defn on-sign-transaction
  [m]
  ;; TODO(yenda) implement
  (utils/show-popup "TODO" "Sign Transaction"))

(defn on-sign-all-transactions
  [m]
  ;; TODO(yenda) implement
  (utils/show-popup "TODO" "Sign All Transactions"))

(defn on-delete-transaction
  [m]
  ;; TODO(yenda) implement
  (utils/show-popup "TODO" "Delete Transaction"))

(defn unsigned-action []
  [rn/text {:style st/toolbar-right-action :onPress on-sign-all-transactions}
   (i18n/label :t/transactions-sign-all)])

(defn history-action []
  ;; TODO(jeluard)
  [rn/text {:style st/toolbar-right-action}
   "AAAAA"])

(defn toolbar-view [view-id]
  [toolbar/toolbar
   {:title         (i18n/label :t/transactions)
    :custom-action
    [(if (= @view-id :wallet-transactions-unsigned) unsigned-action history-action)]}])

(defn- icon-status [k]
  (case k
    :pending :dropdown_white
    :dropdown_white))

(defn action-buttons [m ]
  [rn/view {:style list-st/action-buttons}
   [btn/primary-button {:text (i18n/label :t/transactions-sign) :on-press #(on-sign-transaction m)}]
   [btn/secondary-button {:text (i18n/label :t/transactions-delete) :on-press #(on-delete-transaction m)}]])

(defn- unsigned? [state] (= "unsigned" state))

(defn transaction-details [{:keys [to state] {:keys [value symbol]} :content :as m}]
  [rn/view {:style list-st/item-text-view}
   [rn/text {:style list-st/primary-text} (str value " " symbol)]
   [rn/text {:style list-st/secondary-text :ellipsize-mode "middle" :number-of-lines 1} (str (i18n/label :t/transactions-to) " " to)]
   (if (unsigned? state)
     [action-buttons m])])

(defn render-transaction [m]
  [rn/view {:style list-st/item}
   [rn/image {:source {:uri :console}
              :style  list-st/item-icon}]
   [transaction-details m]
   [rn/icon :forward_gray list-st/secondary-action]])

(defn render-section-header [m]
  [rn/text {:style list-st/section-header} (:title m)])

(def dummy-transaction-data
  [{:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "0,4909" :symbol "ETH"} :state :unsigned}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :unsigned}
   {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :unsigned}])

(def dummy-transaction-data-sorted
  [{:title "Postponed"
    :key :postponed
    :data [{:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "0,4909" :symbol "ETH"} :state :pending}
           {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :pending}
           {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :sent}]}
   {:title "Pending"
    :key :pending
    :data [{:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "0,4909" :symbol "ETH"} :state :pending}
           {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :pending}
           {:to "0x829bd824b016326a401d083b33d092293333a830" :content {:value "10000" :symbol "SGT"} :state :sent}]}])

;; TODO(yenda) hook with re-frame

(defn empty-text [s]
  [rn/text {:style st/empty-text} s])

(defview history-list []
  [list/section-list dummy-transaction-data-sorted render-transaction render-section-header
   {:empty-component (empty-text (i18n/label :t/transactions-history-empty))}])

(defview unsigned-list []
  [list/flat-list dummy-transaction-data render-transaction
   {:empty-component (empty-text (i18n/label :t/transactions-unsigned-empty))}])

(def tab-list
  [{:view-id :wallet-transactions-unsigned
    :title         (i18n/label :t/transactions-unsigned)
    :screen        unsigned-list}
   {:view-id :wallet-transactions-history
    :title         (i18n/label :t/transactions-history)
    :screen        history-list}])

(def tab->index (reduce #(assoc %1 (:view-id %2) (count %1)) {} tab-list))

(defn get-tab-index [view-id]
  (get tab->index view-id 0))

;; TODO(jeluard) whole swipe logic
;; extract navigate-tab action (on tap)

(defn main-section [view-id]
  [rn/view {:style st/main-section}
   [tabs/tabs {:selected-view-id @view-id
               :tab-list         tab-list}]
   [rn/swiper (merge tst/swiper
                     {:index                  (get-tab-index @view-id)
                      :loop                   false
                      ;:ref                    #(reset! swiper %)
                      ;:on-momentum-scroll-end (on-scroll-end swiped? scroll-ended @view-id)
                      })
    (doall
      (map-indexed (fn [index {screen :screen}]
                     ^{:key index} [screen]) tab-list))]])

;; TODO(yenda) must reflect selected wallet

(def initial-tab (-> tab-list first :view-id))

(defview wallet-transactions []
  []
  (let [view-id (r/atom initial-tab)]
    [rn/view {:style st/wallet-transactions-container}
     [toolbar-view view-id]
     [rn/scroll-view
      [main-section view-id]]]))
