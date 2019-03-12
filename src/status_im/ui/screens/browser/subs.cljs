(ns status-im.ui.screens.browser.subs
  (:require [re-frame.core :as re-frame]
            [status-im.browser.core :as browser]))

(re-frame/reg-sub
 :browsers
 (fn [db _]
   (:browser/browsers db)))

(re-frame/reg-sub
 :browser/browsers
 :<- [:browsers]
 :<- [:contacts/dapps-by-name]
 (fn [[browsers dapps]]
   (reduce (fn [acc [k {:keys [dapp? name] :as browser}]]
             (cond-> (update acc k assoc
                             :url (browser/get-current-url browser))
               dapp? (assoc-in [k :dapp] (get dapps name))))
           browsers
           browsers)))

(re-frame/reg-sub
 :browser/browsers-vals
 :<- [:browser/browsers]
 (fn [browsers]
   (sort-by :timestamp > (vals browsers))))

(re-frame/reg-sub
 :get-current-browser
 :<- [:get :browser/options]
 :<- [:browser/browsers]
 (fn [[options browsers]]
   (let [browser (get browsers (:browser-id options))]
     (assoc browser :secure? (browser/secure? browser options)))))
