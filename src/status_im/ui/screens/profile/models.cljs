(ns status-im.ui.screens.profile.models
  (:require [clojure.spec.alpha :as spec]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.profile.navigation]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.chat.models :as chat-models]
            [status-im.utils.image-processing :as image-processing]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]))

(defn open-image-picker! [callback-event]
  (react/show-image-picker
   (fn [image]
     (let [path (get (js->clj image) "path")
           _ (log/debug path)
           on-success (fn [base64]
                        (re-frame/dispatch [callback-event base64]))
           on-error (fn [type error]
                      (.log js/console type error))]
       (image-processing/img->base64 path on-success on-error 150 150)))
   "photo"))

(defn send-transaction [chat-id {:keys [db] :as cofx}]
  ;;TODO start send transaction command flow
  (chat-models/start-chat chat-id {:navigation-reset? true}))

(defn- valid-name? [name]
  (spec/valid? :profile/name name))

(defn update-name [name {:keys [db]}]
  {:db (-> db
           (assoc-in [:my-profile/profile :valid-name?] (valid-name? name))
           (assoc-in [:my-profile/profile :name] name))})

(defn- clean-name [db edit-view]
  (let [name (get-in db [edit-view :name])]
    (if (valid-name? name)
      name
      (get-in db [:multiaccount :name]))))

(fx/defn clear-profile
  [{:keys [db]}]
  {:db (dissoc db :my-profile/profile :my-profile/default-name :my-profile/editing?)})

(defn start-editing [{:keys [db]}]
  (let [profile (select-keys (:multiaccount db) [:name :photo-path])]
    {:db (assoc db
                :my-profile/editing? true
                :my-profile/profile profile)}))

(fx/defn save [{:keys [db now] :as cofx}]
  (let [{:keys [photo-path]} (:my-profile/profile db)
        cleaned-name (clean-name db :my-profile/profile)
        cleaned-edit (merge {:name         cleaned-name
                             :last-updated now}
                            (if photo-path
                              {:photo-path photo-path}))]
    (fx/merge cofx
              (clear-profile)
              (multiaccounts.update/multiaccount-update cleaned-edit {}))))

(defn update-picture [this-event base64-image {:keys [db] :as cofx}]
  (if base64-image
    (fx/merge cofx
              {:db (-> db
                       (assoc-in [:my-profile/profile :photo-path]
                                 (str "data:image/jpeg;base64," base64-image))
                       (assoc :my-profile/editing? true)
                       (assoc :profile/photo-added? true))}
              save)
    {:open-image-picker this-event}))

(defn start-editing-group-chat-profile [{:keys [db]}]
  (let [current-chat-name (get-in db [:chats (:current-chat-id db) :name])]
    {:db (-> db
             (assoc :group-chat-profile/editing? true)
             (assoc-in [:group-chat-profile/profile :name] current-chat-name))}))

(defn enter-two-random-words [{:keys [db]}]
  (let [{:keys [mnemonic]} (:multiaccount db)
        shuffled-mnemonic (shuffle (map-indexed vector (clojure.string/split mnemonic #" ")))]
    {:db (assoc db :my-profile/seed {:step        :first-word
                                     :first-word  (first shuffled-mnemonic)
                                     :second-word (second shuffled-mnemonic)})}))

(defn set-step [step {:keys [db]}]
  {:db (update db :my-profile/seed assoc :step step :error nil :word nil)})

(defn finish [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :my-profile/seed assoc :step :finish :error nil :word nil)}
            (multiaccounts.update/clean-seed-phrase)))

(defn copy-to-clipboard! [value]
  (react/copy-to-clipboard value))

(def show-tooltip!
  (let [tooltips (atom {})]
    (fn [tooltip-id]
      (when-let [{:keys [interval-id]} (@tooltips tooltip-id)]
        (js/clearInterval interval-id))
      (let [interval-id (js/setInterval
                         #(let [{:keys [opacity interval-id cnt]} (@tooltips tooltip-id)]
                            (when opacity
                              (swap! tooltips assoc-in [tooltip-id :cnt] (inc cnt))
                              (if (and opacity (>= 0.0 opacity))
                                (do
                                  (log/debug "remove interval:" interval-id)
                                  (js/clearInterval interval-id)
                                  (re-frame/dispatch [:set-in [:tooltips tooltip-id] nil])
                                  (swap! tooltips dissoc interval-id))
                                (do (re-frame/dispatch [:set-in [:tooltips tooltip-id] opacity])
                                    (when (< 10 cnt)
                                      (swap! tooltips assoc-in [tooltip-id :opacity] (- opacity 0.05)))))))
                         100)]
        (swap! tooltips assoc tooltip-id {:opacity 1.0 :interval-id interval-id :cnt 0})))))
