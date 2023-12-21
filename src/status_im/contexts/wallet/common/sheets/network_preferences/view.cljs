(ns status-im.contexts.wallet.common.sheets.network-preferences.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.foundations.resources :as resources]
            [quo.theme :as quo.theme]
            [react-native.blur :as blur]
            [reagent.core :as reagent]
            [status-im.contexts.wallet.common.sheets.network-preferences.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))


(defn- make-network-item
  [{:keys [network-name] :as _network}
   {:keys [title color on-change network-preferences state blur?] :as _options}]
  {:title        (or title (string/capitalize (name network-name)))
   :blur?        blur?
   :image        :icon-avatar
   :image-props  {:icon (resources/get-network network-name)
                  :size :size-20}
   :action       :selector
   :action-props {:type                (if (= :default state)
                                         :filled-checkbox
                                         :checkbox)
                  :customization-color color
                  :checked?            (contains? network-preferences network-name)
                  :on-change           on-change}})

(defn- view-internal
  [{:keys [selected-networks]}]
  (let [state                               (reagent/atom :default)
        {:keys [color address
                network-preferences-names]} (rf/sub [:wallet/current-viewing-account])
        initial-network-preferences-names   (or selected-networks network-preferences-names)
        network-preferences-names-state     (reagent/atom #{})
        toggle-network                      (fn [network-name]
                                              (reset! state :changed)
                                              (if (contains? @network-preferences-names-state
                                                             network-name)
                                                (swap! network-preferences-names-state disj
                                                  network-name)
                                                (swap! network-preferences-names-state conj
                                                  network-name)))
        get-current-preferences-names       (fn []
                                              (if (= @state :default)
                                                initial-network-preferences-names
                                                @network-preferences-names-state))]
    (fn [{:keys [on-save blur? theme]}]
      (let [network-details  (rf/sub [:wallet/network-details])
            mainnet          (first network-details)
            layer-2-networks (rest network-details)
            current-networks (filter (fn [network]
                                       (contains? (get-current-preferences-names)
                                                  (:network-name network)))
                                     network-details)]
        [:<>
         ;; quo/overlay isn't compatible with sheets
         (when blur?
           [blur/view
            {:style       style/blur
             :blur-amount 20
             :blur-radius 25}])
         [quo/drawer-top
          {:title       (i18n/label :t/network-preferences)
           :description (i18n/label :t/network-preferences-desc)
           :blur?       blur?}]
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
                                                                           theme)})}]
         [quo/category
          {:list-type :settings
           :blur?     blur?
           :data      [(make-network-item mainnet
                                          {:state               @state
                                           :title               (i18n/label :t/mainnet)
                                           :color               color
                                           :blur?               blur?
                                           :network-preferences (get-current-preferences-names)
                                           :on-change           #(toggle-network (:network-name
                                                                                  mainnet))})]}]
         [quo/category
          {:list-type :settings
           :blur?     blur?
           :label     (i18n/label :t/layer-2)
           :data      (mapv (fn [network]
                              (make-network-item network
                                                 {:state               @state
                                                  :color               color
                                                  :blur?               blur?
                                                  :network-preferences (get-current-preferences-names)
                                                  :on-change           #(toggle-network (:network-name
                                                                                         network))}))
                            layer-2-networks)}]
         [quo/bottom-actions
          {:button-one-label (i18n/label :t/update)
           :button-one-props {:disabled?           (= @state :default)
                              :on-press            (fn []
                                                     (let [chain-ids (map :chain-id current-networks)]
                                                       (on-save chain-ids)))
                              :customization-color color}}]]))))

(def view (quo.theme/with-theme view-internal))
