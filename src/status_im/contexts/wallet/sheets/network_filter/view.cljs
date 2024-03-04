(ns status-im.contexts.wallet.sheets.network-filter.view
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [state             (reagent/atom :default)
        networks-selected (reagent/atom #{})
        toggle-network    (fn [network-name]
                            (reset! state :changed)
                            (if (contains? @networks-selected
                                           network-name)
                              (swap! networks-selected disj
                                network-name)
                              (swap! networks-selected conj
                                network-name)))
        get-networks      (fn []
                            (if (= @state :default)
                              constants/default-network-names
                              @networks-selected))]
    (fn []
      (let [color            (rf/sub [:profile/customization-color])
            network-details  (rf/sub [:wallet/network-details])
            mainnet          (first network-details)
            layer-2-networks (rest network-details)]
        [:<>
         [quo/drawer-top {:title (i18n/label :t/select-networks)}]
         [quo/category
          {:list-type :settings
           :data      [(utils/make-network-item mainnet
                                                {:state       @state
                                                 :title       (i18n/label :t/mainnet)
                                                 :color       color
                                                 :networks    (get-networks)
                                                 :on-change   #(toggle-network (:network-name
                                                                                mainnet))
                                                 :label-props "$0.00"})]}]
         [quo/category
          {:list-type :settings
           :label     (i18n/label :t/layer-2)
           :data      (mapv (fn [network]
                              (utils/make-network-item network
                                                       {:state       @state
                                                        :color       color
                                                        :networks    (get-networks)
                                                        :on-change   #(toggle-network (:network-name
                                                                                       network))
                                                        :label-props "$0.00"}))
                            layer-2-networks)}]]))))
