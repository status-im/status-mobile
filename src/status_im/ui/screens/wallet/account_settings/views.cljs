(ns status-im.ui.screens.wallet.account-settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as topbar]
            [status-im.ui.components.text-input.view :as text-input]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.button :as button]
            [clojure.string :as string]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.copyable-text :as copyable-text]
            [reagent.core :as reagent]))

(defview colors-popover [selected-color on-press]
  (letsubs [width [:dimensions/window-width]]
    [react/view {:flex 1 :padding-bottom 16}
     [react/scroll-view {:style {:margin 16}}
      (doall
       (for [color colors/account-colors]
         ^{:key color}
         [react/touchable-highlight {:on-press #(on-press color)}
          [react/view {:height          52 :background-color color :border-radius 8 :width (* 0.7 width)
                       :justify-content :center :padding-left 12 :margin-bottom 16}
           [react/view {:height           32 :width 32 :border-radius 20 :align-items :center :justify-content :center
                        :background-color colors/black-transparent}
            (when (= selected-color color)
              [icons/icon :main-icons/check {:color colors/white}])]]]))]
     [toolbar/toolbar
      {:center {:on-press #(re-frame/dispatch [:hide-popover])
                :label    (i18n/label :t/cancel)
                :type     :secondary}}]]))

(defview account-added []
  (letsubs [{:keys [account]} [:generate-account]]
    [react/keyboard-avoiding-view {:flex 1}
     [react/scroll-view {:keyboard-should-persist-taps :handled
                         :style                        {:margin-top 70 :flex 1}}
      [react/view {:align-items :center :padding-horizontal 40}
       [react/view {:height           40 :width 40 :border-radius 20 :align-items :center :justify-content :center
                    :background-color (:color account)}
        [icons/icon :main-icons/check {:color colors/white}]]
       [react/text {:style {:typography :header :margin-top 16}}
        (i18n/label :t/account-added)]
       [react/text {:style {:color colors/gray :text-align :center :margin-top 16 :line-height 22}}
        (i18n/label :t/you-can-change-account)]]
      [react/view {:height 52}]
      [react/view {:margin-horizontal 16}
       [text-input/text-input-with-label
        {:label          (i18n/label :t/account-name)
         :auto-focus     false
         :default-value  (:name account)
         :on-change-text #(re-frame/dispatch [:set-in [:generate-account :account :name] %])}]
       [react/text {:style {:margin-top 30}} (i18n/label :t/account-color)]
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:show-popover
                                        {:view  [colors-popover (:color account)
                                                 (fn [new-color]
                                                   (re-frame/dispatch [:set-in [:generate-account :account :color] new-color])
                                                   (re-frame/dispatch [:hide-popover]))]
                                         :style {:max-height "60%"}}])}
        [react/view {:height      52 :margin-top 12 :background-color (:color account) :border-radius 8
                     :align-items :flex-end :justify-content :center :padding-right 12}
         [icons/icon :main-icons/dropdown {:color colors/white}]]]]]
     [toolbar/toolbar
      {:right {:type      :next
               :label     (i18n/label :t/finish)
               :on-press  #(re-frame/dispatch [:wallet.accounts/save-generated-account])
               :disabled? (string/blank? (:name account))}}]]))

(defn property [label value]
  [react/view {:margin-top 28}
   [react/text {:style {:color colors/gray}} label]
   (if (string? value)
     [react/text {:style {:margin-top 6}} value]
     value)])

(defview account-settings []
  (letsubs [{:keys [address color path] :as account} [:current-account]
            new-account (reagent/atom nil)]
    [react/keyboard-avoiding-view {:flex 1}
     [topbar/toolbar {}
      topbar/default-nav-back
      [topbar/content-title (i18n/label :t/account-settings)]
      (when (and @new-account (not= "" (:name @new-account)))
        [button/button {:type :secondary :label (i18n/label :t/apply)
                        :on-press #(do
                                     (re-frame/dispatch [:wallet.accounts/save-account account @new-account])
                                     (reset! new-account nil))}])]
     [react/scroll-view {:keyboard-should-persist-taps :handled
                         :style                        {:flex 1}}
      [react/view {:margin-horizontal 16 :padding-bottom 28 :padding-top 10}
       [text-input/text-input-with-label
        {:label          (i18n/label :t/account-name)
         :label-style    {:color colors/gray}
         :auto-focus     false
         :default-value  (:name account)
         :on-change-text #(swap! new-account assoc :name %)}]
       [react/text {:style {:margin-top 30 :color colors/gray}} (i18n/label :t/account-color)]
       [react/touchable-highlight
        {:on-press #(re-frame/dispatch [:show-popover
                                        {:view  [colors-popover color
                                                 (fn [new-color]
                                                   (swap! new-account assoc :color new-color)
                                                   (re-frame/dispatch [:hide-popover]))]
                                         :style {:max-height "60%"}}])}
        [react/view {:height        52 :margin-top 12 :background-color (or (:color @new-account) color)
                     :border-radius 8
                     :align-items   :flex-end :justify-content :center :padding-right 12}
         [icons/icon :main-icons/dropdown {:color colors/white}]]]
       [property (i18n/label :t/type) (i18n/label :t/on-status-tree)]
       [property (i18n/label :t/wallet-address)
        [copyable-text/copyable-text-view
         {:copied-text address}
         [react/text {:style {:margin-top 6 :font-family "monospace"}} address]]]
       [property (i18n/label :t/derivation-path)
        [copyable-text/copyable-text-view
         {:copied-text path}
         [react/text {:style {:margin-top 6 :font-family "monospace"}} path]]]
       [property (i18n/label :t/storage) (i18n/label :t/this-device)]]]]))