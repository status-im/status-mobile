(ns status-im.ui.screens.navigation
  (:require [re-frame.core :as re-frame]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.navigation :as navigation]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]))

;; private helper fns

(defn- push-view [db view-id]
  (-> db
      (update :navigation-stack conj view-id)
      (assoc :view-id view-id)))

;; public fns

(fx/defn navigate-forget
  [{:keys [db]} view-id]
  {:db (assoc db :view-id view-id)})

(defmulti unload-data!
  (fn [db] (:view-id db)))

(defmethod unload-data! :default [db] db)

(defmulti preload-data!
  (fn [db [_ view-id]] (or view-id (:view-id db))))

(defmethod preload-data! :default [db _] db)

(defn- -preload-data! [{:keys [was-modal?] :as db} & args]
  (if was-modal?
    (dissoc db :was-modal?) ;;TODO check how it worked with this bug
    (apply preload-data! db args)))

(fx/defn navigate-to-cofx
  [{:keys [db]} go-to-view-id screen-params]
  (let [view-id (:view-id db)
        db      (cond-> (assoc db :view-id go-to-view-id)
                  (seq screen-params)
                  (assoc-in [:navigation/screen-params go-to-view-id]
                            screen-params))]
    {:db           (if (= view-id go-to-view-id)
                     db
                     (push-view db go-to-view-id))
     ::navigate-to [go-to-view-id screen-params]}))

(fx/defn navigate-reset
  [{:keys [db]} {:keys [index actions] :as config}]
  (let [stack (into '() (map :routeName actions))
        view-id (get stack index)]
    {:db              (assoc db
                             :view-id view-id
                             ;;NOTE: stricly needs to be a list
                             ;;because navigate-back pops it
                             :navigation-stack stack)
     ::navigate-reset config}))

(def unload-data-interceptor
  (re-frame/->interceptor
   :id unload-data-interceptor
   :before (fn unload-data-interceptor-before
             [context]
             (let [db (re-frame/get-coeffect context :db)]
               (re-frame/assoc-coeffect context :db (unload-data! db))))))

(def navigation-interceptors
  [unload-data-interceptor (re-frame/enrich preload-data!)])

;; effects

(re-frame/reg-fx
 ::navigate-to
 (fn [[view-id params]]
   (log/debug :navigate-to view-id params)
   (navigation/navigate-to (name view-id) params)))

(re-frame/reg-fx
 ::navigate-back
 (fn []
   (log/debug :navigate-back)
   (navigation/navigate-back)))

(re-frame/reg-fx
 ::navigate-reset
 (fn [config]
   (log/debug :navigate-reset config)
   (navigation/navigate-reset config)))

;; event handlers

(handlers/register-handler-fx
 :navigate-to
 navigation-interceptors
 (fn [cofx [_ & [go-to-view-id screen-params]]]
   (navigate-to-cofx cofx go-to-view-id screen-params)))

(handlers/register-handler-fx
 :navigate-to-modal
 navigation-interceptors
 (fn [{:keys [db]} [_ modal-view]]
   {:db (assoc db :modal modal-view)}))

(fx/defn navigate-back
  [{{:keys [navigation-stack view-id] :as db} :db}]
  (assoc
   {::navigate-back nil}
   :db (let [[previous-view-id :as navigation-stack'] (pop navigation-stack)
             first-in-stack (first navigation-stack)]
         (if (= view-id first-in-stack)
           (-> db
               (assoc :view-id previous-view-id)
               (assoc :navigation-stack navigation-stack'))
           (assoc db :view-id first-in-stack)))))

(handlers/register-handler-fx
 :navigate-back
 (re-frame/enrich -preload-data!)
 (fn [cofx _]
   (navigate-back cofx)))

(handlers/register-handler-fx
 :navigate-reset
 (fn [cofx [_ view-id]]
   (navigate-reset cofx {:index   0
                         :actions [{:routeName view-id}]})))

(handlers/register-handler-fx
 :navigate-to-clean
 (fn [cofx [_ view-id params]]
   (navigate-to-cofx cofx view-id params)))

(handlers/register-handler-fx
 :navigate-to-tab
 navigation-interceptors
 (fn [{:keys [db] :as cofx} [_ view-id]]
   (fx/merge cofx
             {:db (-> db
                      (assoc :prev-tab-view-id (:view-id db))
                      (assoc :prev-view-id (:view-id db)))}
             (navigate-to-cofx view-id {}))))

;; This atom stores event vector
;; to be dispatched when a react-navigation's BACK
;; actions is invoked
(def wizard-back-event (atom nil))

;; This atom exists in order to avoid
;; endless loop when processing NavigationActions/BACK
;; in react-navigation's getStateForAction fn
(def processing-back-event? (atom false))

(fx/defn reset-processing-flag
  {:events [:navigation/reset-processing-flag]}
  [{:keys [db] :as cofx}]
  {::reset-processing-flag nil})

(re-frame/reg-fx
 ::reset-processing-flag
 (fn []
   (reset! processing-back-event? false)))

;; Below two effects are added when we need
;; to override default react-navigation's BACK action
;; processing
(re-frame/reg-fx
 ::add-wizard-back-event
 (fn [event]
   (reset! wizard-back-event event)))

(re-frame/reg-fx
 ::remove-wizard-back-event
 (fn [event]
   (reset! processing-back-event? false)
   (reset! wizard-back-event nil)))
