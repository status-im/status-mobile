(ns status-im.extensions.core
  (:refer-clojure :exclude [list])
  (:require [clojure.string :as string]
            [pluto.core :as pluto]
            [re-frame.core :as re-frame]
            [re-frame.registrar :as registrar]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.extensions.capacities.components :as components]
            [status-im.extensions.capacities.subs :as subs]
            [status-im.extensions.capacities.events :as events]
            [status-im.extensions.constants :as constants]))

(def capacities
  {:components components/all
   :queries    subs/all
   :events     events/all
   :hooks      {:profile.settings
                {:properties
                 {:label     :string
                  :view      :view
                  :on-open?  :event
                  :on-close? :event}}
                :wallet.settings
                {:properties
                 {:label     :string
                  :view      :view
                  :on-open?  :event
                  :on-close? :event}}
                :chat.command
                {:properties
                 {:description?   :string
                  :scope          #{:personal-chats :public-chats :group-chats}
                  :short-preview? :view
                  :preview?       :view
                  :on-send?       :event
                  :on-receive?    :event
                  :on-send-sync?  :event
                  :parameters?    [{:id           :keyword
                                    :type         {:one-of #{:text :phone :password :number}}
                                    :placeholder  :string
                                    :suggestions? :view}]}}}})

(defn dispatch-events [_ events]
  (doseq [event events]
    (when (vector? event)
      (re-frame/dispatch event))))

(defn resolve-query [[id :as data]]
  (when (registrar/get-handler :sub id)
    (re-frame/subscribe data)))

(defn parse [{:keys [data]} id]
  (try
    (pluto/parse {:capacities capacities
                  :env        {:id id}
                  :event-fn   dispatch-events
                  :query-fn   resolve-query}
                 data)
    (catch :default e {:errors [{:value (str e)}]})))

(defn parse-extension [{:keys [type value]} id]
  (if (= type :success)
    (parse (pluto/read (:content value)) id)
    {:errors [{:type type :value value}]}))

(defn valid-uri? [s]
  (boolean
   (when s
     (let [s' (string/trim s)]
       (or
        (re-matches (re-pattern (str "^" constants/uri-prefix "\\w+@.+")) s')
        (re-matches (re-pattern (str "^" constants/link-prefix "\\w+@.+")) s'))))))

(fx/defn set-extension-url-from-qr
  [cofx url]
  (fx/merge (assoc-in cofx [:db :extensions/manage :url] {:value url
                                                          :error false})
            (navigation/navigate-back)))

(fx/defn set-input
  [{:keys [db]} input-key value]
  {:db (update db :extensions/manage assoc input-key {:value value})})

(fx/defn fetch [cofx ext-key]
  (get-in cofx [:db :account/account :extensions ext-key]))

(fx/defn edit
  [cofx extension-key]
  (let [{:keys [url]} (fetch cofx extension-key)]
    (fx/merge (set-input cofx :url (str url))
              (navigation/navigate-to-cofx :edit-extension nil))))
