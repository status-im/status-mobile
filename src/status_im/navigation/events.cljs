(ns status-im.navigation.events
  (:require
    [clojure.string :as string]
    [re-frame.core :as re-frame]
    [react-native.core :as react]
    [react-native.platform :as platform]
    [utils.re-frame :as rf]
    [utils.url :as url]))

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
   {:db          (-> db
                     (assoc :view-id go-to-view-id)
                     (all-screens-params go-to-view-id screen-params))
    :dispatch-n  [[:hide-bottom-sheet]]
    :navigate-to [go-to-view-id (:theme db) (:animation screen-params)]}))

(rf/defn navigate-to-within-stack
  {:events [:navigate-to-within-stack]}
  [{:keys [db]} comp-id screen-params]
  {:db (all-screens-params db (first comp-id) screen-params)
   :fx [[:navigate-to-within-stack (conj comp-id (:theme db))]]})

(re-frame/reg-event-fx :open-modal
 (fn [{:keys [db]} [component screen-params]]
   {:db (-> db
            (assoc :view-id component)
            (all-screens-params component screen-params))
    :fx [[:dispatch [:hide-bottom-sheet]]
         [:dispatch [:dismiss-keyboard]]
         [:open-modal-fx [component (:theme db)]]]}))

(rf/defn dismiss-modal
  {:events [:dismiss-modal]}
  [{:keys [db]} comp-id]
  {:dismiss-modal comp-id})

(rf/defn navigate-back
  {:events [:navigate-back]}
  [{:keys [db]}]
  {:fx [[:navigate-back nil]
        ;; Required for navigating back using the Android hardware back button
        (when (and platform/android? (= (:view-id db) :chat))
          [:dispatch [:chat/close]])]})

(rf/defn navigate-back-to
  {:events [:navigate-back-to]}
  [{:keys [db]} comp-id]
  {:navigate-back-to comp-id})

(rf/defn pop-to-root
  {:events [:pop-to-root]}
  [{:keys [db]} tab]
  (cond->
    {:pop-to-root-fx tab
     :db             (assoc db :view-id (:shell/selected-stack-id db))}

    (:current-chat-id db)
    (assoc :dispatch [:chat/close (:current-chat-id db)])))

(rf/defn init-root
  "WARNING: Use `:update-theme-and-init-root` instead. `:init-root` should not be used directly."
  {:events [:init-root]}
  [{:keys [db]} root-id]
  {:set-root [root-id (:theme db)]})

(rf/defn update-theme-and-init-root
  {:events [:update-theme-and-init-root]}
  [_ root-id]
  {:fx [[:dispatch [:theme/switch {:view-id root-id}]]
        [:dispatch [:init-root root-id]]]})

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
     {:db                         (assoc db :bottom-sheet {:sheets rest-sheets :hide? false})
      :hide-bottom-sheet          nil
      :reload-status-nav-color-fx [(:view-id db) (:theme db)]}
     (when (seq rest-sheets)
       {:dispatch [:show-next-bottom-sheet]}))))

(rf/defn show-next-bottom-sheet
  {:events [:show-next-bottom-sheet]}
  [_]
  {:show-bottom-sheet nil})

(rf/defn show-bottom-sheet
  {:events [:show-bottom-sheet]}
  [{:keys [db] :as cofx} content]
  (let [theme                  (or (:theme content) (:theme db))
        {:keys [sheets hide?]} (:bottom-sheet db)]
    (rf/merge cofx
              {:db               (update-in db [:bottom-sheet :sheets] conj content)
               :dismiss-keyboard nil}
              (fn [new-cofx]
                (when-not hide?
                  (if (seq sheets)
                    (hide-bottom-sheet new-cofx)
                    {:show-bottom-sheet {:theme theme}}))))))

(rf/defn set-view-id
  {:events [:set-view-id]}
  [{:keys [db]} view-id]
  (let [view-id (if (= view-id :shell-stack) (:shell/selected-stack-id db) view-id)]
    {:db             (assoc db :view-id view-id)
     :set-view-id-fx [view-id (:theme db)]}))

(rf/defn reload-status-nav-color
  {:events [:reload-status-nav-color]}
  [{:keys [db]} view-id]
  {:reload-status-nav-color-fx [(or view-id (:view-id db)) (:theme db)]})

(defn open-share
  [_ [config]]
  {:fx [[:effects.share/open config]]})

(rf/reg-event-fx :open-share open-share)

(rf/reg-event-fx :open-url
 (fn [_ [url]]
   {:fx [[:effects/open-url url]]}))

(rf/reg-fx
 :effects/open-url
 (fn [url]
   (when (not (string/blank? url))
     (.openURL ^js react/linking (url/normalize-url url)))))
