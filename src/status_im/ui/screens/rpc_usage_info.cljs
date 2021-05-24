(ns status-im.ui.screens.rpc-usage-info
  (:require [status-im.ui.components.react :as react]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo.core]
            [quo.react-native :as quo.react-native]
            [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [taoensso.timbre :as log]
            [clojure.string :as clojure.string]))

(re-frame/reg-sub :rpc-usage/raw-data (fn [db] (get db :rpc-usage/data)))
(re-frame/reg-sub :rpc-usage/filter (fn [db] (get db :rpc-usage/filter)))

(re-frame/reg-sub
 :rpc-usage/data
 :<- [:rpc-usage/raw-data]
 :<- [:rpc-usage/filter]
 (fn [[{:keys [total methods]} method-filter]]
   (let [data
         (->> methods
              (map (fn [[k v]]
                     [(name k) v]))
              (filter (fn [[k]]
                        (clojure.string/includes? k method-filter)))
              (sort-by second >))
         filtered-total (reduce + (map second data))]
     {:stats          data
      :filtered-total filtered-total
      :total          total})))

(re-frame/reg-fx
 ::get-stats
 (fn []
   (status-im.ethereum.json-rpc/call
    {:method "rpcstats_getStats"
     :params []
     :on-success #(re-frame/dispatch [::handle-stats %])})))

(re-frame/reg-fx
 ::reset
 (fn []
   (status-im.ethereum.json-rpc/call
    {:method "rpcstats_reset"
     :params []
     :on-success #(log/debug "rpcstats_reset success")})))

(fx/defn handle-stats
  {:events [::handle-stats]}
  [{:keys [db]} data]
  {:db (assoc db :rpc-usage/data data)})

(fx/defn get-stats
  {:events [::get-stats]}
  [{:keys [db]}]
  (let [method-filter (get db :rpc-usage/filter "eth_")]
    {:db         (assoc db :rpc-usage/filter method-filter)
     ::get-stats nil}))

(fx/defn reset
  {:events [::reset]}
  [{:keys [db]}]
  {:db     (dissoc db :rpc-usage/data)
   ::reset nil})

(fx/defn copy
  {:events [::copy]}
  [{:keys [db]}]
  {:db     (dissoc db :rpc-usage/data)
   ::reset nil})

(fx/defn set-filter
  {:events [::set-filter]}
  [{:keys [db]} method-filter]
  {:db (assoc db :rpc-usage/filter method-filter)})

(defn stats-table [{:keys [total filtered-total stats]}]
  [quo.react-native/scroll-view
   {:style {:padding-horizontal 8}}
   [quo.react-native/view
    {:style {:flex-direction :row
             :justify-content :space-between}}
    [quo.core/text "TOTAL"]
    [quo.core/text (str filtered-total " of " total)]]
   (when (seq stats)
     (for [[k v] stats]
       ^{:key (str k v)}
       [quo.react-native/view
        {:style {:flex-direction :row
                 :justify-content :space-between}}
        [quo.core/text k]
        [quo.core/text v]]))])

(defn prepare-stats [{:keys [stats]}]
  (clojure.string/join
   "\n"
   (map (fn [[k v]]
          (str k " " v))
        stats)))

(defn usage-info []
  (let [stats @(re-frame/subscribe [:rpc-usage/data])
        methods-filter @(re-frame/subscribe [:rpc-usage/filter])]
    [react/view {:flex 1
                 :margin-horizontal 8}
     [quo.react-native/view
      {:style {:flex-direction  :row
               :margin-top      8
               :justify-content :space-between}}
      [quo.core/button
       {:on-press            #(re-frame/dispatch [::get-stats])
        :accessibility-label :rpc-usage-get-stats}
       (i18n/label :t/rpc-usage-get-stats)]
      [quo.core/button
       {:on-press            #(re-frame/dispatch [::reset])
        :accessibility-label :rpc-usage-reset}
       (i18n/label :t/rpc-usage-reset)]
      [quo.core/button
       {:on-press
        #(react/copy-to-clipboard (prepare-stats stats))
        :accessibility-label :rpc-usage-copy}
       (i18n/label :t/rpc-usage-copy)]]
     [quo.core/text-input
      {:on-change-text  #(re-frame/dispatch [::set-filter %])
       :label           (i18n/label :t/rpc-usage-filter)
       :default-value   methods-filter
       :auto-capitalize :none
       :show-cancel     false
       :auto-focus      false}]
     [stats-table stats]]))


