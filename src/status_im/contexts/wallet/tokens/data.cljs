(ns status-im.contexts.wallet.tokens.data)

(defn- tokens-by-key
  [{:keys [key-fn added-tokens source-name tokens chain-ids]}]
  (reduce
   (fn [acc {:keys [address chainId decimals name image verified] :as token}]
     (if (some #{chainId} chain-ids)
       (let [k (key-fn token)]
         (assoc acc
                k
                {:key          k
                 :name         name
                 :symbol       (:symbol token)
                 :sources      (if-let [added-token (get added-tokens k)]
                                 (conj (:sources added-token) source-name)
                                 [source-name])
                 :chain-id     chainId
                 :address      address
                 :decimals     decimals
                 :image        image
                 :type         (if (= name "native") :native :erc20)
                 :community-id (get-in token [:communityData :id])
                 :verified?    verified}))
       acc))
   {}
   tokens))

(defn tokens-by-address
  [props]
  (tokens-by-key (assoc props
                        :key-fn
                        (fn [{:keys [chainId address]}]
                          (str chainId "-" address)))))

(defn tokens-by-symbol
  [props]
  (tokens-by-key
   (assoc props
          :key-fn
          (fn [{:keys [chainId address] :as token}]
            (str chainId "-" (if (get-in token [:communityData :id]) address (:symbol token)))))))
