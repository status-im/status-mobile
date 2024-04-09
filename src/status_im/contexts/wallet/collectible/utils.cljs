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
  (if (supported-collectible-types collectible-type) true false))

(defn total-owned-collectible
  [ownership]
  (reduce (fn [acc item] (+ acc (js/parseInt (:balance item)))) 0 ownership))

(defn total-owned-collectible-by-address
  [ownership address]
  (reduce (fn [acc item]
            (if (= (:address item) address)
              (+ acc (js/parseInt (:balance item)))
              acc))
          0
          ownership))

(defn calculate-owned-collectible
  [ownership address]
  (if address
    (total-owned-collectible-by-address ownership address)
    (total-owned-collectible ownership)))

(defn collectible-owned-counter
  [total]
  (when (> total 1) total))
