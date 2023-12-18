(ns status-im2.navigation.events
  (:require
    [re-frame.core :as re-frame]
    [status-im2.contexts.shell.jump-to.events :as shell.events]
    [status-im2.contexts.shell.jump-to.state :as shell.state]
    [status-im2.contexts.shell.jump-to.utils :as shell.utils]
    [utils.re-frame :as rf]))

(defn- all-screens-params
  [db view screen-params]
  (cond-> db
    (and (seq screen-params) (:screen screen-params) (:params screen-params))
    (all-screens-params (:screen screen-params) (:params screen-params))

    (seq screen-params)
    (assoc-in [:navigation/screen-params view] screen-params)))

(rf/defn navigate-to
  {:events [:navigate-to]}
  [{:keys [db] :as cofx} go-to-view-id screen-params]
  (rf/merge
   cofx
   {:db         (all-screens-params db go-to-view-id screen-params)
    :dispatch-n [[:hide-bottom-sheet]]}
   (shell.events/shell-navigate-to go-to-view-id screen-params nil nil)))

(rf/defn navigate-to-within-stack
  {:events [:navigate-to-within-stack]}
  [_ comp-id]
  {:navigate-to-within-stack comp-id})

(re-frame/reg-event-fx :open-modal
 (fn [{:keys [db]} [component screen-params]]
   {:db (-> db
            (assoc :view-id component)
            (all-screens-params component screen-params))
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:open-modal-fx component]]}))

(rf/defn dismiss-modal
  {:events [:dismiss-modal]}
  [_ comp-id]
  {:dismiss-modal comp-id})

(rf/defn navigate-back
  {:events [:navigate-back]}
  [cofx]
  (shell.events/shell-navigate-back cofx nil))

(rf/defn navigate-back-within-stack
  {:events [:navigate-back-within-stack]}
  [_ comp-id]
  {:navigate-back-within-stack comp-id})

(rf/defn navigate-back-to
  {:events [:navigate-back-to]}
  [_ comp-id]
  {:navigate-back-to comp-id})

(rf/defn pop-to-root
  {:events [:pop-to-root]}
  [{:keys [db]} tab]
  {:pop-to-root-fx            tab
   :db                        (-> db
                                  (dissoc :shell/floating-screens)
                                  (dissoc :shell/loaded-screens)
                                  (assoc :view-id (or @shell.state/selected-stack-id :shell)))
   :effects.shell/pop-to-root nil})

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
  [{:keys [db]} stack-id]
  {:db                       (assoc db :view-id stack-id)
   :effects.shell/change-tab stack-id})

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

(rf/defn set-multiaccount-root
  {:events [:set-multiaccount-root]}
  [{:keys [db]}]
  (let [key-uid          (get-in db [:profile/login :key-uid])
        keycard-account? (boolean (get-in db
                                          [:profile/profiles-overview
                                           key-uid
                                           :keycard-pairing]))]
    {:set-root (if keycard-account? :multiaccounts-keycard :multiaccounts)}))

(rf/defn dismiss-all-overlays
  {:events [:dismiss-all-overlays]}
  [_]
  {:dispatch-n [[:hide-popover]
                [:hide-visibility-status-popover]
                [:hide-bottom-sheet]
                [:bottom-sheet-hidden]
                [:hide-signing-sheet]
                [:bottom-sheet/hide-old-navigation-overlay]
                [:toasts/close-all-toasts]]})

(rf/defn set-view-id
  {:events [:set-view-id]}
  [{:keys [db]} view-id]
  (let [view-id (if (= view-id :shell-stack) (shell.utils/calculate-view-id) view-id)]
    {:db             (assoc db :view-id view-id)
     :set-view-id-fx view-id}))
