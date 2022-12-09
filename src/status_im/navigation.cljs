(ns status-im.navigation
  (:require [status-im.utils.fx :as fx]))

(defn- all-screens-params [db view screen-params]
  (cond-> db
    (and (seq screen-params) (:screen screen-params) (:params screen-params))
    (all-screens-params (:screen screen-params) (:params screen-params))

    (seq screen-params)
    (assoc-in [:navigation/screen-params view] screen-params)))

(fx/defn navigate-to-cofx
  [{:keys [db]} go-to-view-id screen-params]
  {:db
   (-> (assoc db :view-id go-to-view-id)
       (all-screens-params go-to-view-id screen-params))
   :navigate-to-fx go-to-view-id})

(fx/defn navigate-to
  {:events [:navigate-to]}
  [cofx go-to-view-id screen-params]
  (navigate-to-cofx cofx go-to-view-id screen-params))

(fx/defn navigate-back
  {:events [:navigate-back]}
  [_]
  {:navigate-back-fx nil})

(fx/defn pop-to-root-tab
  {:events [:pop-to-root-tab]}
  [_ tab]
  {:pop-to-root-tab-fx tab})

(fx/defn set-stack-root
  {:events [:set-stack-root]}
  [_ stack root]
  {:set-stack-root-fx [stack root]})

(fx/defn change-tab
  {:events [:navigate-change-tab]}
  [_ tab]
  {:change-tab-fx tab})

(fx/defn navigate-replace
  {:events       [:navigate-replace]}
  [{:keys [db]} go-to-view-id screen-params]
  (let [db (cond-> (assoc db :view-id go-to-view-id)
             (seq screen-params)
             (assoc-in [:navigation/screen-params go-to-view-id] screen-params))]
    {:db                  db
     :navigate-replace-fx go-to-view-id}))

(fx/defn open-modal
  {:events [:open-modal]}
  [{:keys [db]} comp screen-params]
  {:db            (-> (assoc db :view-id comp)
                      (all-screens-params comp screen-params))
   :open-modal-fx comp})

(fx/defn init-root
  {:events [:init-root]}
  [_ root-id]
  {:init-root-fx root-id})

(fx/defn init-root-with-component
  {:events [:init-root-with-component]}
  [_ root-id comp-id]
  {:init-root-with-component-fx [root-id comp-id]})

(fx/defn change-tab-count
  {:events [:change-tab-count]}
  [_ tab cnt]
  {:change-tab-count-fx [tab cnt]})

(fx/defn hide-signing-sheet
  {:events [:hide-signing-sheet]}
  [_]
  {:hide-signing-sheet nil})

(fx/defn hide-select-acc-sheet
  {:events [:hide-select-acc-sheet]}
  [_]
  {:hide-select-acc-sheet nil})

(fx/defn hide-wallet-connect-sheet
  {:events [:hide-wallet-connect-sheet]}
  [_]
  {:hide-wallet-connect-sheet nil})

(fx/defn hide-wallet-connect-success-sheet
  {:events [:hide-wallet-connect-success-sheet]}
  [_]
  {:hide-wallet-connect-success-sheet nil})

(fx/defn hide-wallet-connect-app-management-sheet
  {:events [:hide-wallet-connect-app-management-sheet]}
  [{:keys [db]}]
  {:db (-> db
           (assoc db :wallet-connect/showing-app-management-sheet? false)
           (dissoc :wallet-connect/session-managed))
   :hide-wallet-connect-app-management-sheet nil})
