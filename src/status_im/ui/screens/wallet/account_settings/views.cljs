(ns status-im.ui.screens.wallet.account-settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.toolbar :as toolbar]
            [status-im.ui.components.copyable-text :as copyable-text]
            [reagent.core :as reagent]
            [quo.core :as quo]
            [status-im.ui.components.topbar :as topbar]))

(defview colors-popover [selected-color on-press]
  (letsubs [width [:dimensions/window-width]]
    [react/view {:flex 1 :padding-bottom 16}
     [react/scroll-view {:style {:margin 16}}
      (doall
       (for [color colors/account-colors]
         ^{:key color}
         [react/touchable-highlight {:on-press #(on-press color)}
          [react/view {:height          52      :background-color color :border-radius 8 :width (* 0.7 width)
                       :justify-content :center :padding-left     12    :margin-bottom 16}
           [react/view {:height           32 :width 32 :border-radius 20 :align-items :center :justify-content :center
                        :background-color colors/black-transparent}
            (when (= selected-color color)
              [icons/icon :main-icons/check {:color colors/white}])]]]))]
     [toolbar/toolbar
      {:center
       [quo/button {:on-press #(re-frame/dispatch [:hide-popover])
                    :type     :secondary}
        (i18n/label :t/cancel)]}]]))

(defn property [label value]
  [react/view {:margin-top 28}
   [react/text {:style {:color colors/gray}} label]
   (if (string? value)
     [react/text {:style {:margin-top 6}} value]
     value)])

(defview account-settings []
  (letsubs [{:keys [address color path type] :as account} [:multiaccount/current-account]
            new-account (reagent/atom nil)
            keycard? [:keycard-multiaccount?]]
    [react/keyboard-avoiding-view {:flex 1}
     [topbar/topbar
      (cond-> {:title (i18n/label :t/account-settings)}
        (and @new-account (not= "" (:name @new-account)))
        (assoc :right-accessories [{:label (i18n/label :t/apply)
                                    :on-press
                                    #(do
                                       (re-frame/dispatch [:wallet.accounts/save-account
                                                           account
                                                           @new-account])
                                       (reset! new-account nil))}]))]
     [react/scroll-view {:keyboard-should-persist-taps :handled
                         :style                        {:flex 1}}
      [react/view {:padding-bottom 28 :padding-top 10}
       [react/view {:margin-horizontal 16}
        [quo/text-input
         {:label               (i18n/label :t/account-name)
          :auto-focus          false
          :default-value       (:name account)
          :accessibility-label :enter-account-name
          :on-change-text      #(swap! new-account assoc :name %)}]
        [react/text {:style {:margin-top 16 :color colors/gray}} (i18n/label :t/account-color)]
        [react/touchable-highlight
         {:on-press #(re-frame/dispatch [:show-popover
                                         {:view  [colors-popover color
                                                  (fn [new-color]
                                                    (swap! new-account assoc :color new-color)
                                                    (re-frame/dispatch [:hide-popover]))]
                                          :style {:max-height "60%"}}])}
         [react/view {:height        52        :margin-top      12      :background-color (or (:color @new-account) color)
                      :border-radius 8
                      :align-items   :flex-end :justify-content :center :padding-right    12}
          [icons/icon :main-icons/dropdown {:color colors/white}]]]
        [property (i18n/label :t/type)
         (case type
           :watch       (i18n/label :t/watch-only)
           (:key :seed) (i18n/label :t/off-status-tree)
           (i18n/label :t/on-status-tree))]
        [property (i18n/label :t/wallet-address)
         [copyable-text/copyable-text-view
          {:copied-text address}
          [quo/text {:style     {:margin-top 6}
                     :monospace true}
           address]]]
        (when-not (= type :watch)
          [property (i18n/label :t/derivation-path)
           [copyable-text/copyable-text-view
            {:copied-text path}
            [quo/text {:style     {:margin-top 6}
                       :monospace true} path]]])
        (when-not (= type :watch)
          [property (i18n/label :t/storage)
           (i18n/label (if keycard?
                         :t/keycard
                         :t/this-device))])]
       (when (= type :watch)
         [react/view
          [react/view {:margin-bottom 8 :margin-top 28 :height 1 :background-color colors/gray-lighter}]
          [quo/list-item
           {:theme    :negative
            :title    (i18n/label :t/delete-account)
            :on-press #(re-frame/dispatch [:wallet.settings/show-delete-account-confirmation account])}]])]]]))
