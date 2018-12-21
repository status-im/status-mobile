(ns status-im.extensions.registry
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as string]
            [pluto.reader.hooks :as hooks]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]
            [clojure.set :as set]
            [status-im.ui.screens.navigation :as navigation]))

(fx/defn update-hooks
  [{:keys [db] :as cofx} hook-fn extension-id]
  (let [account (get db :account/account)
        hooks   (get-in account [:extensions extension-id :hooks])]
    (apply fx/merge cofx
           (mapcat (fn [[_ extension-hooks]]
                     (map (fn [[hook-id {:keys [hook-ref parsed]}]]
                            (partial hook-fn (:hook hook-ref) hook-id {:id extension-id} parsed))
                          extension-hooks))
                   hooks))))

(fx/defn add-to-registry
  [{:keys [db] :as cofx} extension-id extension-data active?]
  (let [{:keys [hooks]} extension-data
        data {:hooks   hooks
              :active? active?}]
    (fx/merge cofx
              {:db (update-in db [:account/account :extensions extension-id] merge data)}
              (update-hooks hooks/hook-in extension-id))))

(fx/defn remove-from-registry
  [cofx extension-id]
  (let [extensions (get-in cofx [:db :account/account :extensions])]
    (fx/merge cofx
              (when (get-in extensions [extension-id :active?])
                (update-hooks hooks/unhook extension-id))
              {:db (update-in cofx [:db :account/account :extensions] dissoc extension-id)})))

(fx/defn change-state
  [cofx extension-key active?]
  (let [extensions     (get-in cofx [:db :account/account :extensions])
        new-extensions (assoc-in extensions [extension-key :active?] active?)
        hook-fn        (if active?
                         hooks/hook-in
                         hooks/unhook)]
    (fx/merge cofx
              (accounts.update/account-update {:extensions new-extensions} {:success-event nil})
              (update-hooks hook-fn extension-key))))

(fx/defn install
  [{:keys [db] :as cofx} url {:keys [hooks] :as extension-data} modal?]
  (let [{:account/keys    [account]} db
        extension      {:id      url
                        :name    (get-in extension-data ['meta :name])
                        :url     url
                        :active? true}
        new-extensions (assoc (:extensions account) url extension)]
    (fx/merge cofx
              {:utils/show-popup {:title      (i18n/label :t/success)
                                  :content    (i18n/label :t/extension-installed)
                                  :on-dismiss #(re-frame/dispatch (if modal?
                                                                    [:navigate-back]
                                                                    [:navigate-to-clean :my-profile]))}}
              (when hooks (accounts.update/account-update {:extensions new-extensions} {}))
              (when hooks (add-to-registry url extension-data true)))))

(fx/defn uninstall
  [{:keys [db] :as cofx} extension-key]
  (let [{:account/keys [account]} db
        new-extensions (dissoc (:extensions account) extension-key)]
    (fx/merge cofx
              {:utils/show-popup {:title     (i18n/label :t/success)
                                  :content   (i18n/label :t/extension-uninstalled)}}
              (remove-from-registry extension-key)
              (accounts.update/account-update {:extensions new-extensions} {}))))

(fx/defn load
  [cofx url modal?]
  (if (get-in cofx [:db :account/account :extensions url])
    {:utils/show-popup {:title   (i18n/label :t/error)
                        :content (i18n/label :t/extension-is-already-added)}}
    {:extensions/load {:extensions [{:url     (string/trim url)
                                     :active? true}]
                       :follow-up  (if modal? :extensions/stage-modal :extensions/stage)}}))

(fx/defn initialize
  [{{:account/keys [account]} :db}]
  (let [{:keys [extensions dev-mode?]} account]
    (when dev-mode?
      {:extensions/load {:extensions (vals extensions)
                         :follow-up  :extensions/add-to-registry}})))

(defn existing-hooks-for
  [type cofx extension-data]
  (let [added-hooks (reduce (fn [acc [_ {:keys [hooks]}]]
                              (->> (type hooks)
                                   (keys)
                                   (into acc)))
                            #{}
                            (get-in cofx [:db :account/account :extensions]))
        hooks       (->> (get-in extension-data [:data :hooks type])
                         (keys)
                         (into #{}))]
    (set/intersection added-hooks hooks)))

(defn existing-hooks [cofx extension-data]
  (->> (get-in extension-data [:data :hooks])
       (keys)
       (map #(existing-hooks-for % cofx extension-data))
       (apply set/union)))

(fx/defn stage-extension [{:keys [db] :as cofx} url extension-data modal?]
  (let [hooks (existing-hooks cofx extension-data)]
    (if (empty? hooks)
      (fx/merge cofx
                {:db (assoc db :extensions/staged-extension {:url            url
                                                             :extension-data extension-data})}
                (navigation/navigate-to-cofx (if modal? :show-extension-modal :show-extension) nil))
      {:utils/show-popup {:title   (i18n/label :t/error)
                          :content (i18n/label :t/extension-hooks-cannot-be-added
                                               {:hooks (->> hooks
                                                            (map name)
                                                            (clojure.string/join ", "))})}})))
