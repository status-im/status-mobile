(ns status-im2.contexts.emoji-hash.events
  (:require [utils.re-frame :as rf]
            [status-im.native-module.core :as native-module]
            [utils.transforms :as transform]))

(defn fetch-for-current-public-key
  []
  (let [public-key (rf/sub [:multiaccount/public-key])]
    (native-module/public-key->emoji-hash
     public-key
     (fn [response]
       (let [response-clj (transform/json->clj response)
             emoji-hash   (get response-clj :result)]
         (rf/dispatch [:emoji-hash/add-to-multiaccount emoji-hash]))))))

(rf/defn add-emoji-hash-to-multiaccount
  {:events [:emoji-hash/add-to-multiaccount]}
  [{:keys [db]} emoji-hash]
  {:db (assoc db :multiaccount/emoji-hash emoji-hash)})
