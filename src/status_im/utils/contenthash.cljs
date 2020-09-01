(ns status-im.utils.contenthash
  "TODO: currently we only support encoding/decoding ipfs contenthash
  implementing swarm and other protocols will come later"
  (:refer-clojure :exclude [cat])
  (:require ["hi-base32" :as hi-base32]
            [alphabase.hex :as hex]
            [clojure.string :as string]
            [status-im.ethereum.core :as ethereum]))

(defn decode
  "TODO properly decode the CID
   extract the content-type using varint ns"
  [hex]
  (when (and hex (not= hex "0x"))
    (cond
      (and (string/starts-with? hex "0xe40101"))
      ;; content type can be 2 or 4 bytes
      ;; we expect 1b20 (hash algorithm keccak256 and hash length 64)
      ;; before the hash so we split the contenthash there
      (when-let [hash (second (string/split hex "1b20"))]
        {:namespace :swarm
         :hash hash})
      (and (= 78 (count hex))
           (string/starts-with? hex "0xe3010170"))
      {:namespace :ipfs
       :hash (str "b" (-> hex
                          (subs 6)
                          hex/decode
                          ((fn [v] (.encode ^js hi-base32 v)))
                          (string/replace #"=" "")
                          string/lower-case))}
      (and (string/starts-with? hex "0xe50101700"))
      {:namespace :ipns
       :hash (-> hex
                 (subs 14)
                 ((fn [v] (str "0x" v)))
                 (ethereum/hex-to-utf8))})))

(defn ipfs-url [hash]
  (str "https://" hash ".ipfs.cf-ipfs.com"))

(defn url-fn [hex]
  (let [{:keys [namespace hash]} (decode (ethereum/normalized-hex hex))]
    (case namespace
      :ipfs (ipfs-url hash)
      "")))

(def url (memoize url-fn))
