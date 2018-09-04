(ns status-im.extensions.registry
  (:require [pluto.reader :as reader]
            [pluto.registry :as registry]
            [pluto.host :as host]
            [pluto.storage :as storage]
            [pluto.storage.gist :as gist]
            [status-im.extensions.core :as extension]
            [status-im.chat.commands.core :as commands]
            [status-im.chat.commands.impl.transactions :as transactions]
            [status-im.ui.components.react :as react]))

(def components
  {'view           react/view
   'text           react/text
   'nft-token      transactions/nft-token
   'send-status    transactions/send-status
   'asset-selector transactions/choose-nft-asset-suggestion
   'token-selector transactions/choose-nft-token-suggestion})

(def functions
  {'transfer-nft-token transactions/transfer-nft-token})

(def app-hooks #{commands/command-hook})

(def capacities
  (reduce (fn [capacities hook]
            (assoc-in capacities [:hooks (host/id hook)] hook))
          {:components    components
           :queries       #{:get-in :get-collectible-token}
           :events        #{:set-in}
           :functions     functions
           :permissions   {:read  {:include-paths #{[:chats #".*"]}}
                           :write {:include-paths #{}}}}
          app-hooks))

(defn parse [{:keys [data]}]
  (try
    (let [{:keys [errors] :as extension-data} (reader/parse {:capacities capacities} data)]
      (when errors
        (println "Failed to parse status extensions" errors))
      extension-data)
    (catch :default e (println "EXC" e))))

(def storages
  {:gist (gist/GistStorage.)})

(defn read-extension [o]
  (-> o :value first :content reader/read))

(defn load-from [url f]
  (let [[type id] (extension/url->storage-details url)
        storage   (get storages type)]
    (when (and storage id)
      (storage/fetch storage
                     {:value id}
                     #(f %)))))
