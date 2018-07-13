(ns status-im.ui.screens.network-settings.edit-network.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [status-im.thread :as status-im.thread]
   [status-im.ui.components.react :as react]
   [status-im.i18n :as i18n]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.toolbar.view :as toolbar]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.text-input.view :as text-input]
   [status-im.ui.screens.network-settings.edit-network.styles :as styles]
   [status-im.ui.components.checkbox.view :as checkbox]))

(defn- render-network-type [manage-network type]
  (let [name (case type
               :mainnet (i18n/label :t/mainnet-network)
               :testnet (i18n/label :t/ropsten-network)
               :rinkeby (i18n/label :t/rinkeby-network))]
    [list/list-item-with-checkbox
     {:checked?        (= (get-in manage-network [:chain :value]) type)
      :on-value-change #(status-im.thread/dispatch [:network-set-input :chain type])
      :plain-checkbox? true}
     [list/item
      nil [list/item-primary-only name]]]))

(views/defview edit-network []
  (views/letsubs [manage-network [:get-manage-network]
                  is-valid?      [:manage-network-valid?]]
    [react/view components.styles/flex
     [status-bar/status-bar]
     [react/keyboard-avoiding-view components.styles/flex
      [toolbar/simple-toolbar (i18n/label :t/add-network)]
      [react/scroll-view
       [react/view styles/edit-network-view
        [text-input/text-input-with-label
         {:label           (i18n/label :t/name)
          :placeholder     (i18n/label :t/specify-name)
          :container       styles/input-container
          :default-value   (get-in manage-network [:name :value])
          :on-change-text  #(status-im.thread/dispatch [:network-set-input :name %])
          :auto-focus      true}]
        [text-input/text-input-with-label
         {:label           (i18n/label :t/rpc-url)
          :placeholder     (i18n/label :t/specify-rpc-url)
          :container       styles/input-container
          :default-value   (get-in manage-network [:url :value])
          :on-change-text  #(status-im.thread/dispatch [:network-set-input :url %])}]
        [react/i18n-text {:key :network-chain}]
        [react/view styles/network-type
         [list/flat-list {:data      [:mainnet :testnet :rinkeby]
                          :key-fn    (fn [_ i] (str i))
                          :separator list/base-separator
                          :render-fn #(render-network-type manage-network %)}]]]]
      [react/view styles/bottom-container
       [react/view components.styles/flex]
       [components.common/bottom-button
        {:forward?  true
         :label     (i18n/label :t/save)
         :disabled? (not is-valid?)
         :on-press  #(status-im.thread/dispatch [:save-new-network])}]]]]))
