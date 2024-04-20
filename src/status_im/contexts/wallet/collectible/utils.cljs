(ns status-im.contexts.wallet.collectible.utils)

(defn collectible-balance
  [collectible]
  (-> collectible
      :ownership
      first
      :balance
      js/parseInt))

(def ^:const supported-collectible-types
  #{"image/jpeg"
    "image/gif"
    "image/bmp"
    "image/png"
    "image/webp"})

(defn supported-file?
  [collectible-type]
  (if (supported-collectible-types collectible-type)
    true
    (do
      (println "unsupoorted collectible file type" collectible-type)
      false)))

(defn total-owned-collectible
  ([ownership]
   (total-owned-collectible ownership false))
  ([ownership address]
   (reduce (fn [acc item]
             (if (or (not address) (= (:address item) address))
               (+ acc (js/parseInt (:balance item)))
               acc))
           0
           ownership)))

(defn collectible-owned-counter
  [total]
  (when (> total 1) (str "x" total)))
