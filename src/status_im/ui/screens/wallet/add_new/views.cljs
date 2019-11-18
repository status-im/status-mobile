(ns status-im.ui.screens.wallet.add-new.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as topbar]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-item.views :as list-item]
            [reagent.core :as reagent]
            [cljs.spec.alpha :as spec]
            [status-im.multiaccounts.db :as multiaccounts.db]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ethereum.core :as ethereum]))

(defn add-account []
  [react/view {:flex 1}
   [topbar/simple-toolbar]
   [react/scroll-view {:keyboard-should-persist-taps :handled
                       :style                        {:flex 1}}
    [react/view {:align-items :center :padding-horizontal 40 :margin-bottom 52}
     [react/text {:style {:typography :header :margin-top 16}}
      (i18n/label :t/add-an-account)]
     [react/text {:style {:color colors/gray :text-align :center :margin-top 16 :line-height 22}}
      (i18n/label :t/add-account-description)]]
    [list-item/list-item
     {:type  :section-header
      :title :t/default}]
    [list-item/list-item
     {:title       :t/generate-a-new-account
      :theme       :action
      :icon        :main-icons/add
      :accessories [:chevron]
      :on-press    #(re-frame/dispatch [:wallet.accounts/start-adding-new-account {:type :generate}])}]
    [list-item/list-item
     {:type                 :section-header
      :container-margin-top 24
      :title                "Advanced"}]
    [list-item/list-item
     {:title       "Enter a seed phrase"
      :theme       :action
      :icon        :main-icons/add
      :accessories [:chevron]
      :disabled?   true
      :on-press    #(re-frame/dispatch [:wallet.accounts/start-adding-new-account {:type :seed}])}]
    [list-item/list-item
     {:title       "Enter a private key"
      :theme       :action
      :icon        :main-icons/add
      :accessories [:chevron]
      :disabled?   true
      :on-press    #(re-frame/dispatch [:wallet.accounts/start-adding-new-account {:type :key}])}]]])

(def input-container
  {:flex-direction     :row
   :align-items        :center
   :border-radius      components.styles/border-radius
   :height             52
   :margin             16
   :padding-horizontal 16
   :background-color   colors/gray-lighter})

(defview add-watch-account []
  (letsubs [{:keys [address]} [:add-account]]
    [react/keyboard-avoiding-view {:flex 1}
     [topbar/simple-toolbar]
     [react/view {:flex            1
                  :justify-content :space-between
                  :align-items     :center :margin-horizontal 16}
      [react/view
       [react/text {:style {:typography :header :margin-top 16}} "Add a watch-only address"]
       [react/text {:style {:color colors/gray :text-align :center :margin-vertical 16}}
        "Enter the address to watch"]]
      [react/view {:align-items :center :flex 1 :flex-direction :row}
       [react/text-input {:auto-focus        true
                          :multiline         true
                          :text-align        :center
                          :placeholder       "Enter address"
                          :style             {:typography :header :flex 1}
                          :on-change-text    #(re-frame/dispatch [:set-in [:add-account :address] %])}]]]
     [toolbar/toolbar
      {:show-border? true
       :right        {:type      :next
                      :label     "Next"
                      :on-press  #(re-frame/dispatch [:wallet.accounts/add-watch-account])
                      :disabled? (not (ethereum/address? address))}}]]))

(defview password []
  (letsubs [{:keys [error]} [:add-account]
            entered-password (reagent/atom "")]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [topbar/simple-toolbar]
     [react/view {:flex            1
                  :justify-content :space-between
                  :align-items     :center :margin-horizontal 16}
      [react/text {:style {:typography :header :margin-top 16}} (i18n/label :t/enter-your-password)]
      [react/view {:justify-content :center :flex 1}
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
     [toolbar/toolbar
      {:show-border? true
       :right        {:type      :next
                      :label     :t/generate-account
                      :on-press  #(re-frame/dispatch [:wallet.accounts/generate-new-account @entered-password])
                      :disabled? (not (spec/valid? ::multiaccounts.db/password @entered-password))}}]]))