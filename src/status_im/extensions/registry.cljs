(ns status-im.extensions.registry
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as string]
            [pluto.utils :as utils]
            [pluto.reader.hooks :as hooks]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.i18n :as i18n]
            [status-im.utils.fx :as fx]
            [clojure.set :as set]))

(fx/defn update-hooks
  [{:keys [db] :as cofx} hook-fn extension-key]
  (let [account (get db :account/account)
        hooks   (get-in account [:extensions extension-key :hooks])]
    (apply utils/merge-fx cofx
           (mapcat (fn [[_ extension-hooks]]
                     (map (fn [[hook-id {:keys [hook-ref parsed]}]]
                            (partial hook-fn (:hook hook-ref) hook-id parsed))
                          extension-hooks))
                   hooks))))

(fx/defn add-to-registry
  [{:keys [db] :as cofx} extension-key extension-data active?]
  (let [{:keys [hooks]} extension-data
        data {:hooks   hooks
              :active? active?}]
    (fx/merge cofx
              {:db (update-in db [:account/account :extensions extension-key] merge data)}
              (update-hooks hooks/hook-in extension-key))))

(fx/defn remove-from-registry
  [{:keys [db] :as cofx} extension-key]
  (fx/merge cofx
            (update-hooks hooks/unhook extension-key)
            {:db (update-in db [:account/account :extensions] dissoc extension-key)}))

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
  [{:keys [db random-id-generator] :as cofx} extension-data]
  (let [{:extensions/keys [manage]
         :account/keys    [account]} db
        {:keys [id url]} manage
        extension      {:id      (-> (:value id)
                                     (or (random-id-generator))
                                     (string/replace "-" ""))
                        :name    (get-in extension-data ['meta :name])
                        :url     (:value url)
                        :active? true}
        new-extensions (assoc (:extensions account) (:url extension) extension)]
    (fx/merge cofx
              {:ui/show-confirmation {:title     (i18n/label :t/success)
                                      :content   (i18n/label :t/extension-installed)
                                      :on-accept #(re-frame/dispatch [:navigate-to-clean :my-profile])
                                      :on-cancel nil}}
              (accounts.update/account-update {:extensions new-extensions} {})
              (add-to-registry (:value url) extension-data true))))

(fx/defn uninstall
  [{:keys [db] :as cofx} extension-key]
  (let [{:account/keys [account]} db
        new-extensions (dissoc (:extensions account) extension-key)]
    (fx/merge cofx
              {:ui/show-confirmation {:title     (i18n/label :t/success)
                                      :content   (i18n/label :t/extension-uninstalled)
                                      :on-accept nil
                                      :on-cancel nil}}
              (remove-from-registry extension-key)
              (accounts.update/account-update {:extensions new-extensions} {}))))

(fx/defn load
  [cofx url]
  (if (get-in cofx [:db :account/account :extensions url])
    {:utils/show-popup {:title   (i18n/label :t/error)
                        :content (i18n/label :t/extension-is-already-added)}}
    {:extensions/load {:extensions [{:url     (string/trim url)
                                     :active? true}]
                       :follow-up  :extensions/stage}}))

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