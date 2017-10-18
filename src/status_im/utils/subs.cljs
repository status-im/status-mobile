(ns status-im.utils.subs)

(defn contains-sub
  "Creates subscrition that cheks if collection (map or set) contains element"
  [collection]
  (fn [db [_ element]]
    (-> (collection db)
        (contains? element))))
