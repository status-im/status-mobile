(ns status-im.ui.screens.network.edit-network.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.network.core :as network]
            [status-im.ui.screens.network.edit-network.styles :as styles]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [status-im.ui.components.topbar :as topbar])
  (:require-macros [status-im.utils.views :as views]))

(defn- render-network-type [type _ _ manage-network]
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
       [react/keyboard-avoiding-view {:flex 1}
        [topbar/topbar {:title (i18n/label :t/add-network)}]
        [react/scroll-view
         [react/view styles/edit-network-view
          [react/view {:padding-vertical 8}
           [quo/text-input
            {:label          (i18n/label :t/name)
             :placeholder    (i18n/label :t/specify-name)
             :default-value  (get-in manage-network [:name :value])
             :on-change-text #(re-frame/dispatch [::network/input-changed :name %])
             :auto-focus     true}]]
          [react/view {:padding-vertical 8}
           [quo/text-input
            {:label          (i18n/label :t/rpc-url)
             :placeholder    (i18n/label :t/specify-rpc-url)
             :default-value  (get-in manage-network [:url :value])
             :on-change-text #(re-frame/dispatch [::network/input-changed :url (string/lower-case %)])}]]
          [react/view {:padding-vertical 8}
           [react/i18n-text {:key :network-chain}]
           [list/flat-list {:data        [:mainnet :testnet :rinkeby :custom]
                            :key-fn      (fn [_ i] (str i))
                            :separator   list/base-separator
                            :render-data manage-network
                            :render-fn   render-network-type}]]
          (when custom?
            [react/view {:padding-vertical 8}
             [quo/text-input
              {:label          (i18n/label :t/network-id)
               :placeholder    (i18n/label :t/specify-network-id)
               :on-change-text #(re-frame/dispatch [::network/input-changed :network-id %])}]])]]
        [react/view styles/bottom-container
         [react/view {:flex 1}]
         [quo/button
          {:after      :main-icons/next
           :type       :secondary
           :disabled   (not is-valid?)
           :on-press  #(re-frame/dispatch [::network/save-network-pressed])}
          (i18n/label :t/save)]]]])))
