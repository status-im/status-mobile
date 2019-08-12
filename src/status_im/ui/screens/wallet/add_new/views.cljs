(ns status-im.ui.screens.wallet.add-new.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-header.views :as list-header]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.text-input.view :as text-input]
            [reagent.core :as reagent]
            [clojure.string :as string]
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
    [list-header/list-header (i18n/label :t/default)]
    [list-item/list-item {:title    (i18n/label :t/generate-a-new-account) :theme :action
                          :icon     :main-icons/add :accessories [:chevron]
                          :on-press #(re-frame/dispatch [:navigate-to :add-new-account-password])}]]])

(defview colors-popover [selected-color]
  (letsubs [width [:dimensions/window-width]]
    [react/view {:padding-bottom 16}
     [react/scroll-view {:style {:margin 16}}
      (doall
       (for [color colors/account-colors]
         ^{:key color}
         [react/touchable-highlight {:on-press #(do
                                                  (re-frame/dispatch [:set-in [:generate-account :account :color] color])
                                                  (re-frame/dispatch [:hide-popover]))}
          [react/view {:height          52 :background-color color :border-radius 8 :width (* 0.7 width)
                       :justify-content :center :padding-left 12 :margin-bottom 16}
           [react/view {:height           32 :width 32 :border-radius 20 :align-items :center :justify-content :center
                        :background-color colors/black-transparent}
            (when (= selected-color color)
              [icons/icon :main-icons/check {:color colors/white}])]]]))]
     [components.common/button {:on-press    #(re-frame/dispatch [:hide-popover])
                                :label       (i18n/label :t/cancel)
                                :background? false}]]))

(defview account-added []
  (letsubs [{:keys [account]} [:generate-account]]
    [react/keyboard-avoiding-view {:flex 1}
     [status-bar/status-bar]
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
       [react/touchable-highlight {:on-press #(re-frame/dispatch [:show-popover {:view  [colors-popover (:color account)]
                                                                                 :style {:max-height "60%"}}])}
        [react/view {:height      52 :margin-top 12 :background-color (:color account) :border-radius 8
                     :align-items :flex-end :justify-content :center :padding-right 12}
         [icons/icon :main-icons/dropdown {:color colors/white}]]]]]
     [react/view {:style {:flex-direction   :row
                          :justify-content  :flex-end
                          :align-self       :stretch
                          :padding-vertical 16
                          :border-top-width 1
                          :border-top-color colors/gray-lighter
                          :padding-right    12}}
      [components.common/bottom-button {:label     (i18n/label :t/finish)
                                        :on-press  #(re-frame/dispatch [:wallet.accounts/save-generated-account])
                                        :disabled? (string/blank? (:name account))
                                        :forward?  true}]]]))

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