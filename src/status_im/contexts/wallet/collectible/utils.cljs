(ns status-im.contexts.wallet.collectible.utils)

(defn collectible-balance
  [collectible]
  (-> collectible
      :ownership
      first
      :balance
      js/parseInt))

(def ^:private supported-collectible-types
  #{"image/jpeg"
    "image/gif"
    "image/bmp"
    "image/png"
    "image/webp"})

(defn collectible-supported?
  [type]
  (some #{type} supported-collectible-types))
