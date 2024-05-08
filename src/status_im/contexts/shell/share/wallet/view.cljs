(ns status-im.contexts.shell.share.wallet.view
  (:require
    [oops.core :as oops]
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [reagent.core :as reagent]
    [status-im.constants :as constants]
    [status-im.contexts.shell.share.style :as style]
    [status-im.contexts.shell.share.wallet.style :as wallet-style]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.common.utils.networks :as network-utils]
    [status-im.contexts.wallet.sheets.network-preferences.view :as network-preferences]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.number]
    [utils.re-frame :as rf]))

(def qr-size 500)

(defn- share-action
  [address share-title]
  (rf/dispatch [:open-share
                {:options (if platform/ios?
                            {:activityItemSources [{:placeholderItem {:type    :text
                                                                      :content address}
                                                    :item            {:default {:type :text
                                                                                :content
                                                                                address}}
                                                    :linkMetadata    {:title share-title}}]}
                            {:title     share-title
                             :subject   share-title
                             :message   address
                             :isNewTask true})}]))

(defn- open-preferences
  [selected-networks account]
  (rf/dispatch
   [:show-bottom-sheet
    {:theme   :dark
     :shell?  true
     :content (fn []
                [network-preferences/view
                 {:blur?             true
                  :selected-networks (set @selected-networks)
                  :account           account
                  :button-label      (i18n/label :t/display)
                  :on-save           (fn [chain-ids]
                                       (rf/dispatch [:hide-bottom-sheet])
                                       (reset! selected-networks (map #(get network-utils/id->network %)
                                                                      chain-ids)))}])}]))

(defn- wallet-qr-code-item
  [{:keys [account index]}]
  (let [{window-width :width} (rn/get-window)
        selected-networks     (reagent/atom constants/default-network-names)
        wallet-type           (reagent/atom :multichain)
        on-settings-press     #(open-preferences selected-networks account)
        on-legacy-press       #(reset! wallet-type :legacy)
        on-multichain-press   #(reset! wallet-type :multichain)]
    (fn []
      (let [share-title         (str (:name account) " " (i18n/label :t/address))
            qr-url              (utils/get-wallet-qr {:wallet-type       @wallet-type
                                                      :selected-networks @selected-networks
                                                      :address           (:address account)})
            qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         qr-url
                                  :port        (rf/sub [:mediaserver/port])
                                  :qr-size     qr-size
                                  :error-level :highest})]
        [rn/view {:style {:width window-width :margin-left (if (zero? index) 0 -30)}}
         [rn/view {:style style/qr-code-container}
          [quo/share-qr-code
           {:type                :wallet
            :width               (- window-width (* style/screen-padding 2))
            :address             @wallet-type
            :qr-image-uri        qr-media-server-uri
            :qr-data             qr-url
            :networks            @selected-networks
            :on-share-press      #(share-action qr-url share-title)
            :profile-picture     nil
            :full-name           (:name account)
            :customization-color (:color account)
            :emoji               (:emoji account)
            :on-multichain-press on-multichain-press
            :on-legacy-press     on-legacy-press
            :on-settings-press   on-settings-press}]]]))))

(defn- indicator
  [active?]
  [rn/view {:style (wallet-style/indicator-wrapper-style active?)}])

(defn- indicator-list
  [num-indicators current-index]
  [rn/view {:style wallet-style/indicator-list-style}
   (for [i (range num-indicators)]
     ^{:key i} [indicator (= current-index i)])])

(defn render-item
  [item]
  [wallet-qr-code-item
   {:account item
    :index   (:position item)}])

(defn- qr-code-visualized-index
  [offset qr-code-size num-qr-codes]
  (-> (+ (/ offset qr-code-size) 0.5)
      (int)
      (utils.number/value-in-range 0 (dec num-qr-codes))))

(defn wallet-tab
  []
  (let [current-index         (reagent/atom 0)
        {window-width :width} (rn/get-window)
        qr-code-size          (- window-width 30)]
    (fn []
      (let [accounts     (rf/sub [:wallet/accounts])
            num-accounts (count accounts)
            on-scroll    (rn/use-callback
                          (fn [e]
                            (let [offset-x (oops/oget e "nativeEvent.contentOffset.x")
                                  index    (qr-code-visualized-index offset-x qr-code-size num-accounts)]
                              (reset! current-index index))))]
        [rn/view
         [rn/flat-list
          {:horizontal                        true
           :deceleration-rate                 0.9
           :snap-to-alignment                 :start
           :snap-to-interval                  qr-code-size
           :disable-interval-momentum         true
           :scroll-event-throttle             64
           :data                              accounts
           :directional-lock-enabled          true
           :shows-horizontal-scroll-indicator false
           :on-scroll                         on-scroll
           :render-fn                         render-item}]
         (when (> num-accounts 1)
           [rn/view {:style {:margin-top 20}}
            [indicator-list num-accounts @current-index]])]))))
