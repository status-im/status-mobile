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
  [{:keys [db]} go-to-view-id screen-params]
  {:db
   (-> (assoc db :view-id go-to-view-id)
       (all-screens-params go-to-view-id screen-params))
   :navigate-to-fx go-to-view-id})

(rf/defn navigate-to
  {:events [:navigate-to]}
  [cofx go-to-view-id screen-params]
  (navigate-to-cofx cofx go-to-view-id screen-params))

(rf/defn navigate-back
  {:events [:navigate-back]}
  [_]
  {:navigate-back-fx nil})

(rf/defn pop-to-root-tab
  {:events [:pop-to-root-tab]}
  [_ tab]
  {:pop-to-root-tab-fx tab})

(rf/defn set-stack-root
  {:events [:set-stack-root]}
  [_ stack root]
  {:set-stack-root-fx [stack root]})

(rf/defn change-tab
  {:events [:navigate-change-tab]}
  [_ tab])
  ;{:change-tab-fx tab} ; TODO: effect needs to be implemented (may not be possible:
  ;https://github.com/wix/react-native-navigation/issues/4837)

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

(rf/defn init-root-with-component
  {:events [:init-root-with-component]}
  [_ root-id comp-id]
  {:init-root-with-component-fx [root-id comp-id]})

(rf/defn change-tab-count
  {:events [:change-tab-count]}
  [_ tab cnt]
  {:change-tab-count-fx [tab cnt]})

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

(rf/defn navigate-to-nav2
  {:events [:navigate-to-nav2]}
  [cofx view-id screen-params from-shell?]
  (rf/merge
   cofx
   {:dispatch [:shell/add-switcher-card view-id screen-params from-shell?]}
   (navigate-to-cofx view-id screen-params)))

(rf/defn change-root-status-bar-style
  {:events [:change-root-status-bar-style]}
  [_ style]
  {:change-root-status-bar-style-fx style})

(rf/defn change-root-nav-bar-color
  {:events [:change-root-nav-bar-color]}
  [_ color]
  {:change-root-nav-bar-color-fx color})
