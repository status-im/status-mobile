(ns legacy.status-im.ui.screens.network.edit-network.views
  (:require
    [legacy.status-im.network.core :as network]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.network.edit-network.styles :as styles]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn- render-network-type
  [type _ _ manage-network]
  (let [name (case type
               :mainnet (i18n/label :t/mainnet-network)
               :goerli  (i18n/label :t/goerli-network)
               :custom  (i18n/label :t/custom))]
    [list.item/list-item
     {:title     name
      :accessory :radio
      :active    (= (get-in manage-network [:chain :value]) type)
      :on-press  #(re-frame/dispatch [::network/input-changed :chain type])}]))

(views/defview edit-network
  []
  (views/letsubs [manage-network [:networks/manage]
                  is-valid?      [:manage-network-valid?]]
    (let [custom? (= (get-in manage-network [:chain :value]) :custom)]
      [react/keyboard-avoiding-view {:flex 1}
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
           {:label          (i18n/label :t/symbol)
            :placeholder    (i18n/label :t/specify-symbol)
            :default-value  (get-in manage-network [:symbol :value])
            :on-change-text #(re-frame/dispatch [::network/input-changed :symbol %])
            :auto-focus     true}]]
         [react/view {:padding-vertical 8}
          [quo/text-input
           {:label          (i18n/label :t/rpc-url)
            :placeholder    (i18n/label :t/specify-rpc-url)
            :default-value  (get-in manage-network [:url :value])
            :on-change-text #(re-frame/dispatch [::network/input-changed :url %])}]]]
        [quo/list-header (i18n/label :t/network-chain)]
        [list/flat-list
         {:data        [:mainnet :goerli :custom]
          :key-fn      (fn [_ i] (str i))
          :render-data manage-network
          :render-fn   render-network-type}]
        (when custom?
          [react/view styles/edit-network-view
           [quo/text-input
            {:label          (i18n/label :t/network-id)
             :placeholder    (i18n/label :t/specify-network-id)
             :on-change-text #(re-frame/dispatch [::network/input-changed :network-id %])}]])]
       [react/view styles/bottom-container
        [react/view {:flex 1}]
        [quo/button
         {:after    :main-icons/next
          :type     :secondary
          :disabled (not is-valid?)
          :on-press #(re-frame/dispatch [::network/save-network-pressed])}
         (i18n/label :t/save)]]])))
