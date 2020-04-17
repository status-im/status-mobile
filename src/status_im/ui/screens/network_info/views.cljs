(ns status-im.ui.screens.network-info.views
  (:require [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.components.styles :as components.styles]
            [reagent.core :as reagent]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as time]))

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
  (let [latest-block (reagent/atom nil)
        last-loaded-block (reagent/atom nil)
        on-press
        (fn []
          (get-block
           "latest"
           (fn [res]
             (reset! latest-block res)
             (get-block
              (str "0x" (abi-spec/number-to-hex
                         (last-loaded-block-number)))
              (fn [res]
                (reset! last-loaded-block res))))))]
    (fn []
      [react/view
       {:style {:flex              1
                :margin-horizontal 16}}
       (if-not @latest-block
         [react/text
          {:on-press on-press}
          (i18n/label :t/network-info-press-to-refresh)]
         [react/text
          {:on-press on-press}
          (let [latest-block-number
                (decode/uint (:number @latest-block))

                latest-block-timestamp
                (decode/uint (:timestamp @latest-block))

                last-loaded-block-number
                (decode/uint (:number @last-loaded-block))

                last-loaded-block-timestamp
                (decode/uint (:timestamp @last-loaded-block))]
            (str (i18n/label :t/network-info-latest-block-number)
                 latest-block-number
                 "\n"
                 (i18n/label :t/network-info-latest-block-time)
                 (to-date latest-block-timestamp)
                 "\n"
                 (i18n/label :t/network-info-last-loaded-block)
                 last-loaded-block-number
                 "\n"
                 (i18n/label :t/network-info-last-loaded-block-time)
                 (to-date last-loaded-block-timestamp)
                 "\n"
                 (i18n/label :t/network-info-seconds-diff) (- latest-block-timestamp
                                                              last-loaded-block-timestamp)
                 "\n"
                 (i18n/label :t/network-info-blocks-diff) (- latest-block-number
                                                             last-loaded-block-number)
                 "\n"
                 (i18n/label :t/network-info-press-to-refresh)))])])))

(defn network-info []
  [react/view components.styles/flex
   [topbar/topbar
    {:title :t/network-info}]
   [check-lag]])
