(ns legacy.status-im.profile.core
  (:require
    [clojure.string :as string]
    [legacy.status-im.multiaccounts.update.core :as multiaccounts.update]
    [legacy.status-im.ui.components.react :as react]
    [re-frame.core :as re-frame]
    [utils.re-frame :as rf]))

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
                                       (swap! tooltips assoc-in
                                         [tooltip-id :opacity]
                                         (- opacity 0.05)))))))
                          100)]
         (swap! tooltips assoc tooltip-id {:opacity 1.0 :interval-id interval-id :cnt 0}))))))

(rf/defn finish-success
  {:events [:my-profile/finish-success]}
  [{:keys [db] :as cofx}]
  {:db (update db :my-profile/seed assoc :step :finish :error nil :word nil)})

(rf/defn finish
  {:events [:my-profile/finish]}
  [cofx]
  (multiaccounts.update/clean-seed-phrase
   cofx
   {:on-success #(re-frame/dispatch [:my-profile/finish-success])}))

(rf/defn enter-two-random-words
  {:events [:my-profile/enter-two-random-words]}
  [{:keys [db]}]
  (let [{:keys [mnemonic]} (:profile/profile db)
        shuffled-mnemonic  (shuffle (map-indexed vector (string/split mnemonic #" ")))]
    {:db (assoc db
                :my-profile/seed
                {:step        :first-word
                 :first-word  (first shuffled-mnemonic)
                 :second-word (second shuffled-mnemonic)})}))

(rf/defn set-step
  {:events [:my-profile/set-step]}
  [{:keys [db]} step]
  {:db (update db :my-profile/seed assoc :step step :error nil :word nil)})

(rf/defn copy-to-clipboard
  {:events [:copy-to-clipboard]}
  [_ value]
  {:copy-to-clipboard value})

(rf/defn show-tooltip
  {:events [:show-tooltip]}
  [_ tooltip-id]
  {:show-tooltip tooltip-id})

(rf/defn show-profile
  {:events [:chat.ui/show-profile]}
  [{:keys [db]} identity ens-name]
  (let [my-public-key (get-in db [:profile/profile :public-key])]
    (if (not= my-public-key identity)
      {:db       (-> db
                     (assoc :contacts/identity identity)
                     (assoc :contacts/ens-name ens-name))
       :dispatch [:contacts/build-contact identity ens-name true]}
      {:dispatch [:navigate-to :my-profile]})))
