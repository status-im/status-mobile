(ns status-im.ui.screens.wallet.settings.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.ethereum.tokens :as tokens]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.list-header.views :as list-header]
            [status-im.ui.components.chat-icon.screen :as chat-icon]))

(defn toolbar []
  [toolbar/toolbar nil
   [toolbar/nav-button
    (toolbar.actions/back #(re-frame/dispatch [:update-wallet-and-nav-back]))]
   [toolbar/content-title
    (i18n/label :t/wallet-assets)]])

(defn- render-token [{:keys [symbol name icon color]} visible-tokens]
  [list/list-item-with-checkbox
   {:checked?        (contains? visible-tokens (keyword symbol))
    :on-value-change #(re-frame/dispatch [:wallet.settings/toggle-visible-token (keyword symbol) %])}
   [list/item
    (if icon
      [list/item-image icon]
      [chat-icon/custom-icon-view-list name color])
    [list/item-content
     [list/item-primary name]
     [list/item-secondary symbol]]]])

(defview manage-assets []
  (letsubs [network        [:network]
            visible-tokens [:wallet/visible-tokens-symbols]
            all-tokens     [:wallet/all-tokens]]
    (let [chain-key (ethereum/network->chain-keyword network)
          {custom-tokens true default-tokens nil} (group-by :custom? (tokens/sorted-tokens-for all-tokens chain-key))]
      [react/view (merge components.styles/flex {:background-color :white})
       [status-bar/status-bar]
       [toolbar]
       [react/view {:style {:flex 1 :padding-top 16}}
        [action-button/action-button {:label     (i18n/label :t/add-custom-token)
                                      :icon      :main-icons/add
                                      :icon-opts {:color :blue}
                                      :on-press  #(re-frame/dispatch [:navigate-to :wallet-add-custom-token])}]
        [list/section-list
         {:sections                    (concat
                                        (when (seq custom-tokens)
                                          [{:title (i18n/label :t/custom)
                                            :data  custom-tokens}])
                                        [{:title (i18n/label :t/default)
                                          :data  default-tokens}])
          :key-fn                      :address
          :stickySectionHeadersEnabled false
          :render-section-header-fn    (fn [{:keys [title data]}]
                                         [list-header/list-header title])
          :render-fn                   #(render-token % visible-tokens)}]]])))

(defn- create-payload [address]
  {:address (ethereum/normalized-address address)})

(defview settings-hook []
  (letsubs [{:keys [label view on-close]} [:get-screen-params :wallet-settings-hook]
            {address :address} [:account/account]]
    [react/keyboard-avoiding-view {:style {:flex 1 :background-color colors/blue}}
     [status-bar/status-bar {:type :wallet}]
     [toolbar/toolbar
      {:style {:border-bottom-color colors/white-light-transparent}}
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
            {address :address} [:account/account]]
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
