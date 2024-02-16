(ns legacy.status-im.ui.screens.wallet.account-settings.views
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]])
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.copyable-text :as copyable-text]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [utils.i18n :as i18n]
    [utils.security.core :as security]))

(defn not-valid-password?
  [password]
  (< (count (security/safe-unmask-data password)) 6))

(defn delete-account
  [_]
  (let [password       (reagent/atom nil)
        text-input-ref (atom nil)
        error          (reagent/atom nil)]
    (fn [account]
      (when (and @text-input-ref error (not @password))
        (.clear ^js @text-input-ref))
      [react/view {:padding 20 :width 300}
       [quo/text-input
        {:style             {:margin-bottom 40}
         :label             (i18n/label :t/password)
         :show-cancel       false
         :secure-text-entry true
         :return-key-type   :next
         :on-submit-editing nil
         :auto-focus        true
         :on-change-text    #(reset! password (security/mask-data %))
         :get-ref           #(reset! text-input-ref %)
         :error             (when (and @error (not @password))
                              (if (= :wrong-password @error)
                                (i18n/label :t/wrong-password)
                                (str @error)))}]
       [quo/button
        {:on-press            (fn []
                                (re-frame/dispatch [:wallet-legacy.accounts/delete-key
                                                    account
                                                    @password
                                                    #(reset! error :wrong-password)])
                                (reset! password nil))
         :theme               :negative
         :accessibility-label :delete-account-confirm
         :disabled            (not-valid-password? @password)}
        (i18n/label :t/delete)]])))

(defview colors-popover
  [selected-color on-press]
  (letsubs [width [:dimensions/window-width]]
    [react/view {:flex 1 :padding-bottom 16}
     [react/scroll-view {:style {:margin 16}}
      (doall
       (for [color colors/account-colors]
         ^{:key color}
         [react/touchable-highlight {:on-press #(on-press color)}
          [react/view
           {:height           52
            :background-color color
            :border-radius    8
            :width            (* 0.7 width)
            :justify-content  :center
            :padding-left     12
            :margin-bottom    16}
           [react/view
            {:height           32
             :width            32
             :border-radius    20
             :align-items      :center
             :justify-content  :center
             :background-color colors/black-transparent}
            (when (= selected-color color)
              [icons/icon :main-icons/check {:color colors/white}])]]]))]
     [toolbar/toolbar
      {:center
       [quo/button
        {:on-press #(re-frame/dispatch [:hide-popover])
         :type     :secondary}
        (i18n/label :t/cancel)]}]]))

(defn property
  [label value]
  [react/view {:margin-top 28}
   [react/text {:style {:color colors/gray}} label]
   (if (string? value)
     [react/text {:style {:margin-top 6}} value]
     value)])

(defview account-settings
  []
  (letsubs [{:keys [address color path type] :as account} [:multiaccount/current-account]
            new-account                                   (reagent/atom nil)
            keycard?                                      [:keycard-multiaccount?]]
    [react/keyboard-avoiding-view
     {:style         {:flex 1}
      :ignore-offset true}
     [topbar/topbar
      (cond-> {:title (i18n/label :t/account-settings)}
        (and @new-account (not= "" (:name @new-account)))
        (assoc :right-accessories
               [{:label (i18n/label :t/apply)
                 :on-press
                 #(do
                    (re-frame/dispatch [:wallet-legacy.accounts/save-account
                                        account
                                        @new-account])
                    (reset! new-account nil))}]))]
     [react/scroll-view
      {:keyboard-should-persist-taps :handled
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
         [react/view
          {:height           52
           :margin-top       12
           :background-color (or (:color @new-account) color)
           :border-radius    8
           :align-items      :flex-end
           :justify-content  :center
           :padding-right    12}
          [icons/icon :main-icons/dropdown {:color colors/white}]]]
        [property (i18n/label :t/type)
         (case type
           :watch       (i18n/label :t/watch-only)
           (:key :seed) (i18n/label :t/off-status-tree)
           (i18n/label :t/on-status-tree))]
        [property (i18n/label :t/wallet-address)
         [copyable-text/copyable-text-view
          {:copied-text address}
          [quo/text
           {:style     {:margin-top 6}
            :monospace true}
           address]]]
        (when-not (= type :watch)
          [property (i18n/label :t/derivation-path)
           [copyable-text/copyable-text-view
            {:copied-text path}
            [quo/text
             {:style     {:margin-top 6}
              :monospace true} path]]])
        (when-not (= type :watch)
          [property (i18n/label :t/storage)
           (i18n/label (if keycard?
                         :t/keycard
                         :t/this-device))])]
       (when (#{:key :seed :watch} type)
         [react/view
          [react/view {:margin-bottom 8 :margin-top 28 :height 1 :background-color colors/gray-lighter}]
          [list.item/list-item
           {:theme    :negative
            :title    (i18n/label :t/delete-account)
            :on-press #(if (= :watch type)
                         (re-frame/dispatch [:wallet-legacy.settings/show-delete-account-confirmation
                                             account])
                         (re-frame/dispatch [:show-popover {:view [delete-account account]}]))}]])]]]))
