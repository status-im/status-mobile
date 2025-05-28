(ns status-im.contexts.browser.subs.core
  (:require [re-frame.core :as rf]))

(rf/reg-sub :browser/tab-ids
 (fn [db]
   (get db :browser/tab-ids)))

(rf/reg-sub :browser/tabs-by-id
 (fn [db]
   (get db :browser/tabs-by-id)))

(rf/reg-sub :browser/permissions
 (fn [db]
   (get db :browser/permissions)))

(rf/reg-sub :browser/dapps
 (fn [db]
   (get db :browser/dapps)))

(rf/reg-sub :browser/mode
 (fn [db]
   (get db :browser/mode)))

(rf/reg-sub :browser/focused-tab-id
 (fn [db]
   (let [default (-> db :browser/tab-ids first)]
     (get db :browser/focused-tab-id default))))

(rf/reg-sub :browser/focused-tab
 :<- [:browser/tabs-by-id]
 :<- [:browser/focused-tab-id]
 (fn [[tabs-by-id focused-tab-id]]
   (get tabs-by-id focused-tab-id)))

(rf/reg-sub :browser/focused-tab-idx
 :<- [:browser/focused-tab-id]
 :<- [:browser/tab-ids]
 (fn [[focused-id tab-ids]]
   (let [idx (.indexOf tab-ids focused-id)]
     (when (not= -1 idx) idx))))

(rf/reg-sub :browser/can-focus-next?
 :<- [:browser/focused-tab-idx]
 :<- [:browser/tab-ids]
 (fn [[focused-idx tab-ids]]
   (not= (inc focused-idx) (count tab-ids))))

(rf/reg-sub :browser/can-focus-prev?
 :<- [:browser/focused-tab-idx]
 (fn [focused-idx]
   (not (zero? focused-idx))))

(rf/reg-sub :browser/tab-by-id
 :<- [:browser/tabs-by-id]
 (fn [tabs-by-id [_ tab-id]]
   (get tabs-by-id tab-id)))

(defn- tab-by-id-query
  [[_ tab-id]]
  [(rf/subscribe [:browser/tab-by-id tab-id])])

(rf/reg-sub :browser/tabs
 :<- [:browser/tab-ids]
 :<- [:browser/tabs-by-id]
 (fn [[tab-ids tabs-by-id]]
   (->> tab-ids
        (map (partial get tabs-by-id)))))

(rf/reg-sub :browser/tab-url
 tab-by-id-query
 (fn [[tab-data]]
   (:url tab-data)))

(rf/reg-sub :browser/tab-type
 tab-by-id-query
 (fn [[tab-data]]
   (:type tab-data)))

(rf/reg-sub :browser/tab-type
 tab-by-id-query
 (fn [[tab-data]]
   (:type tab-data)))

(rf/reg-sub :browser/dapp-for-tab
 :<- [:browser/tabs-by-id]
 :<- [:browser/dapps]
 (fn [[tabs dapps] [_ tab-id]]
   (let [dapp-id (get-in tabs [tab-id :dapp-id])
         dapp    (get dapps dapp-id)]
     dapp)))

(defn- dapp-by-tab-id-query
  [[_ tab-id]]
  [(rf/subscribe [:browser/dapp-for-tab tab-id])])

(rf/reg-sub :browser/tab-title
 dapp-by-tab-id-query
 (fn [[dapp]]
   (let [title (-> dapp :metadata :title)
         url   (-> dapp :origin-url)]
     (or title url))))

(rf/reg-sub :browser/screenshots
 (fn [db]
   (get db :browser/screenshots)))

(rf/reg-sub :browser/tab-screenshot
 :<- [:browser/screenshots]
 (fn [screenshots [_ tab-id]]
   (get-in screenshots [tab-id :url])))
