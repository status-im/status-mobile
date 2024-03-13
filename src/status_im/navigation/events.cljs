(ns status-im.navigation.events
  (:require
    [re-frame.core :as re-frame]
    [status-im.contexts.shell.jump-to.events :as shell.events]
    [status-im.contexts.shell.jump-to.state :as shell.state]
    [status-im.contexts.shell.jump-to.utils :as shell.utils]
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

(defn- add-view-to-modals
  [modal-view-ids new-id]
  (if (seq modal-view-ids)
    (conj modal-view-ids new-id)
    modal-view-ids))

(rf/defn navigate-to-within-stack
  {:events [:navigate-to-within-stack]}
  [{:keys [db]} comp-id]
  {:db (-> db
           (update :modal-view-ids add-view-to-modals (first comp-id))
           (assoc :view-id (first comp-id)))
   :fx [[:navigate-to-within-stack comp-id]]})

(re-frame/reg-event-fx :open-modal
 (fn [{:keys [db]} [component screen-params]]
   {:db (-> db
            (assoc :view-id component)
            (assoc :modal-view-ids [component])
            (all-screens-params component screen-params))
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:open-modal-fx component]]}))

(rf/defn dismiss-modal
  {:events [:dismiss-modal]}
  [{:keys [db]} comp-id]
  {:db            (dissoc db :modal-view-ids)
   :dismiss-modal comp-id})

(rf/defn navigate-back
  {:events [:navigate-back]}
  [cofx]
  (shell.events/shell-navigate-back cofx nil))

(defn- remove-last-view-to-modals
  [modal-view-ids]
  (if (seq modal-view-ids)
    (pop modal-view-ids)
    modal-view-ids))

(rf/defn navigate-back-within-stack
  {:events [:navigate-back-within-stack]}
  [{:keys [db]} comp-id]
  {:db (update db :modal-view-ids remove-last-view-to-modals)
   :fx [[:navigate-back-within-stack comp-id]]})

(defn- remove-modal-views-until-comp-id
  [modal-view-ids comp-id]
  (let [comp-id-index  (.indexOf (or modal-view-ids []) comp-id)
        modal-view-ids (if (> comp-id-index -1)
                         (subvec modal-view-ids 0 (inc comp-id-index))
                         modal-view-ids)]
    modal-view-ids))

(rf/defn navigate-back-to
  {:events [:navigate-back-to]}
  [{:keys [db]} comp-id]
  (let [modal-view-ids (remove-modal-views-until-comp-id (:modal-view-ids db) comp-id)]
    (assoc {:navigate-back-to comp-id}
           :db
           (if modal-view-ids
             (assoc db :modal-view-ids modal-view-ids)
             (dissoc db :modal-view-ids)))))

(rf/defn pop-to-root
  {:events [:pop-to-root]}
  [{:keys [db]} tab]
  {:pop-to-root-fx            tab
   :db                        (-> db
                                  (dissoc :shell/floating-screens)
                                  (dissoc :shell/loaded-screens)
                                  (dissoc :modal-view-ids)
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
  [{:keys [db] :as cofx} content]
  (let [{:keys [sheets hide?]} (:bottom-sheet db)]
    (rf/merge cofx
              {:db               (update-in db [:bottom-sheet :sheets] #(conj % content))
               :dismiss-keyboard nil}
              #(when-not hide?
                 (if (seq sheets) (hide-bottom-sheet %) {:show-bottom-sheet nil})))))

(rf/defn dismiss-all-overlays
  {:events [:dismiss-all-overlays]}
  [_]
  {:dispatch-n [[:hide-popover]
                [:hide-visibility-status-popover]
                [:hide-bottom-sheet]
                [:bottom-sheet-hidden]
                [:bottom-sheet/hide-old-navigation-overlay]
                [:toasts/close-all-toasts]]})

(rf/defn set-view-id
  {:events [:set-view-id]}
  [{:keys [db]} view-id]
  (let [view-id (if (= view-id :shell-stack) (shell.utils/calculate-view-id) view-id)]
    {:db             (assoc db :view-id view-id)
     :set-view-id-fx view-id}))

(defn wizard-find-next-screen
  [db flow-config current-screen]
  (first (filter (fn [screen]
                   (let [skip-step (:skip-step? screen)]
                     (and (not= (:screen-id screen) current-screen)
                          (not (when (not (nil? skip-step)) (skip-step db))))))
                 flow-config)))

(rf/reg-event-fx
 :navigation/wizard-forward
 (fn [{:keys [db]} [{:keys [current-screen flow-config start-flow?]}]]
   (let [next-screen (wizard-find-next-screen db flow-config current-screen)]
     {:fx [[:dispatch
            (if start-flow?
              [:open-modal (:screen-id next-screen)]
              [:navigate-to-within-stack [(:screen-id next-screen) current-screen]])]]})))

(rf/reg-event-fx
 :navigation/wizard-backward
 (fn [{:keys [db]}]
   (let [stack          (:modal-view-ids db)
         current-screen (last stack)]
     {:fx [[:dispatch
            (if (= (count stack) 1)
              [:navigate-back]
              [:navigate-back-within-stack current-screen])]]})))
