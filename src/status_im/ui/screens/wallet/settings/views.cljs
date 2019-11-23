(ns status-im.ui.screens.wallet.settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.list-item.views :as list-item])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn toolbar []
  [toolbar/toolbar nil
   [toolbar/nav-button
    (actions/back #(re-frame/dispatch
                    [:wallet.settings.ui/navigate-back-pressed]))]
   [toolbar/content-title
    (i18n/label :t/wallet-assets)]])

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn custom-token-actions-view [{:keys [custom?] :as token}]
  (fn []
    [react/view
     [list-item/list-item
      {:theme    :action
       :title    :t/token-details
       :icon     :main-icons/warning
       :on-press #(hide-sheet-and-dispatch [:navigate-to :wallet-custom-token-details token])}]
     (when custom?
       [list-item/list-item
        {:theme    :action-destructive
         :title    :t/remove-token
         :icon     :main-icons/delete
         :on-press #(hide-sheet-and-dispatch [:wallet.custom-token.ui/remove-pressed token])}])]))

(defn- render-token [{:keys [symbol name icon color custom? checked?] :as token}]
  [list/list-item-with-checkbox
   {:checked?        checked?
    :on-long-press   #(re-frame/dispatch [:bottom-sheet/show-sheet {:content        (custom-token-actions-view token)
                                                                    :content-height (if custom? 128 68)}])
    :on-value-change #(re-frame/dispatch [:wallet.settings/toggle-visible-token (keyword symbol) %])}
   [list/item
    (if icon
      [list/item-image icon]
      [chat-icon/custom-icon-view-list name color])
    [list/item-content
     [list/item-primary name]
     [list/item-secondary symbol]]]])

(defview manage-assets []
  (letsubs [{custom-tokens true default-tokens nil} [:wallet/grouped-chain-tokens]]
    [react/view (merge components.styles/flex {:background-color :white})
     [toolbar]
     [react/view {:style components.styles/flex}
      [list/section-list
       {:header                      [react/view {:margin-top 16}
                                      [list-item/list-item
                                       {:theme     :action
                                        :title     :t/add-custom-token
                                        :icon      :main-icons/add
                                        :on-press  #(re-frame/dispatch [:navigate-to :wallet-add-custom-token])}]]
        :sections                    (concat
                                      (when (seq custom-tokens)
                                        [{:title (i18n/label :t/custom)
                                          :data  custom-tokens}])
                                      [{:title (i18n/label :t/default)
                                        :data  default-tokens}])
        :key-fn                      :address
        :stickySectionHeadersEnabled false
        :render-section-header-fn    (fn [{:keys [title data]}]
                                       [list-item/list-item {:type :section-header :title title}])
        :render-fn                   render-token}]]]))

(defn- create-payload [address]
  {:address (ethereum/normalized-address address)})

(defview settings-hook []
  (letsubs [{:keys [label view on-close]} [:get-screen-params :wallet-settings-hook]
            {address :address} [:multiaccount]]
    [react/keyboard-avoiding-view {:style {:flex 1 :background-color colors/blue}}
     [toolbar/toolbar
      {:style {:border-bottom-color colors/white-transparent-10}}
      [toolbar/nav-button
       (actions/back-white
        #(re-frame/dispatch [:wallet.settings.ui/navigate-back-pressed
                             (when (fn? on-close)
                               (on-close (create-payload address)))]))]
      [toolbar/content-title {:color colors/white}
       label]]
     [view (create-payload address)]]))

(defn- setting->action [address {:keys [label on-open] :as m}]
  {:label  label
   :action #(do
              (when (fn? on-open)
                (on-open (create-payload address)))
              (re-frame/dispatch [:navigate-to :wallet-settings-hook m]))})

(defview toolbar-view []
  (letsubs [settings [:wallet/settings]
            {address :address} [:multiaccount]]
    [toolbar/toolbar {:style {:background-color    colors/blue
                              :border-bottom-width 0}}
     nil
     [toolbar/content-wrapper]
     [toolbar/actions
      [{:icon      :main-icons/more
        :icon-opts {:color               colors/white
                    :accessibility-label :options-menu-button}
        :options   (into [{:label  (i18n/label :t/wallet-manage-assets)
                           :action #(re-frame/dispatch [:navigate-to :wallet-settings-assets])}]
                         (map #(setting->action address %) settings))}]]]))
