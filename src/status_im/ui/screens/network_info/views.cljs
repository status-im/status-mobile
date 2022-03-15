(ns status-im.ui.screens.network-info.views
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.utils.datetime :as time]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [status-im.i18n.i18n :as i18n]
            [status-im.wallet.core :as wallet]))

(defn get-block [block callback]
  (json-rpc/call
   {:method     "eth_getBlockByNumber"
    :params     [block false]
    :on-success callback
    :on-error   #(js/alert (str "can't fetch latest block" %))}))

(defn last-loaded-block-number []
  @(re-frame/subscribe [:ethereum/current-block]))

(defn to-date [timestamp]
  (time/timestamp->long-date
   (* 1000 timestamp)))

(defn check-lag []
  (let [latest-block      (reagent/atom nil)
        last-loaded-block (reagent/atom nil)
        refreshing?       (reagent/atom false)
        refresh           (fn []
                            (reset! refreshing? true)
                            (get-block
                             "latest"
                             (fn [res]
                               (reset! latest-block res)
                               (when-not (last-loaded-block-number)
                                 (re-frame/dispatch-sync [::wallet/request-current-block-update]))
                               (get-block
                                (str "0x" (abi-spec/number-to-hex (last-loaded-block-number)))
                                (fn [res]
                                  (reset! last-loaded-block res)
                                  (reset! refreshing? false))))))]
    (reagent/create-class
     {:display-name ::check-lag
      :component-did-mount
      refresh
      :reagent-render
      (fn []
        [react/view
         {:style {:flex              1
                  :margin-horizontal 16}}
         (let [latest-block-number      (-> @latest-block :number decode/uint)
               last-loaded-block-number (-> @last-loaded-block :number decode/uint)
               latest-block-ts          (-> @latest-block :timestamp decode/uint)
               last-loaded-block-ts     (-> @last-loaded-block :timestamp decode/uint)
               seconds-diff             (- latest-block-ts last-loaded-block-ts)
               blocks-diff              (- latest-block-number last-loaded-block-number)
               data                     [{:id    :latest-block
                                          :value latest-block-number
                                          :ts    (to-date latest-block-ts)
                                          :label "Latest Block"}
                                         {:id    :last-loaded-block
                                          :value last-loaded-block-number
                                          :ts    (to-date last-loaded-block-ts)
                                          :label "Last Loaded Block"}]
               footer-text              (i18n/label :t/network-info-footer {:blocks-diff blocks-diff :seconds-diff seconds-diff})]
           (rn/flat-list {:data       data
                          :onRefresh  refresh
                          :refreshing @refreshing?
                          :footer     [react/text {:style {:margin-top 5 :color colors/gray}} footer-text]
                          :render-fn  (fn [{:keys [value label ts]}]
                                        [quo/list-item
                                         {:title          label
                                          :accessory      :text
                                          :accessory-text value
                                          :subtitle       ts}])}))])})))

(defn network-info []
  [check-lag])
