(ns status-im.contexts.shell.share.wallet.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.share :as share]
    [reagent.core :as reagent]
    [status-im.contexts.shell.share.style :as style]
    [status-im.contexts.wallet.common.sheets.network-preferences.view :as network-preferences]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(def qr-size 500)

(defn- share-action
  [address share-title]
  (share/open
   (if platform/ios?
     {:activityItemSources [{:placeholderItem {:type    "text"
                                               :content address}
                             :item            {:default {:type "text"
                                                         :content
                                                         address}}
                             :linkMetadata    {:title share-title}}]}
     {:title     share-title
      :subject   share-title
      :message   address
      :isNewTask true})))

(defn- open-preferences
  [selected-networks]
  (rf/dispatch [:show-bottom-sheet
                {:theme :dark
                 :shell? true
                 :content
                 (fn []
                   [network-preferences/view
                    {:blur?             true
                     :selected-networks (set selected-networks)
                     :on-save           (fn [chain-ids]
                                          (rf/dispatch [:hide-bottom-sheet])
                                          (reset! selected-networks (map #(get utils/id->network %)
                                                                         chain-ids)))}])}]))
(defn- wallet-qr-code-item
  [account width index]
  (let [selected-networks (reagent/atom [:ethereum :optimism :arbitrum])
        wallet-type       (reagent/atom :wallet-legacy)]
    (fn []
      (let [share-title         (str (.-name account) " " (i18n/label :t/address))
            qr-url              (utils/get-wallet-qr {:wallet-type       @wallet-type
                                                      :selected-networks @selected-networks
                                                      :address           (.-address account)})
            qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                                 {:url         qr-url
                                  :port        (rf/sub [:mediaserver/port])
                                  :qr-size     qr-size
                                  :error-level :highest})]
        [rn/view {:style {:width width :margin-left (if (zero? index) 0 -30)}}
         [rn/view {:style style/qr-code-container}
          [quo/share-qr-code
           {:type                @wallet-type
            :qr-image-uri        qr-media-server-uri
            :qr-data             qr-url
            :networks            @selected-networks
            :on-share-press      #(share-action qr-url share-title)
            :profile-picture     nil
<<<<<<< HEAD
            :full-name           (:name account)
            :customization-color (:color account)
            :emoji               (:emoji account)
=======
            :unblur-on-android?  true
            :full-name           (.-name account)
            :customization-color (.-color account)
            :emoji               (.-emoji account)
>>>>>>> e183f3647 (use memo to prevent re render)
            :on-multichain-press #(reset! wallet-type :wallet-multichain)
            :on-legacy-press     #(reset! wallet-type :wallet-legacy)
            :on-settings-press   #(open-preferences @selected-networks)}]]]))))

(defn- indicator
  [is-active]
  [rn/view
   {:style {:width           8
            :height          8
            :borderRadius    4
            :backgroundColor :white
            :opacity         (if is-active 1.0 0.5)}}])

(defn- indicator-list
  [n current-index]
  [rn/view
   {:style {:display         :flex
            :flex-direction  :row
            :align-items     :center
            :justify-content :center
            :gap             8}}
   (for [i (range n)]
     ^{:key i} [indicator (= current-index i)])])

(def render-item
  (reagent/adapt-react-class
   (rn/memo
    (fn [item]
      (print item)
      (let [width (rf/sub [:dimensions/window-width])]
        (reagent/as-element
         [wallet-qr-code-item item width (.-position item)]))))))

(defn wallet-tab
  []
  (let [accounts      (rf/sub [:wallet/accounts])
        width         (rf/sub [:dimensions/window-width])
        current-index (reagent/atom 0)]
    (fn []
      [rn/view
       [rn/flat-list
        {:horizontal                        true
         :deceleration-rate                 0.9
         :snap-to-alignment                 "start"
         :snap-to-interval                  (- width 30)
         :disable-interval-momentum         true
         :scroll-event-throttle             64
         :data                              accounts
         :directional-lock-enabled          true
         :shows-horizontal-scroll-indicator false
         :on-scroll                         (fn [e]
                                              (reset! current-index (js/Math.ceil
                                                                     (/ e.nativeEvent.contentOffset.x
                                                                        width))))
         :render-fn                         render-item}]
       [rn/view
        {:style {:margin-top 20}}
        (indicator-list (count accounts) @current-index)]])))
