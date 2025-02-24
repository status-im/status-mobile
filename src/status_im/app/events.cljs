(ns status-im.app.events
  (:require
    [status-im.infra.transform :as transform]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]
    [re-frame.db :as rfdb]
    [re-frame.db :as db]))


(def navigation-effects-tree
  {:uc-view-saved-addresses
   {:on-start               [[:dispatch [:open-modal :screen/settings.saved-addresses]]]
    :uc-add-saved-addresses {:on-start  [[:dispatch [:open-modal :screen/settings.add-address-to-save]]]
                             :on-finish [[:dispatch [:navigate-back]]]}
    :on-finish              [[:dispatch [:navigate-back]]]}})


(def db-path-use-case-stack [:app :use-cases-stack])

(defn use-cases
  [db]
  (get-in db db-path-use-case-stack '()))

(defn current-use-case
  [db]
  (-> db
      use-cases
      first))

(defn- navigation-effects-for-key
  [db last-key]
  (let [path (-> db
                 use-cases
                 reverse
                 vec
                 (conj last-key))]
    (tap> {:in        :navigation-effects-for-key
           :use-cases (use-cases db)})
    (get-in navigation-effects-tree path)))

(defn start-use-case-navigation-effects
  [db]
  (navigation-effects-for-key db :on-start))

(defn finish-use-case-navigation-effects
  [db]
  (navigation-effects-for-key db :on-finish))


(rf/reg-event-fx :app/start-use-case
 (fn [{:keys [db]} [use-case-name]]
   (let [new-db (update-in db db-path-use-case-stack conj use-case-name)]
     #_(tap> {:in      :app/start-use-case
              :effects (start-use-case-navigation-effects new-db)})
     {:db new-db
      :fx (start-use-case-navigation-effects new-db)})))

(rf/reg-event-fx :app/finish-use-case
 (fn [{:keys [db]} [use-case-to-finish]]
   (let [new-db (update-in db db-path-use-case-stack rest)]
     (if (= (current-use-case db) use-case-to-finish)
       {:db new-db
        :fx (finish-use-case-navigation-effects db)}
       ;; TODO: kozieiev: replace with effect
       (log/error "Attempt to finish use case that is not current:" use-case-to-finish)))))

(rf/reg-event-fx :app/clear-use-cases-stack
 (fn [{:keys [db]}]
   {:db (assoc-in db db-path-use-case-stack '())}))

(comment
  (rf/dispatch [:app/start-use-case :uc-save-address])
  (rf/dispatch [:app/start-use-case :uc-edit-address])
  (rf/dispatch [:app/finish-use-case :uc-edit-address])
  (rf/dispatch [:app/finish-use-case :uc-save-address])


  (rf/dispatch [:app/clear-use-cases-stack])

  (rf/sub [:app/use-case-active? :uc-edit-address])
  (rf/sub [:app/use-case-active? :uc-save-address])
  (rf/sub [:app/current-use-case])
  (rf/dispatch [:navigate-back])

  (start-use-case-navigation-effects {:app {:use-cases-stack
                                            '(:uc-view-saved-addresses :uc-add-saved-addresses)}})
  (finish-use-case-navigation-effects {:app {:use-cases-stack
                                             '(:uc-view-saved-addresses :uc-add-saved-addresses)}})

  (use-cases {:app {:use-cases-stack
                    '(:uc-view-saved-addresses :uc-add-saved-addresses)}})

  (-> {:app {:use-cases-stack '(:uc-view-saved-addresses :uc-add-saved-addresses)}}
      use-cases
      reverse
      vec
      (conj :on-exit))


)

#_(rf/reg-event-fx
   :app/notify-user
   (fn [{:keys [db]} [{:keys [text type] :as message}]]
     (let [notification-id (-> db
                               (get-in [:app :last-user-notification :id] 0)
                               inc)]
       {;; whenever we need to publish notification we override the old and increment id
        :db (assoc-in db
             [:app :last-user-notification]
             (merge message
                    {:id notification-id}))
        ;; TODO: toasts are part of ui layer and they shouldn't be published here. Instead some part
        ;; of ui should keep track of last user notification and generate toast
        :fx [#_[:dispatch
                [:toasts/upsert
                 {:type type
                  :text text}]]]})))

#_(comment
    (rf/dispatch [:app/notify-user
                  {:type :positive
                   :text "This is a good news"}])
    (rf/dispatch [:app/notify-user
                  {:type :negative
                   :text "This is a bad news"}])

    (rf/dispatch [:toasts/upsert
                  {:type :positive
                   :text "This is a test notification3"}])
  )
