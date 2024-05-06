(ns status-im.contexts.wallet.send.routes.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.send.routes.style :as style]
    [status-im.contexts.wallet.send.utils :as send-utils]
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def row-height 44)
(def space-between-rows 11)
(def network-link-linear-height 10)
(def network-link-1x-height 56)
(def network-link-2x-height 111)

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

(defn fetch-routes
  [amount valid-input? bounce-duration-ms]
  (if valid-input?
    (debounce/debounce-and-dispatch
     [:wallet/get-suggested-routes {:amount amount}]
     bounce-duration-ms)
    (rf/dispatch [:wallet/clean-suggested-routes])))

(defn networks-drawer
  [{:keys [on-save theme]}]
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
        {:actions          :one-action
         :button-one-label (i18n/label :t/apply-changes)
         :button-one-props {:disabled?           (or (= selected-networks @network-preferences)
                                                     (empty? @network-preferences))
                            :on-press            (fn []
                                                   (rf/dispatch [:wallet/update-receiver-networks
                                                                 @network-preferences])
                                                   (rf/dispatch [:hide-bottom-sheet])
                                                   (on-save))
                            :customization-color color}}]])))

(defn render-network-values
  [{:keys [network-values token-symbol on-press theme on-save to? loading-suggested-routes?]}]
  [rn/view
   (map-indexed (fn [index {:keys [chain-id total-amount type]}]
                  [rn/view
                   {:key   (str (if to? "to" "from") "-" chain-id)
                    :style {:margin-top (if (pos? index) 11 7.5)}}
                   [quo/network-bridge
                    {:amount   (str total-amount " " token-symbol)
                     :network  (network-utils/id->network chain-id)
                     :status   type
                     :on-press #(when (not loading-suggested-routes?)
                                  (cond
                                    (= type :add)
                                    (rf/dispatch [:show-bottom-sheet
                                                  {:content (fn []
                                                              [networks-drawer
                                                               {:theme   theme
                                                                :on-save on-save}])}])
                                    on-press (on-press chain-id total-amount)))}]])
                network-values)])

(defn render-network-links
  [{:keys [network-links sender-network-values]}]
  [rn/view {:style style/network-links-container}
   (map
    (fn [{:keys [from-chain-id to-chain-id position-diff]}]
      (let [position-diff-absolute (js/Math.abs position-diff)
            shape                  (case position-diff-absolute
                                     0 :linear
                                     1 :1x
                                     2 :2x)
            height                 (case position-diff-absolute
                                     0 network-link-linear-height
                                     1 network-link-1x-height
                                     2 network-link-2x-height)
            inverted?              (neg? position-diff)
            source                 (network-utils/id->network from-chain-id)
            destination            (network-utils/id->network to-chain-id)
            from-chain-id-index    (first (keep-indexed #(when (= from-chain-id (:chain-id %2)) %1)
                                                        sender-network-values))
            base-margin-top        (* (+ row-height space-between-rows)
                                      from-chain-id-index)
            margin-top             (if (zero? position-diff)
                                     (+ base-margin-top
                                        (- (/ row-height 2) (/ height 2)))
                                     (+ base-margin-top
                                        (- (/ row-height 2) height)
                                        (if inverted? height 0)))]
        [rn/view
         {:key   (str "from-" from-chain-id "-to-" to-chain-id)
          :style (style/network-link-container margin-top inverted?)}
         [rn/view {:style {:flex 1}}
          [quo/network-link
           {:shape       shape
            :source      source
            :destination destination}]]]))
    network-links)])

(defn disable-chain
  [chain-id disabled-from-chain-ids token-available-networks-for-suggested-routes]
  (let [disabled-chain-ids
        (if (contains? (set
                        disabled-from-chain-ids)
                       chain-id)
          (vec (remove #(= % chain-id)
                       disabled-from-chain-ids))
          (conj disabled-from-chain-ids
                chain-id))
        re-enabling-chain?
        (< (count disabled-chain-ids)
           (count disabled-from-chain-ids))]
    (if (or re-enabling-chain?
            (> (count token-available-networks-for-suggested-routes) 1))
      (rf/dispatch [:wallet/disable-from-networks
                    disabled-chain-ids])
      (rf/dispatch [:toasts/upsert
                    {:id   :disable-chain-error
                     :type :negative
                     :text (i18n/label :t/at-least-one-network-must-be-activated)}]))))

(defn view
  [{:keys [token theme input-value valid-input?
           on-press-to-network current-screen-id]}]
  (let [token-symbol (:symbol token)
        nav-current-screen-id (rf/sub [:view-id])
        active-screen? (= nav-current-screen-id current-screen-id)
        loading-suggested-routes? (rf/sub
                                   [:wallet/wallet-send-loading-suggested-routes?])
        sender-network-values (rf/sub
                               [:wallet/wallet-send-sender-network-values])
        receiver-network-values (rf/sub
                                 [:wallet/wallet-send-receiver-network-values])
        network-links (rf/sub [:wallet/wallet-send-network-links])
        disabled-from-chain-ids (rf/sub
                                 [:wallet/wallet-send-disabled-from-chain-ids])
        {token-balances-per-chain :balances-per-chain} (rf/sub
                                                        [:wallet/current-viewing-account-tokens-filtered
                                                         (str token-symbol)])
        token-available-networks-for-suggested-routes
        (send-utils/token-available-networks-for-suggested-routes
         {:balances-per-chain token-balances-per-chain
          :disabled-chain-ids disabled-from-chain-ids})
        show-routes? (not-empty sender-network-values)]
    (rn/use-effect
     #(when (and active-screen? (> (count token-available-networks-for-suggested-routes) 0))
        (fetch-routes input-value valid-input? 2000))
     [input-value valid-input?])
    (rn/use-effect
     #(when (and active-screen? (> (count token-available-networks-for-suggested-routes) 0))
        (fetch-routes input-value valid-input? 0))
     [disabled-from-chain-ids])
    [rn/scroll-view {:content-container-style style/routes-container}
     (when show-routes?
       [rn/view {:style style/routes-header-container}
        [quo/section-label
         {:section         (i18n/label :t/from-label)
          :container-style style/section-label-left}]
        [quo/section-label
         {:section         (i18n/label :t/to-label)
          :container-style style/section-label-right}]])
     [rn/view {:style style/routes-inner-container}
      [render-network-values
       {:token-symbol              token-symbol
        :network-values            sender-network-values
        :on-press                  #(disable-chain %1
                                                   disabled-from-chain-ids
                                                   token-available-networks-for-suggested-routes)
        :to?                       false
        :theme                     theme
        :loading-suggested-routes? loading-suggested-routes?}]
      [render-network-links
       {:network-links         network-links
        :sender-network-values sender-network-values}]
      [render-network-values
       {:token-symbol              token-symbol
        :network-values            receiver-network-values
        :on-press                  on-press-to-network
        :to?                       true
        :loading-suggested-routes? loading-suggested-routes?
        :theme                     theme
        :on-save                   #(fetch-routes input-value valid-input? 0)}]]]))

