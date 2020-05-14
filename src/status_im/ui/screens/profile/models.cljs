(ns status-im.ui.screens.profile.models
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as clojure.string]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.chat.models :as chat-models]
            [taoensso.timbre :as log]
            [status-im.utils.fx :as fx]))

(defn send-transaction [chat-id cofx]
  ;;TODO start send transaction command flow
  (chat-models/start-chat cofx chat-id {:navigation-reset? true}))

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
        cleaned-name (clean-name db :my-profile/profile)]
    (fx/merge cofx
              (clear-profile)
              (multiaccounts.update/multiaccount-update :name cleaned-name {})
              (multiaccounts.update/multiaccount-update :last-updated now {})
              (when photo-path
                (multiaccounts.update/multiaccount-update :photo-path photo-path {})))))

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
