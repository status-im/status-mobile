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
             (= 46 (count hash))
             (nil? ipld))
    (str "0xe301" (hex/encode (b58/decode hash)))))

(defn decode [hex]
  (when (and hex (not= hex "0x")
             (string/starts-with? hex "0xe30101")
             (= 78 (count hex)))
    {:namespace :ipfs
     :hash  (-> hex
                (subs 10)
                hex/decode
                b58/encode)}))

(fx/defn cat
  [cofx {:keys [contenthash on-success on-failure]}]
  (let [{:keys [namespace hash]} (decode contenthash)]
    (when (= namespace :ipfs)
      (ipfs/cat cofx
                {:hash hash
                 :on-success on-success
                 :on-failure on-failure}))))
