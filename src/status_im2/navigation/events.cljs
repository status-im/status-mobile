(ns status-im2.navigation.events
  (:require [utils.re-frame :as rf]))

(defn- all-screens-params
  [db view screen-params]
  (cond-> db
    (and (seq screen-params) (:screen screen-params) (:params screen-params))
    (all-screens-params (:screen screen-params) (:params screen-params))

    (seq screen-params)
    (assoc-in [:navigation/screen-params view] screen-params)))

(rf/defn navigate-to-cofx
  [{:keys [db] :as cofx} go-to-view-id screen-params]
  (merge
   {:db             (-> (assoc db :view-id go-to-view-id)
                        (all-screens-params go-to-view-id screen-params))
    :navigate-to-fx go-to-view-id}
   (when (#{:chat :community-overview} go-to-view-id)
     {:dispatch-later
      ;; 300 ms delay because, navigation is priority over shell card update
      [{:dispatch [:shell/add-switcher-card go-to-view-id screen-params]
        :ms       300}]})))

(rf/defn navigate-to
  {:events [:navigate-to]}
  [cofx go-to-view-id screen-params]
  (navigate-to-cofx cofx go-to-view-id screen-params))

(rf/defn navigate-back
  {:events [:navigate-back]}
  [_]
  {:navigate-back-fx nil})

(rf/defn pop-to-root
  {:events [:pop-to-root]}
  [_ tab]
  {:pop-to-root-fx tab})

(rf/defn set-stack-root
  {:events [:set-stack-root]}
  [_ stack root]
  {:set-stack-root-fx [stack root]})

(rf/defn change-tab
  {:events [:navigate-change-tab]}
  [_ stack-id]
  {:shell/change-tab-fx stack-id})

(rf/defn navigate-replace
  {:events [:navigate-replace]}
  [{:keys [db]} go-to-view-id screen-params]
  (let [db (cond-> (assoc db :view-id go-to-view-id)
             (seq screen-params)
             (assoc-in [:navigation/screen-params go-to-view-id] screen-params))]
    {:db                  db
     :navigate-replace-fx go-to-view-id}))

(rf/defn open-modal
  {:events [:open-modal]}
  [{:keys [db]} comp screen-params]
  {:db            (-> (assoc db :view-id comp)
                      (all-screens-params comp screen-params))
   :open-modal-fx comp})

(rf/defn init-root
  {:events [:init-root]}
  [_ root-id]
  {:init-root-fx root-id})

(rf/defn hide-signing-sheet
  {:events [:hide-signing-sheet]}
  [_]
  {:hide-signing-sheet nil})

(rf/defn hide-select-acc-sheet
  {:events [:hide-select-acc-sheet]}
  [_]
  {:hide-select-acc-sheet nil})

(rf/defn hide-wallet-connect-sheet
  {:events [:hide-wallet-connect-sheet]}
  [_]
  {:hide-wallet-connect-sheet nil})

(rf/defn hide-wallet-connect-success-sheet
  {:events [:hide-wallet-connect-success-sheet]}
  [_]
  {:hide-wallet-connect-success-sheet nil})

(rf/defn hide-wallet-connect-app-management-sheet
  {:events [:hide-wallet-connect-app-management-sheet]}
  [{:keys [db]}]
  {:db                                       (-> db
                                                 (assoc db
                                                        :wallet-connect/showing-app-management-sheet?
                                                        false)
                                                 (dissoc :wallet-connect/session-managed))
   :hide-wallet-connect-app-management-sheet nil})

;; NAVIGATION 2
(rf/defn reload-new-ui
  {:events [:reload-new-ui]}
  [_]
  {:shell/reset-bottom-tabs nil
   :dispatch                [:init-root :shell-stack]})

(rf/defn change-root-status-bar-style
  {:events [:change-root-status-bar-style]}
  [_ style]
  {:change-root-status-bar-style-fx style})

(rf/defn change-root-nav-bar-color
  {:events [:change-root-nav-bar-color]}
  [_ color]
  {:change-root-nav-bar-color-fx color})
