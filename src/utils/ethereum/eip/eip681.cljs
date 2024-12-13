(ns utils.ethereum.eip.eip681
  "Utility function related to [EIP681](https://eips.ethereum.org/EIPS/eip-681)

   This EIP standardize how ethereum payment request can be represented as URI (say to embed them in a QR code).

   e.g. ethereum:0x1234@1/transfer?to=0x5678&value=1e18&gas=5000"
  (:require
    [clojure.string :as string]
    [native-module.core :as native-module]
    [utils.ens.core :as utils.ens]
    [utils.ethereum.chain :as chain]))

(def scheme "ethereum")
(def scheme-separator ":")
(def chain-id-separator "@")
(def function-name-separator "/")
(def query-separator "?")
(def parameter-separator "&")
(def key-value-separator "=")

(defn- address?
  [address]
  (native-module/address? address))

(def uri-pattern
  (re-pattern (str scheme scheme-separator "([^" query-separator "]*)(?:\\" query-separator "(.*))?")))
(def authority-path-pattern
  (re-pattern (str "^([^"
                   chain-id-separator
                   function-name-separator
                   "]*)(?:"
                   chain-id-separator
                   "(\\d+))?(?:"
                   function-name-separator
                   "(\\w*))?")))
(def key-value-format (str "([^" parameter-separator key-value-separator "]+)"))
(def query-pattern (re-pattern (str key-value-format key-value-separator key-value-format)))

(def valid-native-arguments #{:value :gas :gasPrice :gasLimit})

(defn- parse-query
  [s]
  (into {}
        (for [[_ k v] (re-seq query-pattern (or s ""))]
          [(keyword k) v])))

(defn- parse-native-arguments
  [m]
  (select-keys m valid-native-arguments))

(defn- parse-arguments
  [function-name s]
  (let [m         (parse-query s)
        arguments (parse-native-arguments m)]
    (if function-name
      (merge arguments
             {:function-name function-name}
             (when (seq m)
               {:function-arguments (apply dissoc m valid-native-arguments)}))
      arguments)))

(defn parse-uri
  "Parse a EIP 681 URI as a map (keyword / strings). Parsed map will contain at least the key `address`
   which will be either a valid ENS or Ethereum address.
   Note that values are not decoded and you might need to rely on specific methods for some fields
   (parse-value, parse-number).
   Invalid URI will be parsed as `nil`."
  [s]
  (when (string? s)
    (if (address? s)
      {:address s}
      (let [[_ authority-path query] (re-find uri-pattern s)]
        (when authority-path
          (let [[_ raw-address chain-id function-name] (re-find authority-path-pattern authority-path)]
            (when (or (or (utils.ens/is-valid-eth-name? raw-address) (address? raw-address))
                      (when (string/starts-with? raw-address "pay-")
                        (let [pay-address (string/replace-first raw-address "pay-" "")]
                          (or (utils.ens/is-valid-eth-name? pay-address)
                              (address? pay-address)))))
              (let [address (if (string/starts-with? raw-address "pay-")
                              (string/replace-first raw-address "pay-" "")
                              raw-address)]
                (when-let [arguments (parse-arguments function-name query)]
                  (let [contract-address (get-in arguments [:function-arguments :address])]
                    (if-not (or (not contract-address)
                                (or (utils.ens/is-valid-eth-name? contract-address)
                                    (address? contract-address)))
                      nil
                      (merge {:address  address
                              :chain-id (if chain-id
                                          (js/parseInt chain-id)
                                          (chain/chain-keyword->chain-id :mainnet))}
                             arguments))))))))))))

(defn- generate-query-string
  [m]
  (string/join parameter-separator
               (for [[k v] m]
                 (str (name k) key-value-separator v))))

(defn generate-uri
  "Generate a EIP 681 URI based on `address` and a map (keywords / {bignumbers/strings} ) of extra properties.
   No validation of address format is performed."
  [address {:keys [chain-id function-name function-arguments] :as m}]
  (when (address? address)
    (let [parameters (dissoc (into {} (filter second m)) :chain-id)] ;; filter nil values
      (str scheme
           scheme-separator
           address
           (when (and chain-id (not= chain-id (chain/chain-keyword->chain-id :mainnet)))
             ;; Add chain-id if specified and is not main-net
             (str chain-id-separator chain-id))
           (when-not (empty? parameters)
             (if function-name
               (str function-name-separator
                    function-name
                    query-separator
                    (let [native-parameters (dissoc parameters :function-name :function-arguments)]
                      (generate-query-string (merge function-arguments native-parameters))))
               (str query-separator (generate-query-string parameters))))))))
