(ns status-im.ui.screens.wallet.add-new.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.common.common :as components.common]
            [reagent.core :as reagent]
            [cljs.spec.alpha :as spec]
            [status-im.multiaccounts.db :as multiaccounts.db]))

(defn add-account []
  [react/view {:flex 1}
   [status-bar/status-bar]
   [toolbar/toolbar {:transparent? true} toolbar/default-nav-back nil]
   [react/scroll-view {:keyboard-should-persist-taps :handled
                       :style                        {:flex 1}}
    [react/view {:align-items :center :padding-horizontal 40}
     [react/text {:style {:typography :header :margin-top 16}} (i18n/label :t/add-an-account)]
     [react/text {:style {:color colors/gray :text-align :center :margin-top 16 :line-height 22}}
      (i18n/label :t/add-account-description)]]
    [react/view {:height 52}]
    [list-item/list-item
     {:type  :section-header
      :title :t/default}]
    [list-item/list-item
     {:title       :t/generate-a-new-account
      :theme       :action
      :icon        :main-icons/add
      :accessories [:chevron]
      :on-press
      #(re-frame/dispatch
        [:navigate-to :add-new-account-password])}]]])

(defview password []
  (letsubs [{:keys [error]} [:generate-account]
            entered-password (reagent/atom "")]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [status-bar/status-bar {:flat? true}]
     [toolbar/toolbar {:transparent? true} toolbar/default-nav-back nil]
     [react/view {:flex 1}
      [react/view {:style {:flex            1
                           :justify-content :space-between
                           :align-items     :center :margin-horizontal 16}}
       [react/text {:style {:typography :header :margin-top 16}} (i18n/label :t/enter-your-password)]
       [react/view {:style {:justify-content :center :flex 1}}
        [react/text-input {:secure-text-entry true
                           :auto-focus        true
                           :text-align        :center
                           :placeholder       ""
                           :style             {:typography :header}
                           :on-change-text    #(reset! entered-password %)}]
        (when error
          [react/text {:style {:text-align :center :color colors/red :margin-top 76}} error])]
       [react/text {:style {:color colors/gray :text-align :center :margin-bottom 16}}
        (i18n/label :t/to-encrypt-enter-password)]]
      [react/view {:style {:flex-direction   :row
                           :justify-content  :flex-end
                           :align-self       :stretch
                           :padding-vertical 16
                           :border-top-width 1
                           :border-top-color colors/gray-lighter
                           :padding-right    12}}
       [components.common/bottom-button {:label     (i18n/label :t/generate-account)
                                         :on-press  #(re-frame/dispatch
                                                      [:wallet.accounts/generate-new-account @entered-password])
                                         :disabled? (not (spec/valid? ::multiaccounts.db/password @entered-password))
                                         :forward?  true}]]]]))