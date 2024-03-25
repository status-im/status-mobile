(ns status-im.contexts.wallet.send.routes.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- find-affordable-networks
  [{:keys [balances-per-chain]} input-value selected-networks]
  (->> balances-per-chain
       (filter (fn [[_ {:keys [balance chain-id]}]]
                 (and
                  (>= (js/parseFloat balance) input-value)
                  (some #(= % chain-id) selected-networks))))
       (map first)))

(defn- make-network-item
  [{:keys [network-name chain-id] :as _network}
   {:keys [title color on-change network-preferences] :as _options}]
  {:title        (or title (string/capitalize (name network-name)))
   :image        :icon-avatar
   :image-props  {:icon (resources/get-network network-name)
                  :size :size-20}
   :action       :selector
   :action-props {:type                :checkbox
                  :customization-color color
                  :checked?            (some #(= % chain-id) @network-preferences)
                  :on-change           on-change}})

(defn networks-drawer
  [{:keys [fetch-routes theme]}]
  (let [network-details     (rf/sub [:wallet/network-details])
        {:keys [color]}     (rf/sub [:wallet/current-viewing-account])
        selected-networks   (rf/sub [:wallet/wallet-send-selected-networks])
        prefix              (rf/sub [:wallet/wallet-send-address-prefix])
        prefix-seq          (string/split prefix #":")
        grouped-details     (group-by #(contains? (set prefix-seq) (:short-name %)) network-details)
        preferred           (get grouped-details true [])
        not-preferred       (get grouped-details false [])
        network-preferences (reagent/atom selected-networks)
        toggle-network      (fn [{:keys [chain-id]}]
                              (swap! network-preferences
                                (fn [preferences]
                                  (if (some #(= % chain-id) preferences)
                                    (vec (remove #(= % chain-id) preferences))
                                    (conj preferences chain-id)))))]
    (fn []
      [rn/view
       [quo/drawer-top {:title (i18n/label :t/edit-receiver-networks)}]
       [quo/category
        {:list-type :settings
         :label     (i18n/label :t/preferred-by-receiver)
         :data      (mapv (fn [network]
                            (make-network-item network
                                               {:color               color
                                                :network-preferences network-preferences
                                                :on-change           #(toggle-network network)}))
                          preferred)}]
       (when (pos? (count not-preferred))
         [quo/category
          {:list-type :settings
           :label     (i18n/label :t/not-preferred-by-receiver)
           :data      (mapv (fn [network]
                              (make-network-item network
                                                 {:color               color
                                                  :network-preferences network-preferences
                                                  :on-change           #(toggle-network network)}))
                            not-preferred)}])
       (when (not= selected-networks @network-preferences)
         [rn/view {:style (style/warning-container color theme)}
          [quo/icon :i/info {:color (colors/resolve-color color theme)}]
          [quo/text
           {:size  :paragraph-2
            :style style/warning-text} (i18n/label :t/receiver-networks-warning)]])
       [quo/bottom-actions
        {:button-one-label (i18n/label :t/apply-changes)
         :button-one-props {:disabled?           (= selected-networks @network-preferences)
                            :on-press            (fn []
                                                   (rf/dispatch [:wallet/update-receiver-networks
                                                                 @network-preferences])
                                                   (rf/dispatch [:hide-bottom-sheet])
                                                   (fetch-routes))
                            :customization-color color}}]])))

(defn route-item
  [{:keys [amount from-network to-network status theme fetch-routes]}]
  (if (= status :add)
    [quo/network-bridge
     {:status          :add
      :container-style style/add-network
      :on-press        #(rf/dispatch [:show-bottom-sheet
                                      {:content (fn [] [networks-drawer
                                                        {:theme        theme
                                                         :fetch-routes fetch-routes}])}])}]
    [rn/view {:style style/routes-inner-container}
     [quo/network-bridge
      {:amount  amount
       :network from-network
       :status  status}]
     (if (= status :default)
       [quo/network-link
        {:shape           :linear
         :source          from-network
         :destination     to-network
         :container-style style/network-link}]
       [rn/view {:style {:width 73}}])
     [quo/network-bridge
      {:amount          amount
       :network         to-network
       :status          status
       :container-style {:right 12}}]]))

(defn- view-internal
  [{:keys [amount routes token input-value theme fetch-routes]}]
  (let [selected-networks         (rf/sub [:wallet/wallet-send-selected-networks])
        loading-networks          (find-affordable-networks token input-value selected-networks)
        loading-suggested-routes? (rf/sub [:wallet/wallet-send-loading-suggested-routes?])
        data                      (if loading-suggested-routes? loading-networks routes)]
    (if (or (and (not-empty loading-networks) loading-suggested-routes?) (not-empty routes))
      [rn/flat-list
       {:data                    (if (and (< (count data) 3) (pos? (count data)))
                                   (concat data [{:status :add}])
                                   data)
        :content-container-style style/routes-container
        :header                  [rn/view {:style style/routes-header-container}
                                  [quo/section-label
                                   {:section         (i18n/label :t/from-label)
                                    :container-style (style/section-label 0)}]
                                  [quo/section-label
                                   {:section         (i18n/label :t/to-label)
                                    :container-style (style/section-label 64)}]]
        :render-fn               (fn [item]
                                   [route-item
                                    {:amount       amount
                                     :theme        theme
                                     :fetch-routes fetch-routes
                                     :status       (cond
                                                     (= (:status item) :add)   :add
                                                     loading-suggested-routes? :loading
                                                     :else                     :default)
                                     :from-network (if loading-suggested-routes?
                                                     (utils/id->network item)
                                                     (utils/id->network (get-in item [:from :chain-id])))
                                     :to-network   (if loading-suggested-routes?
                                                     (utils/id->network item)
                                                     (utils/id->network (get-in item
                                                                                [:to :chain-id])))}])}]
      [rn/view {:style style/empty-container}
       (when (and (not (nil? routes)) (not loading-suggested-routes?))
         [quo/text (i18n/label :t/no-routes-found)])])))

(def view (quo.theme/with-theme view-internal))
