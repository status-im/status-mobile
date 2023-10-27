(ns status-im.ui.screens.wallet.address-add-edit.views
  (:require
    [reagent.core :as reagent]
    [react-native.safe-area :as safe-area]
    [utils.i18n :as i18n]
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [quo.core :as quo]
    [quo.theme :as quo.theme]
    [status-im2.contexts.wallet.common.screen-base.create-or-edit-account.view :as
     create-or-edit-account]
    [utils.re-frame :as rf]
    [status-im.ui.screens.wallet.address-add-edit.style :as style]
    [status-im.ui.screens.wallet.address-add-edit.utils :as utils]))

(defn- view-internal
  []
  (let [{:keys [accounts-count address]} (rf/sub [:get-screen-params])
        account-name (reagent/atom (str "Account " accounts-count))
        address-title (i18n/label :t/watch-address)
        account-color (reagent/atom :purple)
        account-emoji (reagent/atom (utils/random-emoji))
        on-change-name #(reset! account-name %)
        on-change-color #(reset! account-color %)
        on-change-emoji #(reset! account-emoji %)
        safe-bottom (safe-area/get-bottom)]
    (fn []
      [rn/view {:style style/container}
       [create-or-edit-account/view
        {:page-nav-right-side [{:icon-name :i/info
                                :on-press
                                #(js/alert
                                   "Get info (to be
                                implemented)")}]
         :account-name        @account-name
         :account-emoji       @account-emoji
         :account-color       @account-color
         :on-change-name      on-change-name
         :on-change-color     on-change-color
         :on-change-emoji     on-change-emoji}
        [quo/data-item
         {:card?           true
          :right-icon      :i/advanced
          :icon-right?     true
          :emoji           @account-emoji
          :title           address-title
          :subtitle        address
          :status          :default
          :size            :default
          :container-style style/data-item
          :on-press #(js/alert "To be implemented")}]]
       [rn/view {:style (style/button-container safe-bottom)}
        [quo/bottom-actions
         {:button-one-label     (i18n/label :t/create-account)
          :button-one-disabled? (clojure.string/blank? @account-name)
          :button-one-press     #(re-frame/dispatch [:navigate-to :wallet-account])
          :customization-color  @account-color}]]])))

(def address-add-edit (quo.theme/with-theme view-internal))
