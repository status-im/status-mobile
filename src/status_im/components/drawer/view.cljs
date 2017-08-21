(ns status-im.components.drawer.view
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [status-im.components.chat-icon.screen :as ci]
            [status-im.components.common.common :as common]
            [status-im.components.context-menu :as context-menu]
            [status-im.components.drawer.styles :as st]
            [status-im.components.react :refer [view
                                                text
                                                text-input
                                                list-item
                                                list-view
                                                drawer-layout
                                                touchable-without-feedback
                                                touchable-highlight
                                                touchable-opacity
                                                dismiss-keyboard!]]
            [status-im.components.icons.vector-icons :as vi]
            [status-im.components.status-view.view :as status-view]
            [status-im.i18n :as i18n]
            [status-im.ui.screens.profile.db :as profile.db]
            [status-im.utils.datetime :as time]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.listview :as lw]
            [status-im.utils.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im.utils.money :as money]))

(defonce drawer-atom (atom nil))
(defn open-drawer! [] (.openDrawer @drawer-atom))
(defn close-drawer! [] (.closeDrawer @drawer-atom))

;; the save-event subscription is here because no save action would
;; be dispatched when the user presses a screen changing zone while
;; editing the name or status field

(defn save-profile! []
  (when-let [save-event @(rf/subscribe [:my-profile.drawer/save-event])]
    (rf/dispatch [save-event])))

(defn navigate-to-profile []
  (close-drawer!)
  (save-profile!)
  (rf/dispatch [:navigate-to :my-profile]))

(defn navigate-to-accounts []
  (close-drawer!)
  (save-profile!)
  (rf/dispatch [:navigate-to :accounts]))

(defview profile-picture []
  (letsubs [account [:get-current-account]]
    [touchable-opacity {:on-press navigate-to-profile
                        :style    st/user-photo-container}
     [view
      [ci/chat-icon (:photo-path account) {:size                52
                                           :accessibility-label :drawer-profile-icon}]]]))

(defview name-input []
  (letsubs [profile-name [:my-profile.drawer/get :name]
            valid-name?  [:my-profile.drawer/valid-name?]
            placeholder  [:get :my-profile/default-name]]
    [view st/name-input-wrapper
     [text-input
      {:placeholder    placeholder
       :style          (st/name-input-text valid-name?)
       :font           :medium
       :default-value profile-name
       :on-focus       #(rf/dispatch [:my-profile.drawer/edit-name])
       :on-change-text #(rf/dispatch [:my-profile.drawer/update-name %])
       :on-end-editing #(rf/dispatch [:my-profile.drawer/save-name])}]]))

(defview status-input []
  (letsubs [edit-status? [:my-profile.drawer/get :edit-status?]
            status       [:my-profile.drawer/get :status]]
    (let [placeholder (i18n/label :t/update-status)]
      [view st/status-container
       (if edit-status?
         [text-input {:style               st/status-input-view
                      :multiline           true
                      :auto-focus          true
                      :focus               edit-status?
                      :max-length          140
                      :accessibility-label :drawer-status-input
                      :placeholder         placeholder
                      :default-value       status
                      :on-change-text      #(rf/dispatch [:my-profile.drawer/update-status %])
                      :on-end-editing      #(when edit-status?
                                              (rf/dispatch [:my-profile.drawer/save-status]))}]

         [status-view/status-view {:style           (st/status-view (str/blank? status))
                                   :number-of-lines 3
                                   :status          (if (str/blank? status)
                                                      placeholder
                                                      status)
                                   :on-press        #(rf/dispatch [:my-profile.drawer/edit-status])}])])))

(defview transaction-list-item [{:keys [to value timestamp] :as transaction}]
  [recipient [:contact-by-address to]]
  (let [eth-value      (str (money/wei->ether value))
        value          (i18n/label-number eth-value)
        recipient-name (or (:name recipient) to)]
    [touchable-highlight {:on-press #(rf/dispatch [:navigate-to-modal :transaction-details transaction])}
     [view {:style st/transaction}
      [vi/icon :icons/arrow-right {:container-style st/transaction-icon}]
      [view {:style st/transaction-info}
       [view {:style st/transaction-value-container}
        [text {:style st/transaction-value :font :medium} value]
        [text {:style st/transaction-unit} "ETH"]]
       [view {:style st/transaction-details-container}
        [text {:style st/transaction-to} (i18n/label :t/to)]
        [text {:style st/transaction-recipient :number-of-lines 1} recipient-name]
        [text {:style st/transaction-time} (time/format-date "dd MMM HH:mm" (time/to-date timestamp))]]]
      [view {:style st/transaction-picture}
       (when recipient
         [ci/chat-icon (:photo-path recipient) {:size 40}])]]]))

(defn render-separator-fn [transactions-count]
  (fn [_ row-id _]
    (when (< row-id (dec transactions-count))
      (list-item
        ^{:key row-id}
        [common/separator {} st/transactions-list-separator]))))

(defview unsigned-transactions []
  [all-transactions [:transactions]]
  (let [transactions (take 2 (sort-by :timestamp > all-transactions))]
    (if (empty? transactions)
      [view {:style st/empty-transactions-title-container}
       [text {:style st/transactions-title} (i18n/label :t/no-unsigned-transactions)]]

      [view
       [view {:style st/transactions-title-container}
        [text {:style st/transactions-title} (i18n/label :t/unsigned-transactions)]]
       [list-view {:dataSource      (lw/to-datasource transactions)
                   :renderSeparator (render-separator-fn (count transactions))
                   :renderRow       (fn [row _ _] (list-item [transaction-list-item row]))}]
       [touchable-opacity {:style    st/view-all-transactions-button
                           :on-press #(rf/dispatch [:navigate-to-modal :unsigned-transactions])}
        [text {:style      st/view-all-transactions-text
               :font       (if platform/android? :medium :default)
               :uppercase? platform/android?}
         (i18n/label :t/view-all)]]])))

(defview current-network []
  (letsubs [network [:get-current-account-network]]
    [view {:style st/network-label-container}
     [text {:style st/network-label} (i18n/label :t/current-network)]
     [text {:style st/network-title} (:name network)]]))

(defn options-btn []
  [view {:style st/options-button}
   [touchable-highlight
    {:on-press navigate-to-profile}
    [view [vi/icon :icons/options]]]])

(defn switch-account []
  [view st/switch-account-container
   [touchable-highlight
    {:on-press navigate-to-accounts}
    [view
     [text {:style      st/switch-account-text
            :font       (if platform/android? :medium :default)
            :uppercase? platform/android?}
      (i18n/label :t/switch-users)]]]])

(defn drawer []
  (fn []
    [touchable-without-feedback {:on-press #(dismiss-keyboard!)}
     [view st/drawer
      [view st/upper-container
       [view st/profile-container
        [profile-picture]
        [name-input]
        [status-input]
        [options-btn]]
       [current-network]]
      [view
       [unsigned-transactions]
       [switch-account]]]]))

(defn drawer-view [items]
  [drawer-layout {:drawerWidth          300
                  :renderNavigationView #(r/as-element [drawer])
                  :onDrawerSlide        dismiss-keyboard!
                  :ref                  (fn [drawer]
                                          (reset! drawer-atom drawer))}
   items])
