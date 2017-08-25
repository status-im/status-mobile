(ns status-im.ui.screens.wallet.history.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [status-im.components.button.view :as btn]
            [status-im.components.react :as rn]
            [status-im.components.list.views :as list]
            [status-im.components.tabs.styles :as tst]
            [status-im.components.tabs.views :as tabs]
            [status-im.components.toolbar-new.actions :as act]
            [status-im.components.toolbar-new.view :as toolbar]
            [status-im.ui.screens.wallet.history.styles :as st]
            [status-im.utils.utils :as utils]
            [status-im.i18n :as i18n]))

(defn on-sign-transaction
  [m]
  ;; TODO(yenda) implement
  (utils/show-popup "TODO" "Sign Transaction"))

(defn on-delete-transaction
  [m]
  ;; TODO(yenda) implement
  (utils/show-popup "TODO" "Delete Transaction"))

(defn unsigned-action []
  [toolbar/text-action #(rf/dispatch [:navigate-to-modal :wallet-transactions-sign-all])
   (i18n/label :t/transactions-sign-all)])

(defn history-action []
  ;; TODO(jeluard) replace with proper icon
  [toolbar/text-action #(rf/dispatch [:navigate-to-modal :wallet-transactions-filter])
   "History"])

(defn toolbar-view [view-id]
  [toolbar/toolbar
   {:title         (i18n/label :t/transactions)
    :custom-action
    [(if (= @view-id :wallet-transactions-unsigned) unsigned-action history-action)]}])

(defn- icon-status [k]
  (case k
    :pending :dropdown_white
    :dropdown_white))

(defn action-buttons [m]
  [rn/view {:style st/action-buttons}
   [btn/primary-button {:text (i18n/label :t/transactions-sign) :on-press #(on-sign-transaction m)}]
   [btn/secondary-button {:text (i18n/label :t/transactions-delete) :on-press #(on-delete-transaction m)}]])

(defn- unsigned? [state] (= "unsigned" state))

(defn render-transaction [{:keys [to state] {:keys [value symbol]} :content :as m}]
  [list/item
   [list/item-icon :icons/ok {:color :blue}]
   [list/item-content
    (str value " " symbol) (str (i18n/label :t/transactions-to) " " to)
    (if (unsigned? state)
      [action-buttons m])]
   [list/item-icon :icons/forward]])

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
  [list/section-list {:sections        dummy-transaction-data-sorted
                      :render-fn       render-transaction
                      :empty-component (empty-text (i18n/label :t/transactions-history-empty))}])

(defview unsigned-list []
  [list/flat-list {:data            dummy-transaction-data
                   :render-fn       render-transaction
                   :empty-component (empty-text (i18n/label :t/transactions-unsigned-empty))}])

(def tab-list
  [{:view-id :wallet-transactions-unsigned
    :title   (i18n/label :t/transactions-unsigned)
    :screen  unsigned-list}
   {:view-id :wallet-transactions-history
    :title   (i18n/label :t/transactions-history)
    :screen  history-list}])

(def tab->index (reduce #(assoc %1 (:view-id %2) (count %1)) {} tab-list))

(defn get-tab-index [view-id]
  (get tab->index view-id 0))

;; Sign all

(defview sign-all []
  []
  [rn/keyboard-avoiding-view {:style st/sign-all-view}
   [rn/view {:style st/sign-all-done}
    [btn/primary-button {:style    st/sign-all-done-button
                         :text     (i18n/label :t/transactions-sign-all-done)
                         :on-press #(rf/dispatch [:navigate-back])}]]
   [rn/view {:style st/sign-all-popup}
    [rn/text {:style st/sign-all-popup-sign-phrase} "one two three"] ;; TODO hook
    [rn/text {:style st/sign-all-popup-text} (i18n/label :t/transactions-sign-all-text)]
    [rn/view {:style st/sign-all-actions}
     [rn/text-input {:style             st/sign-all-input
                     :secure-text-entry true
                     :placeholder       (i18n/label :t/transactions-sign-input-placeholder)}]
     [btn/primary-button {:text (i18n/label :t/transactions-sign-all) :on-press #(on-sign-transaction %)}]]]])

;; Filter history

(defn- token->icon [s]
  (case s
    "GNO" [:icons/ok {:color :green}] ;TODO :dollar
    [:icons/ok {:color :blue}]))

(defn item-tokens [{:keys [symbol label]}]
  [list/item
   ((comp vec flatten vector) list/item-icon (token->icon symbol))
   [list/item-content label symbol]])
   ;; TODO checkbox


(defn- type->icon [k]
  (case k
    "incoming"  [:icon/ok {:color :blue}] ;TODO :dollar_green
    "outgoing"  [:icon/ok {:color :blue}]
    "pending"   [:icon/ok {:color :blue}] ;TODO :dollar_green
    "postponed" [:icon/ok {:color :blue}]
    [:icon/ok {:color :blue}]))

(defn item-type [{:keys [id label]}]
  [list/item
   ((comp vec flatten vector) list/item-icon (type->icon id))
   [list/item-content label]])
   ;; TODO checkbox


(def filter-data
  [{:title (i18n/label :t/transactions-filter-tokens)
    :key :tokens
    :render-fn item-tokens
    :data [{:symbol "GNO" :label "Gnosis"}
           {:symbol "SNT" :label "Status Network Token"}
           {:symbol "SGT" :label "Status Genesis Token"}
           {:symbol "GOL" :label "Golem"}]}
   {:title (i18n/label :t/transactions-filter-type)
    :key :type
    :render-fn item-type
    :data [{:id :incoming :label "Incoming"}
           {:id :outgoing :label "Outgoing"}
           {:id :pending :label "Pending"}
           {:id :postponed :label "Postponed"}]}])

(defview filter-history []
  []
  [rn/view
   [toolbar/toolbar
    ;; TODO(jeluard) replace with icon when available and toolbar has been refactored
    {:title      (i18n/label :t/transactions-filter-title)
     :nav-action (act/back #(rf/dispatch [:navigate-back]));; TODO close modal
     :custom-action
                 [toolbar/text-action #(rf/dispatch [:navigate-to-modal :wallet-transactions-filter])
                  (i18n/label :t/transactions-filter-select-all)]}]
   [rn/scroll-view
    [list/section-list {:sections filter-data}]]])

;; TODO(jeluard) whole swipe logic
;; extract navigate-tab action (on tap)

(defn main-section [view-id]
  [rn/view {:style st/main-section}
   [tabs/tabs {:selected-view-id @view-id
               :tab-list         tab-list}]
   [rn/swiper (merge tst/swiper
                     {:index                  (get-tab-index @view-id)
                      :loop                   false})
                      ;:ref                    #(reset! swiper %)
                      ;:on-momentum-scroll-end (on-scroll-end swiped? scroll-ended @view-id)

    (doall
      (map-indexed (fn [index {screen :screen}]
                     ^{:key index} [screen]) tab-list))]])

;; TODO(yenda) must reflect selected wallet

(def initial-tab (-> tab-list first :view-id))

(defview transactions []
  []
  (let [view-id (r/atom initial-tab)]
    [rn/view {:style st/wallet-transactions-container}
     [toolbar-view view-id]
     [rn/scroll-view
      [main-section view-id]]]))
