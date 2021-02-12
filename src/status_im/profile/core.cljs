(ns status-im.profile.core
  (:require [re-frame.core :as re-frame]
            [status-im.utils.fx :as fx]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.utils :as universal-links]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.ui.components.react :as react]
            [clojure.string :as string]))

(re-frame/reg-fx
 :copy-to-clipboard
 (fn [value]
   (react/copy-to-clipboard value)))

(re-frame/reg-fx
 :show-tooltip
 (fn []
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
                                   (js/clearInterval interval-id)
                                   (re-frame/dispatch [:set-in [:tooltips tooltip-id] nil])
                                   (swap! tooltips dissoc interval-id))
                                 (do (re-frame/dispatch [:set-in [:tooltips tooltip-id] opacity])
                                     (when (< 10 cnt)
                                       (swap! tooltips assoc-in [tooltip-id :opacity] (- opacity 0.05)))))))
                          100)]
         (swap! tooltips assoc tooltip-id {:opacity 1.0 :interval-id interval-id :cnt 0}))))))

(re-frame/reg-fx
 :profile/share-profile-link
 (fn [contact-code]
   (let [link (universal-links/generate-link :user :external contact-code)]
     (list-selection/open-share {:message link}))))

(fx/defn finish
  {:events [:my-profile/finish]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :my-profile/seed assoc :step :finish :error nil :word nil)}
            (multiaccounts.update/clean-seed-phrase)))

(fx/defn enter-two-random-words
  {:events [:my-profile/enter-two-random-words]}
  [{:keys [db]}]
  (let [{:keys [mnemonic]} (:multiaccount db)
        shuffled-mnemonic (shuffle (map-indexed vector (string/split mnemonic #" ")))]
    {:db (assoc db :my-profile/seed {:step        :first-word
                                     :first-word  (first shuffled-mnemonic)
                                     :second-word (second shuffled-mnemonic)})}))

(fx/defn set-step
  {:events [:my-profile/set-step]}
  [{:keys [db]} step]
  {:db (update db :my-profile/seed assoc :step step :error nil :word nil)})

(fx/defn copy-to-clipboard
  {:events [:copy-to-clipboard]}
  [_ value]
  {:copy-to-clipboard value})

(fx/defn show-tooltip
  {:events [:show-tooltip]}
  [_ tooltip-id]
  {:show-tooltip tooltip-id})

(fx/defn share-profile-link
  {:events [:profile/share-profile-link]}
  [_ value]
  {:profile/share-profile-link value})
