(ns status-im.ui.screens.wallet.add-new.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.core :as ethereum]
            [utils.i18n :as i18n]
            [status-im.multiaccounts.db :as multiaccounts.db]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.keycard.pin.views :as pin.views]
            [status-im.ui.screens.wallet.account-settings.views :as account-settings]
            [utils.security.core :as security]))

(defn add-account-topbar
  [type]
  (let [title (case type
                :generate :t/generate-an-account
                :watch    :t/add-watch-account
                :seed     :t/add-seed-account
                :key      :t/add-private-key-account
                "")]
    [topbar/topbar
     (merge {:title (i18n/label title)}
            (when (= type :watch)
              {:right-accessories
               [{:icon     :qr
                 :on-press #(re-frame/dispatch [:wallet.add-new/qr-scanner
                                                {:handler :wallet.add-new/qr-scanner-result}])}]}))]))

(defn common-settings
  [account]
  [react/view {:margin-horizontal 16}
   [quo/text-input
    {:label               (i18n/label :t/account-name)
     :auto-focus          false
     :default-value       (:name account)
     :accessibility-label :enter-account-name
     :placeholder         (i18n/label :t/account-name)
     :on-change-text      #(re-frame/dispatch [:set-in [:add-account :account :name] %])}]
   [react/text {:style {:margin-top 30}} (i18n/label :t/account-color)]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch
                 [:show-popover
                  {:view  [account-settings/colors-popover (:color account)
                           (fn [new-color]
                             (re-frame/dispatch [:set-in [:add-account :account :color] new-color])
                             (re-frame/dispatch [:hide-popover]))]
                   :style {:max-height "60%"}}])}
    [react/view
     {:height           52
      :margin-top       12
      :background-color (:color account)
      :border-radius    8
      :align-items      :flex-end
      :justify-content  :center
      :padding-right    12}
     [icons/icon :main-icons/dropdown {:color colors/white}]]]])

(defn settings
  [{:keys [type scanned-address password-error account-error]}
   entered-password]
  [react/view
   {:padding-horizontal 16
    :padding-vertical   16}
   (if (= type :watch)
     [quo/text-input
      {:label               (i18n/label :t/wallet-key-title)
       :auto-focus          false
       :default-value       scanned-address
       :monospace           true
       :placeholder         (i18n/label :t/enter-address)
       :accessibility-label :add-account-enter-watch-address
       :on-change-text      #(re-frame/dispatch [:wallet.accounts/set-account-to-watch %])}]
     [quo/text-input
      {:label               (i18n/label :t/password)
       :show-cancel         false
       :auto-focus          false
       :placeholder         (i18n/label :t/enter-your-password)
       :secure-text-entry   true
       :text-content-type   :none
       :accessibility-label :add-account-enter-password
       :bottom-value        0
       :error               (when password-error (i18n/label :t/add-account-incorrect-password))
       :on-change-text      #(do
                               (re-frame/dispatch [:set-in [:add-account :password-error] nil])
                               (reset! entered-password %))}])
   (when (= type :seed)
     [react/view {:padding-top 16}
      [quo/text-input
       {:label (i18n/label :t/recovery-phrase)
        :auto-focus false
        :placeholder (i18n/label :t/multiaccounts-recover-enter-phrase-title)
        :auto-correct false
        :keyboard-type :visible-password
        :multiline true
        :height 95
        :error account-error
        :accessibility-label :add-account-enter-seed
        :monospace true
        :on-change-text
        #(do
           (re-frame/dispatch [:set-in [:add-account :account-error] nil])
           (re-frame/dispatch [:set-in [:add-account :seed]
                               (security/mask-data (string/lower-case %))]))}]])
   (when (= type :key)
     [react/view {:margin-top 30}
      [quo/text-input
       {:label (i18n/label :t/private-key)
        :auto-focus false
        :placeholder (i18n/label :t/enter-a-private-key)
        :auto-correct false
        :keyboard-type :visible-password
        :error account-error
        :secure-text-entry true
        :accessibility-label :add-account-enter-private-key
        :text-content-type :none
        :on-change-text
        #(do
           (re-frame/dispatch [:set-in [:add-account :account-error] nil])
           (re-frame/dispatch [:set-in [:add-account :private-key] (security/mask-data %)]))}]])])

(defview pin
  []
  (letsubs [card-pin      [:keycard/pin]
            status        [:keycard/pin-status]
            error-label   [:keycard/pin-error-label]
            retry-counter [:keycard/retry-counter]]
    [react/keyboard-avoiding-view
     {:style         {:flex 1}
      :ignore-offset true}
     [topbar/topbar
      {:navigation :none
       :right-accessories
       [{:label    (i18n/label :t/cancel)
         :on-press #(re-frame/dispatch [:keycard/new-account-pin-sheet-hide])}]}]
     [pin.views/pin-view
      {:pin               card-pin
       :status            status
       :retry-counter     retry-counter
       :title-label       :t/current-pin
       :description-label :t/current-pin-description
       :error-label       error-label
       :step              :export-key}]]))

(defview add-account-view
  []
  (letsubs [{:keys [type account] :as add-account} [:add-account]
            add-account-disabled?                  [:add-account-disabled?]
            entered-password                       (reagent/atom "")
            keycard?                               [:keycard-multiaccount?]]
    [react/keyboard-avoiding-view
     {:style         {:flex 1}
      :ignore-offset true}
     [add-account-topbar type]
     [react/scroll-view
      {:keyboard-should-persist-taps :handled
       :style                        {:flex 1 :padding-top 20}}
      (when (or (not keycard?)
                (= type :watch))
        [settings add-account entered-password])
      [common-settings account]]
     [toolbar/toolbar
      {:show-border? true
       :right
       [quo/button
        {:type :secondary
         :after :main-icon/next
         :accessibility-label :add-account-add-account-button
         :on-press
         (if (and keycard?
                  (not= type :watch))
           #(re-frame/dispatch [:keycard/new-account-pin-sheet
                                {:view {:content pin
                                        :height  256}}])
           #(re-frame/dispatch [:wallet.accounts/add-new-account
                                (ethereum/sha3 @entered-password)]))
         :disabled
         (or add-account-disabled?
             (and
              (not (= type :watch))
              (and
               (not keycard?)
               (not (spec/valid? ::multiaccounts.db/password
                                 @entered-password)))))}
        (i18n/label :t/add-account)]}]]))
