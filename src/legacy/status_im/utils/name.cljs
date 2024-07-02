(ns legacy.status-im.utils.name
  (:require
    [clojure.string :as string]))

(defn too-long?
  [name max-len]
  (> (count name) max-len))

(defn max-name
  [name max-len]
  (let [names (string/split name " ")]
    (first
     (reduce (fn [[name done] next-name]
               (if done
                 name
                 (let [new-name (string/join " " [name next-name])]
                   (if (too-long? new-name max-len)
                     (let [new-name' (str name " " (first next-name) ".")]
                       (if (too-long? new-name' max-len)
                         [name true]
                         [new-name' true]))
                     [new-name]))))
             [(first names)]
             (rest names)))))

(defn shortened-name
  [name max-len]
  (if (> (count name) max-len)
    (let [name' (max-name name max-len)]
      (if (too-long? name' max-len)
        (str (string/trim (subs name 0 max-len)) "...")
        name'))
    name))
