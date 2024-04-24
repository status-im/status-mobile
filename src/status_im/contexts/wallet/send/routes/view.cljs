(ns status-im.contexts.wallet.send.routes.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.send :as send-utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.vector :as vector-utils]))

(def ^:private network-priority-score
  {:ethereum 1
   :optimism 2
   :arbitrum 3})

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

(defn- find-network-link-insertion-index
  [network-links chain-id loading-suggested-routes?]
  (let [network                              (utils/id->network chain-id)
        inserted-network-link-priority-score (network-priority-score network)]
    (or (->> network-links
             (keep-indexed (fn [idx network-link]
                             (let [network-link (utils/id->network (if loading-suggested-routes?
                                                                     network-link
                                                                     (get-in network-link
                                                                             [:from :chain-id])))]
                               (when (> (network-priority-score network-link)
                                        inserted-network-link-priority-score)
                                 idx))))
             first)
        (count network-links))))

(defn- add-disabled-networks
  [network-links disabled-from-networks loading-suggested-routes?]
  (let [sorted-networks (sort-by (comp network-priority-score utils/id->network) disabled-from-networks)]
    (reduce (fn [acc-network-links chain-id]
              (let [index                 (find-network-link-insertion-index acc-network-links
                                                                             chain-id
                                                                             loading-suggested-routes?)
                    disabled-network-link {:status   :disabled
                                           :chain-id chain-id
                                           :network  (utils/id->network chain-id)}]
                (vector-utils/insert-element-at acc-network-links disabled-network-link index)))
            network-links
            sorted-networks)))

(defn networks-drawer
  [{:keys [fetch-routes theme]}]
  (let [network-details     (rf/sub [:wallet/network-details])
        {:keys [color]}     (rf/sub [:wallet/current-viewing-account])
        selected-networks   (rf/sub [:wallet/wallet-send-receiver-networks])
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
  [{:keys [first-item? from-amount to-amount token-symbol from-chain-id to-chain-id from-network
           to-network on-press-from-network on-press-to-network status theme fetch-routes disabled?
           loading?]}]
  (if (= status :add)
    [quo/network-bridge
     {:status          :add
      :container-style style/add-network
      :on-press        #(rf/dispatch [:show-bottom-sheet
                                      {:content (fn [] [networks-drawer
                                                        {:theme        theme
                                                         :fetch-routes fetch-routes}])}])}]
    [rn/view {:style (style/routes-inner-container first-item?)}
     [quo/network-bridge
      {:amount   (str from-amount " " token-symbol)
       :network  from-network
       :status   status
       :on-press #(when (and on-press-from-network (not loading?))
                    (on-press-from-network from-chain-id from-amount))}]
     (if (= status :default)
       [quo/network-link
        {:shape           :linear
         :source          from-network
         :destination     to-network
         :container-style style/network-link}]
       [rn/view {:style {:flex 1}}])
     [quo/network-bridge
      {:amount   (str to-amount " " token-symbol)
       :network  to-network
       :status   (cond
                   (and disabled? loading?)       :loading
                   (and disabled? (not loading?)) :default
                   :else                          status)
       :on-press #(when (and on-press-to-network (not loading?))
                    (on-press-to-network to-chain-id to-amount))}]]))

(defn- render-network-link
  [item index _
   {:keys [from-values-by-chain to-values-by-chain theme fetch-routes on-press-from-network
           on-press-to-network token-symbol loading-suggested-routes?]}]
  (let [first-item?       (zero? index)
        disabled-network? (= (:status item) :disabled)
        from-chain-id     (get-in item [:from :chain-id])
        to-chain-id       (get-in item [:to :chain-id])
        from-amount       (when from-chain-id
                            (from-values-by-chain from-chain-id))
        to-amount         (when to-chain-id
                            (to-values-by-chain to-chain-id))]
    [route-item
     {:first-item?           first-item?
      :from-amount           (if disabled-network? 0 from-amount)
      :to-amount             (if disabled-network? 0 to-amount)
      :token-symbol          token-symbol
      :disabled?             disabled-network?
      :loading?              loading-suggested-routes?
      :theme                 theme
      :fetch-routes          fetch-routes
      :status                (cond
                               (= (:status item) :add)      :add
                               (= (:status item) :disabled) :disabled
                               loading-suggested-routes?    :loading
                               :else                        :default)
      :from-chain-id         (or from-chain-id (:chain-id item))
      :to-chain-id           (or to-chain-id (:chain-id item))
      :from-network          (cond (and loading-suggested-routes?
                                        (not disabled-network?))
                                   (utils/id->network item)
                                   disabled-network?
                                   (utils/id->network (:chain-id
                                                       item))
                                   :else
                                   (utils/id->network from-chain-id))
      :to-network            (cond (and loading-suggested-routes?
                                        (not disabled-network?))
                                   (utils/id->network item)
                                   disabled-network?
                                   (utils/id->network (:chain-id
                                                       item))
                                   :else
                                   (utils/id->network to-chain-id))
      :on-press-from-network on-press-from-network
      :on-press-to-network   on-press-to-network}]))

(defn fetch-routes
  [amount routes-can-be-fetched? bounce-duration-ms]
  (if routes-can-be-fetched?
    (debounce/debounce-and-dispatch
     [:wallet/get-suggested-routes {:amount amount}]
     bounce-duration-ms)
    (rf/dispatch [:wallet/clean-suggested-routes])))

(defn view
  [{:keys [token theme input-value routes-can-be-fetched?
           on-press-to-network]}]

  (let [token-symbol                                   (:symbol token)
        loading-suggested-routes?                      (rf/sub
                                                        [:wallet/wallet-send-loading-suggested-routes?])
        from-values-by-chain                           (rf/sub
                                                        [:wallet/wallet-send-from-values-by-chain])
        to-values-by-chain                             (rf/sub [:wallet/wallet-send-to-values-by-chain])
        suggested-routes                               (rf/sub [:wallet/wallet-send-suggested-routes])
        selected-networks                              (rf/sub [:wallet/wallet-send-receiver-networks])
        disabled-from-chain-ids                        (rf/sub
                                                        [:wallet/wallet-send-disabled-from-chain-ids])
        routes                                         (when suggested-routes
                                                         (or (:best suggested-routes) []))
        {token-balances-per-chain :balances-per-chain} (rf/sub
                                                        [:wallet/current-viewing-account-tokens-filtered
                                                         (str token-symbol)])
        affordable-networks                            (send-utils/find-affordable-networks
                                                        {:balances-per-chain token-balances-per-chain
                                                         :input-value        input-value
                                                         :selected-networks  selected-networks
                                                         :disabled-chain-ids disabled-from-chain-ids})
        network-links                                  (if loading-suggested-routes?
                                                         affordable-networks
                                                         routes)
        show-routes?                                   (or (and (not-empty affordable-networks)
                                                                loading-suggested-routes?)
                                                           (not-empty routes))]

    (rn/use-effect
     #(when (> (count affordable-networks) 0)
        (fetch-routes input-value routes-can-be-fetched? 2000))
     [input-value routes-can-be-fetched?])
    (rn/use-effect
     #(when (> (count affordable-networks) 0)
        (fetch-routes input-value routes-can-be-fetched? 0))
     [disabled-from-chain-ids])
    (if show-routes?
      (let [initial-network-links-count   (count network-links)
            disabled-count                (count disabled-from-chain-ids)
            network-links                 (if (not-empty disabled-from-chain-ids)
                                            (add-disabled-networks network-links
                                                                   disabled-from-chain-ids
                                                                   loading-suggested-routes?)
                                            network-links)
            network-links-with-add-button (if (and (< (- (count network-links) disabled-count)
                                                      constants/default-network-count)
                                                   (pos? initial-network-links-count))
                                            (concat network-links [{:status :add}])
                                            network-links)]
        [rn/flat-list
         {:data network-links-with-add-button
          :content-container-style style/routes-container
          :header [rn/view {:style style/routes-header-container}
                   [quo/section-label
                    {:section         (i18n/label :t/from-label)
                     :container-style style/section-label-left}]
                   [quo/section-label
                    {:section         (i18n/label :t/to-label)
                     :container-style style/section-label-right}]]
          :render-data
          {:from-values-by-chain      from-values-by-chain
           :to-values-by-chain        to-values-by-chain
           :theme                     theme
           :fetch-routes              #(fetch-routes % routes-can-be-fetched? 2000)
           :on-press-from-network     (fn [chain-id _]
                                        (let [disabled-chain-ids (if (contains? (set
                                                                                 disabled-from-chain-ids)
                                                                                chain-id)
                                                                   (vec (remove #(= % chain-id)
                                                                                disabled-from-chain-ids))
                                                                   (conj disabled-from-chain-ids
                                                                         chain-id))
                                              re-enabling-chain? (< (count disabled-chain-ids)
                                                                    (count disabled-from-chain-ids))]
                                          (when (or re-enabling-chain?
                                                    (> (count affordable-networks) 1))
                                            (rf/dispatch [:wallet/disable-from-networks
                                                          disabled-chain-ids]))))
           :on-press-to-network       on-press-to-network
           :token-symbol              token-symbol
           :loading-suggested-routes? loading-suggested-routes?}
          :render-fn render-network-link}])
      [rn/view {:style style/empty-container}
       (when (and (not (nil? routes)) (not loading-suggested-routes?))
         [quo/text (i18n/label :t/no-routes-found)])])))

