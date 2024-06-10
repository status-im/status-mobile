(ns status-im.contexts.wallet.sheets.network-preferences.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.sheets.network-preferences.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [first-section-label second-section-label selected-networks
           receiver-preferred-networks account watch-only?]}]
  (let [state                               (reagent/atom :default)
        {:keys [color address
                network-preferences-names]} (or account (rf/sub [:wallet/current-viewing-account]))
        initial-network-preferences-names   (or selected-networks network-preferences-names)
        receiver?                           (boolean (not-empty receiver-preferred-networks))
        network-preferences-names-state     (reagent/atom (if receiver? selected-networks #{}))
        toggle-network                      (fn [network-name]
                                              (reset! state :changed)
                                              (let [contains-network? (contains?
                                                                       @network-preferences-names-state
                                                                       network-name)
                                                    update-fn         (if contains-network? disj conj)
                                                    networks-count    (count
                                                                       @network-preferences-names-state)]
                                                (if (and (= networks-count 1) contains-network?)
                                                  (reset! network-preferences-names-state
                                                    (set constants/default-network-names))
                                                  (swap! network-preferences-names-state update-fn
                                                    network-name))))
        get-current-preferences-names       (fn []
                                              (if (= @state :default)
                                                initial-network-preferences-names
                                                @network-preferences-names-state))]
    (fn [{:keys [on-save on-change blur? button-label first-section-warning-label
                 second-section-warning-label title description]}]
      (let [theme                            (quo.theme/use-theme)
            network-details                  (rf/sub [:wallet/network-details])
            first-section-networks           (filter (fn [network]
                                                       (if receiver-preferred-networks
                                                         (some (fn [chain-id]
                                                                 (= (:chain-id network) chain-id))
                                                               receiver-preferred-networks)
                                                         (= (:network-name network) :mainnet)))
                                                     network-details)
            second-section-networks          (remove (fn [network]
                                                       (some (fn [chain-id]
                                                               (= (:chain-id network) chain-id))
                                                             (map :chain-id first-section-networks)))
                                                     network-details)
            current-networks                 (filter (fn [network]
                                                       (contains? (get-current-preferences-names)
                                                                  (:network-name network)))
                                                     network-details)
            sending-to-unpreferred-networks? (and receiver?
                                                  (some #(contains? @network-preferences-names-state
                                                                    (:network-name %))
                                                        second-section-networks))]
        [:<>
         ;; quo/overlay isn't compatible with sheets
         (when blur?
           [quo/blur
            {:style       style/blur
             :blur-amount 20
             :blur-radius 25}])
         [quo/drawer-top
          {:title       (or title (i18n/label :t/network-preferences))
           :description (when-not receiver?
                          (or description
                              (if watch-only?
                                (i18n/label :t/network-preferences-desc-1)
                                (i18n/label :t/network-preferences-desc-2))))
           :blur?       blur?}]
         (when-not receiver?
           [quo/data-item
            {:status          :default
             :size            :default
             :description     :default
             :label           :none
             :blur?           blur?
             :card?           true
             :title           (i18n/label :t/address)
             :custom-subtitle (fn []
                                [quo/address-text
                                 {:networks current-networks
                                  :address  address
                                  :blur?    blur?
                                  :format   :long}])
             :container-style (merge style/data-item
                                     {:background-color (colors/theme-colors colors/neutral-2_5
                                                                             colors/neutral-90
                                                                             theme)})}])
         [quo/category
          {:list-type :settings
           :blur?     blur?
           :label     (when first-section-label first-section-label)
           :data      (mapv (fn [network]
                              (utils/make-network-item
                               {:state            @state
                                :network-name     (:network-name network)
                                :color            color
                                :normal-checkbox? receiver?
                                :networks         (get-current-preferences-names)
                                :type             :checkbox
                                :on-change        (fn []
                                                    (toggle-network (:network-name
                                                                     network))
                                                    (when on-change
                                                      (let [chain-ids (map :chain-id current-networks)]
                                                        (on-change chain-ids))))}))
                            first-section-networks)}]
         (when first-section-warning-label
           [rn/view
            {:style (style/warning-container false)}
            [quo/icon :i/alert
             {:size  16
              :color colors/danger-50}]
            [quo/text
             {:size  :paragraph-2
              :style {:margin-left 4
                      :color       colors/danger-50}}
             first-section-warning-label]])
         (when (not-empty second-section-networks)
           [quo/category
            {:list-type :settings
             :blur?     blur?
             :label     (or second-section-label (i18n/label :t/layer-2))
             :data      (mapv (fn [network]
                                (utils/make-network-item
                                 {:state            @state
                                  :network-name     (:network-name network)
                                  :color            color
                                  :normal-checkbox? receiver?
                                  :networks         (get-current-preferences-names)
                                  :type             :checkbox
                                  :on-change        (fn []
                                                      (toggle-network (:network-name
                                                                       network))
                                                      (when on-change
                                                        (let [chain-ids (map :chain-id current-networks)]
                                                          (on-change chain-ids))))}))
                              second-section-networks)}])
         (when second-section-warning-label
           [rn/view
            {:style (style/warning-container sending-to-unpreferred-networks?)}
            [quo/icon :i/alert
             {:size  16
              :color colors/danger-50}]
            [quo/text
             {:size  :paragraph-2
              :style {:margin-left 4
                      :color       colors/danger-50}}
             second-section-warning-label]])
         (when sending-to-unpreferred-networks?
           [rn/view {:style (style/sending-to-unpreferred-networks-alert-container theme)}
            [rn/view
             [quo/icon :i/alert
              {:size            16
               :color           (colors/resolve-color :blue theme)
               :container-style {:margin-top 2}}]]
            [quo/text
             {:style style/sending-to-unpreferred-networks-text
              :size  :paragraph-2}
             (i18n/label :t/sending-to-networks-the-receiver-does-not-prefer)]])
         [quo/bottom-actions
          {:actions          :one-action
           :blur?            blur?
           :button-one-label (or button-label (i18n/label :t/confirm))
           :button-one-props {:disabled?           (= @state :default)
                              :on-press            (fn []
                                                     (let [chain-ids (map :chain-id current-networks)]
                                                       (on-save chain-ids)))
                              :customization-color color}}]]))))
