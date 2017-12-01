(ns status-im.model
  (:require #?@(:clj  [[clojure.spec.alpha :as s]]
                :cljs [[cljs.spec.alpha :as s]])
                       [clojure.string :as string]
                       [clojure.test.check.generators :as gen]))

(s/def ::id string?)

;; chat

(s/def ::chat.name string?)

(s/def ::chat (s/keys :req [::id
                            ::chat.name]))

;; contact

(s/def ::contact.whisper-identity (s/and string? #(not (string/blank? %))))
(s/def ::contact.address string?)
(s/def ::contact.name string?)
(s/def ::contact.photo-path string?)
(s/def ::contact.dapp? boolean?)

(s/def ::contact (s/keys :req [::contact.whisper-identity
                               ::contact.address
                               ::contact.name
                               ::contact.photo-path
                               ::contact.dapp?]))

;; model fns

(defn model->current-model [model-type model]
  (let [[t & {:keys [req opt]}] (s/describe (s/get-spec model-type))]
    (->> (concat req opt)
         (map (fn [k]
                (let [current-k (-> k name (string/replace #"^.*\." "") keyword)]
                  [current-k (get model k)])))
         (into {}))))
