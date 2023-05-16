(ns status-im2.navigation.events
  (:require [utils.re-frame :as rf]))

(defn- all-screens-params
  [db view screen-params]
  (cond-> db
    (and (seq screen-params) (:screen screen-params) (:params screen-params))
    (all-screens-params (:screen screen-params) (:params screen-params))

    (seq screen-params)
    (assoc-in [:navigation/screen-params view] screen-params)))

(rf/defn navigate-to
  {:events [:navigate-to]}
  [{:keys [db]} go-to-view-id screen-params]
  (merge
   {:db          (-> (assoc db :view-id go-to-view-id)
                     (all-screens-params go-to-view-id screen-params))
    :navigate-to go-to-view-id
    :dispatch    [:hide-bottom-sheet]}
   (when (#{:chat :community-overview} go-to-view-id)
     {:dispatch-later
      ;; 300 ms delay because, navigation is priority over shell card update
      [{:dispatch [:shell/add-switcher-card go-to-view-id screen-params]
        :ms       300}]})))

(rf/defn open-modal
  {:events [:open-modal]}
  [{:keys [db]} comp screen-params]
  {:db            (-> (assoc db :view-id comp)
                      (all-screens-params comp screen-params))
   :dispatch      [:hide-bottom-sheet]
   :open-modal-fx comp})

(rf/defn navigate-back
  {:events [:navigate-back]}
  [_]
  {:navigate-back nil})

(rf/defn pop-to-root
  {:events [:pop-to-root]}
  [_ tab]
  {:pop-to-root-fx tab})

(rf/defn init-root
  {:events [:init-root]}
  [_ root-id]
  {:set-root root-id})

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

(rf/defn hide-bottom-sheet
  {:events [:hide-bottom-sheet]}
  [{:keys [db]}]
  (let [{:keys [hide? sheets]} (:bottom-sheet db)]
    (when (and (not hide?) (seq sheets))
      {:db (assoc-in db [:bottom-sheet :hide?] true)})))

(rf/defn bottom-sheet-hidden
  {:events [:bottom-sheet-hidden]}
  [{:keys [db]}]
  (let [{:keys [sheets]} (:bottom-sheet db)
        rest-sheets      (butlast sheets)]
    (merge
     {:db                (assoc db :bottom-sheet {:sheets rest-sheets :hide? false})
      :hide-bottom-sheet nil}
     (when (seq rest-sheets)
       {:dispatch [:show-next-bottom-sheet]}))))

(rf/defn show-next-bottom-sheet
  {:events [:show-next-bottom-sheet]}
  [_]
  {:show-bottom-sheet nil})

(rf/defn show-bottom-sheet
  {:events [:show-bottom-sheet]}
  [{:keys [db]} content]
  (let [{:keys [sheets hide?]} (:bottom-sheet db)]
    (rf/merge {:db (update-in db [:bottom-sheet :sheets] #(conj % content))}
              #(when-not hide?
                 (if (seq sheets)
                   (hide-bottom-sheet %)
                   {:show-bottom-sheet nil})))))

;; LEGACY (should be removed in status 2.0)
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

(rf/defn set-multiaccount-root
  {:events [:set-multiaccount-root]}
  [{:keys [db]}]
  (let [key-uid          (get-in db [:multiaccounts/login :key-uid])
        keycard-account? (boolean (get-in db
                                          [:multiaccounts/multiaccounts
                                           key-uid
                                           :keycard-pairing]))]
    {:set-root (if keycard-account? :multiaccounts-keycard :multiaccounts)}))

(rf/defn dismiss-all-overlays
  {:events [:dissmiss-all-overlays]}
  [{:keys [db]}]
  {:dispatch-n [[:hide-popover]
                [:hide-visibility-status-popover]
                [:hide-bottom-sheet]
                [:bottom-sheet-hidden]
                [:hide-wallet-connect-sheet]
                [:hide-wallet-connect-success-sheet]
                [:hide-wallet-connect-app-management-sheet]
                [:hide-signing-sheet]
                [:hide-select-acc-sheet]
                [:bottom-sheet/hide-old-navigation-overlay]
                [:toasts/close-all-toasts]]})
