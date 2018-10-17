(ns status-im.utils.contacts
  (:require [status-im.js-dependencies :as js-dependencies]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.ethereum.core :as ethereum]))

(defn whisper-id->new-contact [whisper-id]
  {:name             (gfycat/generate-gfy whisper-id)
   :photo-path       (identicon/identicon whisper-id)
   :whisper-identity whisper-id})

(defn public-key->address [public-key]
  (let [length (count public-key)
        normalized-key (case length
                         132 (subs public-key 4)
                         130 (subs public-key 2)
                         128 public-key
                         nil)]
    (when normalized-key
      (subs (.sha3 js-dependencies/Web3.prototype normalized-key #js {:encoding "hex"}) 26))))

(defn- contact-by-address [[_ contact] address]
  (when (ethereum/address= (:address contact) address)
    contact))

(defn find-contact-by-address [contacts address]
  (some #(contact-by-address % address) contacts))
