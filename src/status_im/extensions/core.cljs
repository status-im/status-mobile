(ns status-im.extensions.core
  (:require [clojure.string :as string]
            [pluto.reader :as reader]
            [pluto.registry :as registry]
            [pluto.storages :as storages]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.ui.components.react :as react]))

(def components
  {'view           {:value react/view}
   'text           {:value react/text}
   'nft-token      {:value transactions/nft-token}
   'send-status    {:value transactions/send-status}
   'asset-selector {:value transactions/choose-nft-asset-suggestion}
   'token-selector {:value transactions/choose-nft-token-suggestion}})

(def app-hooks #{commands/command-hook})

(def capacities
  (reduce (fn [capacities hook]
            (assoc-in capacities [:hooks :commands] hook))
          {:components components
           :queries    {'get-collectible-token {:value :get-collectible-token}}
           :events     {}}
          app-hooks))

(defn read-extension [o]
  (-> o :value first :content reader/read))

(defn parse [{:keys [data] :as m}]
  (try
    (let [{:keys [errors] :as extension-data} (reader/parse {:capacities capacities} data)]
      (when errors
        (println "Failed to parse status extensions" errors))
      extension-data)
    (catch :default e (println "EXC" e))))

(defn url->uri [s]
  (when s
    (string/replace s "https://get.status.im/extension/" "")))

(defn load-from [url f]
  (when-let [uri (url->uri url)]
    (storages/fetch uri f)))
