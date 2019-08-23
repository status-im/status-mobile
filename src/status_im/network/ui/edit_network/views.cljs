(ns status-im.network.ui.edit-network.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [re-frame.core :as re-frame]
   [status-im.ui.components.react :as react]
   [status-im.i18n :as i18n]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.components.toolbar.view :as toolbar]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.text-input.view :as text-input]
   [status-im.network.core :as network]
   [status-im.network.ui.edit-network.styles :as styles]
   [clojure.string :as string]))

(defn- render-network-type [manage-network type]
  (let [name (case type
               :mainnet (i18n/label :t/mainnet-network)
               :testnet (i18n/label :t/ropsten-network)
               :rinkeby (i18n/label :t/rinkeby-network)
               :custom (i18n/label :t/custom))]
    [list/list-item-with-radio-button
     {:checked?        (= (get-in manage-network [:chain :value]) type)
      :on-value-change #(re-frame/dispatch [::network/input-changed :chain type])}
     [list/item
      nil [list/item-primary-only name]]]))

(views/defview edit-network []
  (views/letsubs [manage-network [:networks/manage]
                  is-valid?      [:manage-network-valid?]]
    (let [custom? (= (get-in manage-network [:chain :value]) :custom)]
      [react/view styles/container
       [status-bar/status-bar]
       [react/keyboard-avoiding-view components.styles/flex
        [toolbar/simple-toolbar (i18n/label :t/add-network)]
        [react/scroll-view
         [react/view styles/edit-network-view
          [text-input/text-input-with-label
           {:label          (i18n/label :t/name)
            :placeholder    (i18n/label :t/specify-name)
            :container      styles/input-container
            :default-value  (get-in manage-network [:name :value])
            :on-change-text #(re-frame/dispatch [::network/input-changed :name %])
            :auto-focus     true}]
          [text-input/text-input-with-label
           {:label          (i18n/label :t/rpc-url)
            :placeholder    (i18n/label :t/specify-rpc-url)
            :container      styles/input-container
            :default-value  (get-in manage-network [:url :value])
            :on-change-text #(re-frame/dispatch [::network/input-changed :url (string/lower-case %)])}]
          [react/i18n-text {:key :network-chain}]
          [react/view styles/network-type
           [list/flat-list {:data      [:mainnet :testnet :rinkeby :custom]
                            :key-fn    (fn [_ i] (str i))
                            :separator list/base-separator
                            :render-fn #(render-network-type manage-network %)}]]
          (when custom?
            [text-input/text-input-with-label
             {:label          (i18n/label :t/network-id)
              :container      styles/input-container
              :placeholder    (i18n/label :t/specify-network-id)
              :on-change-text #(re-frame/dispatch [::network/input-changed :network-id %])}])]]
        [react/view styles/bottom-container
         [react/view components.styles/flex]
         [components.common/bottom-button
          {:forward?  true
           :label     (i18n/label :t/save)
           :disabled? (not is-valid?)
           :on-press  #(re-frame/dispatch [::network/save-network-pressed])}]]]])))
