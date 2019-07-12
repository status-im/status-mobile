(ns status-im.data-store.browser
  (:require [re-frame.core :as re-frame]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]))

(fx/defn initialize-browsers
  {:events [::initialize]}
  [{:keys [db]} all-stored-browsers]
  (let [browsers (reduce (fn [acc {:keys [browser-id] :as browser}]
                           (assoc acc browser-id browser))
                         {}
                         all-stored-browsers)]
    {:db (assoc db :browser/browsers browsers)}))

(re-frame/reg-fx
 :data-store/get-browsers
 (fn []
   (json-rpc/call
    {:method "browsers_getBrowsers"
     :on-success #(re-frame/dispatch [::initialize %])})))

(re-frame/reg-fx
 :data-store/save-browser
 (fn [browser]
   (json-rpc/call
    {:method "browsers_addBrowser"
     :params [(select-keys browser [:browser-id :timestamp :name :dapp? :history :history-index])]
     :on-success #()})))

(re-frame/reg-fx
 :data-store/delete-browser
 (fn [browser-id]
   (json-rpc/call
    {:method "browsers_deleteBrowser"
     :params [browser-id]
     :on-success #()})))
