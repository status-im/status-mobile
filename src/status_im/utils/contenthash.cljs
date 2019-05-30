(ns status-im.utils.contenthash
  "TODO: currently we only support encoding/decoding ipfs contenthash
  implementing swarm and other protocols will come later"
  (:refer-clojure :exclude [cat])
  (:require [alphabase.base58 :as b58]
            [alphabase.hex :as hex]
            [clojure.string :as string]
            [status-im.ipfs.core :as ipfs]
            [status-im.utils.fx :as fx]))

(defn encode [{:keys [hash namespace ipld]}]
  (when (and hash
             (= namespace :ipfs)
             (nil? ipld))
    (when-let [b58-hash (if (string/starts-with? hash "z")
                          (when (= (count hash) 49)
                            ;; this is a CID multihash
                            ;; the z is removed, it indicates that the
                            ;; CID is b58 encoded
                            (subs hash 1))
                          (when (= (count hash) 46)
                            ;; this is a deprecated simple ipfs hash
                            hash))]
      (str "0xe301" (hex/encode (b58/decode b58-hash))))))

(defn decode
  "TODO properly decode the CID
   extract the content-type using varint ns"
  [hex]
  (when (and hex (not= hex "0x"))
    (cond
      (and (string/starts-with? hex "0xe40101"))
      ;; content type can be 2 or 4 bytes
      ;; we expect 1b20 (hash algorithm and hash length 64)
      ;; before the hash so we split the contenthash there
      (when-let [hash (second (string/split hex "1b20"))]
        (when (= 0x20 (count hash)))
        {:namespace :swarm
         :hash hash})
      (and (= 78 (count hex))
           (string/starts-with? hex "0xe3010170"))
      ;; for retrocompatibility with old CIDv0
      {:namespace :ipfs
       :hash (-> hex
                 (subs 10)
                 hex/decode
                 b58/encode)}
      (and (= 78 (count hex))
           (string/starts-with? hex "0xe30101"))
      ;; new CIDv1
      {:namespace :ipfs
       :hash  (str "z" (-> hex
                           (subs 6)
                           hex/decode
                           b58/encode))})))

(fx/defn cat
  [cofx {:keys [contenthash on-success on-failure]}]
  (let [{:keys [namespace hash]} (decode contenthash)]
    (when (= namespace :ipfs)
      (ipfs/cat cofx
                {:hash hash
                 :on-success on-success
                 :on-failure on-failure}))))
