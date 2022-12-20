(ns status-im.ui.screens.network-info.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.ethereum.decode :as decode]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.native-module.core :as status]
            [status-im.ui.components.react :as react]
            [status-im.utils.datetime :as time]))

(defn get-block
  [block callback]
  (json-rpc/call
   {:method     "eth_getBlockByNumber"
    :params     [block false]
    :on-success callback
    :on-error   #(js/alert (str "can't fetch latest block" %))}))

(defn last-loaded-block-number
  []
  @(re-frame/subscribe [:ethereum/current-block]))

(defn to-date
  [timestamp]
  (time/timestamp->long-date
   (* 1000 timestamp)))

(defn check-lag
  []
  (let [latest-block      (reagent/atom nil)
        last-loaded-block (reagent/atom nil)
        on-press
        (fn []
          (get-block
           "latest"
           (fn [res]
             (reset! latest-block res)
             (get-block
              (str "0x"
                   (status/number-to-hex
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
          "PRESS TO REFRESH"]
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
            (str "Latest block number: "
                 latest-block-number
                 "\n"
                 "Latest block time: "
                 (to-date latest-block-timestamp)
                 "\n"
                 "Last loaded block: "
                 last-loaded-block-number
                 "\n"
                 "Last loaded block time: "
                 (to-date last-loaded-block-timestamp)
                 "\n"
                 "Seconds diff: "
                 (- latest-block-timestamp
                    last-loaded-block-timestamp)
                 "\n"
                 "Blocks diff: " (- latest-block-number
                                    last-loaded-block-number)
                 "\n"
                 "PRESS TO REFRESH"))])])))

(defn network-info
  []
  [check-lag])
