(ns status-im.data-store.browser
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]))

;; TODO: adjust the api so there is no need to do these transformations after
;; benchmarking the cost of recreating all these maps with different keys
(fx/defn initialize-browsers
  {:events [::browsers]}
  [{:keys [db]} all-stored-browsers]
  (let [browsers (reduce (fn [acc {:keys [id name timestamp dapp historyIndex history]}]
                           (assoc acc id {:browser-id id
                                          :name name
                                          :timestamp timestamp
                                          :dapp dapp
                                          :history-index historyIndex
                                          :history history}))
                         {}
                         all-stored-browsers)]
    {:db (assoc db :browser/browsers browsers)}))

(re-frame/reg-fx
 :data-store/get-browsers
 (fn []
   (json-rpc/call
    {:method "browsers_getBrowsers"
     :on-success #(re-frame/dispatch [::browsers %])})))

(defn save-browser-tx
  "Returns tx function for saving browser"
  [{:keys [browser-id timestamp name dapp? history history-index]}]
  (fn [realm]
    (json-rpc/call
     {:method "browsers_addBrowser"
      :params [{"id" browser-id
                "name" name
                "timestamp" timestamp
                "dapp" dapp?
                "historyIndex" history-index
                "history" history}]
      :on-success #()})))

(defn remove-browser-tx
  "Returns tx function for removing browser"
  [browser-id]
  (fn [realm]
    (json-rpc/call {:method "browsers_deleteBrowser"
                    :params [browser-id]
                    :on-success #()})))
