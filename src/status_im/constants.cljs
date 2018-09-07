(ns status-im.constants
  (:require [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.types :as types]
            [status-im.utils.config :as config]))

(def ethereum-rpc-url "http://localhost:8545")

(def text-content-type "text/plain")
(def content-type-log-message "log-message")
(def content-type-command "command")
(def content-type-command-request "command-request")
(def content-type-status "status")
(def content-type-placeholder "placeholder")
(def content-type-emoji "emoji")

(def desktop-content-types
  #{text-content-type content-type-emoji})

(def command-send "send")
(def command-request "request")
(def command-send-status-update-interval-ms 60000)

(def min-password-length 6)
(def max-chat-name-length 20)
(def response-suggesstion-resize-duration 100)
(def default-number-of-messages 20)
(def blocks-per-hour 120)

(def console-chat-id "console")

(def inbox-password "status-offline-inbox")

(def default-network config/default-network)

(def system "system")

(def default-wallet-transactions
  {:filters
   {:type [{:id :inbound   :label (i18n/label :t/incoming)  :checked? true}
           {:id :outbound  :label (i18n/label :t/outgoing)  :checked? true}
           {:id :pending   :label (i18n/label :t/pending)   :checked? true}
           {:id :failed    :label (i18n/label :t/failed)    :checked? true}]}})

(def mainnet-networks
  {"mainnet"     {:id     "mainnet",
                  :name   "Mainnet",
                  :config {:NetworkId (ethereum/chain-keyword->chain-id :mainnet)
                           :DataDir   "/ethereum/mainnet"}}
   "mainnet_rpc" {:id     "mainnet_rpc",
                  :name   "Mainnet with upstream RPC",
                  :config {:NetworkId      (ethereum/chain-keyword->chain-id :mainnet)
                           :DataDir        "/ethereum/mainnet_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://mainnet.infura.io/z6GCTmjdP3FETEJmMBI4"}}}})

(def testnet-networks
  {"testnet"     {:id     "testnet",
                  :name   "Ropsten",
                  :config {:NetworkId (ethereum/chain-keyword->chain-id :testnet)
                           :DataDir   "/ethereum/testnet"}}
   "testnet_rpc" {:id     "testnet_rpc",
                  :name   "Ropsten with upstream RPC",
                  :config {:NetworkId      (ethereum/chain-keyword->chain-id :testnet)
                           :DataDir        "/ethereum/testnet_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://ropsten.infura.io/z6GCTmjdP3FETEJmMBI4"}}}
   "rinkeby"     {:id     "rinkeby",
                  :name   "Rinkeby",
                  :config {:NetworkId (ethereum/chain-keyword->chain-id :rinkeby)
                           :DataDir   "/ethereum/rinkeby"}}
   "rinkeby_rpc" {:id     "rinkeby_rpc",
                  :name   "Rinkeby with upstream RPC",
                  :config {:NetworkId      (ethereum/chain-keyword->chain-id :rinkeby)
                           :DataDir        "/ethereum/rinkeby_rpc"
                           :UpstreamConfig {:Enabled true
                                            :URL     "https://rinkeby.infura.io/z6GCTmjdP3FETEJmMBI4"}}}})

(defn network-enabled? [network]
  (if config/rpc-networks-only?
    (get-in (val network) [:config :UpstreamConfig :Enabled])
    true))

(def default-networks
  (into {} (filter network-enabled?
                   (merge testnet-networks mainnet-networks))))

(def default-wnodes-without-custom
  {"eth.beta" {"mailserver-a" {:id      "mailserver-a" ;mail-01.do-ams3.eth.beta
                               :name    "Status mailserver A"
                               :password inbox-password
                               :address "enode://c42f368a23fa98ee546fd247220759062323249ef657d26d357a777443aec04db1b29a3a22ef3e7c548e18493ddaf51a31b0aed6079bd6ebe5ae838fcfaf3a49@206.189.243.162:30504"}
               "mailserver-b" {:id      "mailserver-b" ;mail-02.do-ams3.eth.beta
                               :name    "Status mailserver B"
                               :password inbox-password
                               :address "enode://7aa648d6e855950b2e3d3bf220c496e0cae4adfddef3e1e6062e6b177aec93bc6cdcf1282cb40d1656932ebfdd565729da440368d7c4da7dbd4d004b1ac02bf8@206.189.243.169:30504"}
               "mailserver-c" {:id      "mailserver-c" ;mail-03.do-ams3.eth.beta
                               :name    "Status mailserver C"
                               :password inbox-password
                               :address "enode://8a64b3c349a2e0ef4a32ea49609ed6eb3364be1110253c20adc17a3cebbc39a219e5d3e13b151c0eee5d8e0f9a8ba2cd026014e67b41a4ab7d1d5dd67ca27427@206.189.243.168:30504"}
               "mailserver-d" {:id      "mailserver-d" ;mail-01.gc-us-central1-a.eth.beta
                               :name    "Status mailserver D"
                               :password inbox-password
                               :address "enode://7de99e4cb1b3523bd26ca212369540646607c721ad4f3e5c821ed9148150ce6ce2e72631723002210fac1fd52dfa8bbdf3555e05379af79515e1179da37cc3db@35.188.19.210:30504"}
               "mailserver-e" {:id      "mailserver-e" ;mail-02.gc-us-central1-a.eth.beta
                               :name    "Status mailserver E"
                               :password inbox-password
                               :address "enode://015e22f6cd2b44c8a51bd7a23555e271e0759c7d7f52432719665a74966f2da456d28e154e836bee6092b4d686fe67e331655586c57b718be3997c1629d24167@35.226.21.19:30504"}
               "mailserver-f" {:id      "mailserver-f" ;mail-03.gc-us-central1-a.eth.beta
                               :name    "Status mailserver F"
                               :password inbox-password
                               :address "enode://531e252ec966b7e83f5538c19bf1cde7381cc7949026a6e499b6e998e695751aadf26d4c98d5a4eabfb7cefd31c3c88d600a775f14ed5781520a88ecd25da3c6@35.225.227.79:30504"}}
   "eth.staging" {"mailserver-a" {:id      "mailserver-a" ;mail-01.gc-us-central1-a.eth.staging
                                  :name    "Status mailserver A"
                                  :password inbox-password
                                  :address "enode://e4fc10c1f65c8aed83ac26bc1bfb21a45cc1a8550a58077c8d2de2a0e0cd18e40fd40f7e6f7d02dc6cd06982b014ce88d6e468725ffe2c138e958788d0002a7f@35.239.193.41:30504"}
                  "mailserver-b" {:id      "mailserver-b" ;mail-01.do-ams3.eth.staging
                                  :name    "Status mailserver B"
                                  :password inbox-password
                                  :address "enode://69f72baa7f1722d111a8c9c68c39a31430e9d567695f6108f31ccb6cd8f0adff4991e7fdca8fa770e75bc8a511a87d24690cbc80e008175f40c157d6f6788d48@206.189.240.16:30504"}
                  "mailserver-c" {:id      "mailserver-c" ;mail-01.ac-cn-hongkong-c.eth.staging
                                  :name    "Status mailserver C"
                                  :password inbox-password
                                  :address "enode://b74859176c9751d314aeeffc26ec9f866a412752e7ddec91b19018a18e7cca8d637cfe2cedcb972f8eb64d816fbd5b4e89c7e8c7fd7df8a1329fa43db80b0bfe@47.75.159.188:30504"}}
   "eth.test" {"mailserver-a" {:id      "mailserver-a" ;mail-01.gc-us-central1-a.eth.test
                               :name    "Status mailserver A"
                               :password inbox-password
                               :address "enode://707e57453acd3e488c44b9d0e17975371e2f8fb67525eae5baca9b9c8e06c86cde7c794a6c2e36203bf9f56cae8b0e50f3b33c4c2b694a7baeea1754464ce4e3@35.192.229.172:30504"}
               "mailserver-b" {:id      "mailserver-b" ;mail-01.do-ams3.eth.test
                               :name    "Status mailserver B"
                               :password inbox-password
                               :address "enode://e4865fe6c2a9c1a563a6447990d8e9ce672644ae3e08277ce38ec1f1b690eef6320c07a5d60c3b629f5d4494f93d6b86a745a0bf64ab295bbf6579017adc6ed8@206.189.243.161:30504"}
               "mailserver-c" {:id      "mailserver-c" ;mail-01.ac-cn-hongkong-c.eth.test
                               :name    "Status mailserver C"
                               :password inbox-password
                               :address "enode://954c06603a6e755bffe9992615f4755848bda9aadda74d920aa31d1d8e4f6022dc556dca6768f8a0f9459f57b729509db3c8b3bb80acfbd8a2123087f6cbd7bd@47.52.251.180:30504"}}})

(def default-wnodes
  ;; We use the same set of mailservers for every network now
  ;; They are only dependent on the selected fleet (test, stage, beta)
  (let [nodes-for-fleet (get default-wnodes-without-custom config/fleet)]
    {:custom  nodes-for-fleet
     :testnet nodes-for-fleet
     :mainnet nodes-for-fleet
     :rinkeby nodes-for-fleet}))

(defn default-account-settings []
  {:wallet {:visible-tokens {:testnet #{:STT :HND}
                             :mainnet #{:SNT}
                             :rinkeby #{:MOKSHA}}}})

(def currencies
  {:aed {:id :aed :code "AED" :display-name (i18n/label :t/currency-display-name-aed) :symbol "د.إ"}
   :afn {:id :afn :code "AFN" :display-name (i18n/label :t/currency-display-name-afn) :symbol "؋"}
   :ars {:id :ars :code "ARS" :display-name (i18n/label :t/currency-display-name-ars) :symbol "$"}
   :aud {:id :aud :code "AUD" :display-name (i18n/label :t/currency-display-name-aud) :symbol "$"}
   :bbd {:id :bbd :code "BBD" :display-name (i18n/label :t/currency-display-name-bbd) :symbol "$"}
   :bdt {:id :bdt :code "BDT" :display-name (i18n/label :t/currency-display-name-bdt) :symbol " Tk"}
   :bgn {:id :bgn :code "BGN" :display-name (i18n/label :t/currency-display-name-bgn) :symbol "лв"}
   :bhd {:id :bhd :code "BHD" :display-name (i18n/label :t/currency-display-name-bhd) :symbol "BD"}
   :bnd {:id :bnd :code "BND" :display-name (i18n/label :t/currency-display-name-bnd) :symbol "$"}
   :bob {:id :bob :code "BOB" :display-name (i18n/label :t/currency-display-name-bob) :symbol "$b"}
   :brl {:id :brl :code "BRL" :display-name (i18n/label :t/currency-display-name-brl) :symbol "R$"}
   :btn {:id :btn :code "BTN" :display-name (i18n/label :t/currency-display-name-btn) :symbol "Nu."}
   :cad {:id :cad :code "CAD" :display-name (i18n/label :t/currency-display-name-cad) :symbol "$"}
   :chf {:id :chf :code "CHF" :display-name (i18n/label :t/currency-display-name-chf) :symbol "CHF"}
   :clp {:id :clp :code "CLP" :display-name (i18n/label :t/currency-display-name-clp) :symbol "$"}
   :cny {:id :cny :code "CNY" :display-name (i18n/label :t/currency-display-name-cny) :symbol "¥"}
   :cop {:id :cop :code "COP" :display-name (i18n/label :t/currency-display-name-cop) :symbol "$"}
   :crc {:id :crc :code "CRC" :display-name (i18n/label :t/currency-display-name-crc) :symbol "₡"}
   :czk {:id :czk :code "CZK" :display-name (i18n/label :t/currency-display-name-czk) :symbol "Kč"}
   :dkk {:id :dkk :code "DKK" :display-name (i18n/label :t/currency-display-name-dkk) :symbol "kr"}
   :dop {:id :dop :code "DOP" :display-name (i18n/label :t/currency-display-name-dop) :symbol "RD$"}
   :egp {:id :egp :code "EGP" :display-name (i18n/label :t/currency-display-name-egp) :symbol "£"}
   :etb {:id :etb :code "ETB" :display-name (i18n/label :t/currency-display-name-etb) :symbol "Br"}
   :eur {:id :eur :code "EUR" :display-name (i18n/label :t/currency-display-name-eur) :symbol "€"}
   :gbp {:id :gbp :code "GBP" :display-name (i18n/label :t/currency-display-name-gbp) :symbol "£"}
   :gel {:id :gel :code "GEL" :display-name (i18n/label :t/currency-display-name-gel) :symbol "₾"}
   :ghs {:id :ghs :code "GHS" :display-name (i18n/label :t/currency-display-name-ghs) :symbol "¢"}
   :hkd {:id :hkd :code "HKD" :display-name (i18n/label :t/currency-display-name-hkd) :symbol "$"}
   :hrk {:id :hrk :code "HRK" :display-name (i18n/label :t/currency-display-name-hrk) :symbol "kn"}
   :huf {:id :huf :code "HUF" :display-name (i18n/label :t/currency-display-name-huf) :symbol "Ft"}
   :idr {:id :idr :code "IDR" :display-name (i18n/label :t/currency-display-name-idr) :symbol "Rp"}
   :ils {:id :ils :code "ILS" :display-name (i18n/label :t/currency-display-name-ils) :symbol "₪"}
   :inr {:id :inr :code "INR" :display-name (i18n/label :t/currency-display-name-inr) :symbol "₹"}
   :isk {:id :isk :code "ISK" :display-name (i18n/label :t/currency-display-name-isk) :symbol "kr"}
   :jmd {:id :jmd :code "JMD" :display-name (i18n/label :t/currency-display-name-jmd) :symbol "J$"}
   :jpy {:id :jpy :code "JPY" :display-name (i18n/label :t/currency-display-name-jpy) :symbol "¥"}
   :kes {:id :kes :code "KES" :display-name (i18n/label :t/currency-display-name-kes) :symbol "KSh"}
   :krw {:id :krw :code "KRW" :display-name (i18n/label :t/currency-display-name-krw) :symbol "₩"}
   :kwd {:id :kwd :code "KWD" :display-name (i18n/label :t/currency-display-name-kwd) :symbol "د.ك"}
   :kzt {:id :kzt :code "KZT" :display-name (i18n/label :t/currency-display-name-kzt) :symbol "лв"}
   :lkr {:id :lkr :code "LKR" :display-name (i18n/label :t/currency-display-name-lkr) :symbol "₨"}
   :mad {:id :mad :code "MAD" :display-name (i18n/label :t/currency-display-name-mad) :symbol "MAD"}
   :mdl {:id :mdl :code "MDL" :display-name (i18n/label :t/currency-display-name-mdl) :symbol "MDL"}
   :mur {:id :mur :code "MUR" :display-name (i18n/label :t/currency-display-name-mur) :symbol "₨"}
   :mwk {:id :mwk :code "MWK" :display-name (i18n/label :t/currency-display-name-mwk) :symbol "MK"}
   :mxn {:id :mxn :code "MXN" :display-name (i18n/label :t/currency-display-name-mxn) :symbol "$"}
   :myr {:id :myr :code "MYR" :display-name (i18n/label :t/currency-display-name-myr) :symbol "RM"}
   :mzn {:id :mzn :code "MZN" :display-name (i18n/label :t/currency-display-name-mzn) :symbol "MT"}
   :nad {:id :nad :code "NAD" :display-name (i18n/label :t/currency-display-name-nad) :symbol "$"}
   :ngn {:id :ngn :code "NGN" :display-name (i18n/label :t/currency-display-name-ngn) :symbol "₦"}
   :nok {:id :nok :code "NOK" :display-name (i18n/label :t/currency-display-name-nok) :symbol "kr"}
   :npr {:id :npr :code "NPR" :display-name (i18n/label :t/currency-display-name-npr) :symbol "₨"}
   :nzd {:id :nzd :code "NZD" :display-name (i18n/label :t/currency-display-name-nzd) :symbol "$"}
   :omr {:id :omr :code "OMR" :display-name (i18n/label :t/currency-display-name-omr) :symbol "﷼"}
   :pen {:id :pen :code "PEN" :display-name (i18n/label :t/currency-display-name-pen) :symbol "S/."}
   :pgk {:id :pgk :code "PGK" :display-name (i18n/label :t/currency-display-name-pgk) :symbol "K"}
   :php {:id :php :code "PHP" :display-name (i18n/label :t/currency-display-name-php) :symbol "₱"}
   :pkr {:id :pkr :code "PKR" :display-name (i18n/label :t/currency-display-name-pkr) :symbol "₨"}
   :pln {:id :pln :code "PLN" :display-name (i18n/label :t/currency-display-name-pln) :symbol "zł"}
   :pyg {:id :pyg :code "PYG" :display-name (i18n/label :t/currency-display-name-pyg) :symbol "Gs"}
   :qar {:id :qar :code "QAR" :display-name (i18n/label :t/currency-display-name-qar) :symbol "﷼"}
   :ron {:id :ron :code "RON" :display-name (i18n/label :t/currency-display-name-ron) :symbol "lei"}
   :rsd {:id :rsd :code "RSD" :display-name (i18n/label :t/currency-display-name-rsd) :symbol "Дин."}
   :rub {:id :rub :code "RUB" :display-name (i18n/label :t/currency-display-name-rub) :symbol "₽"}
   :sar {:id :sar :code "SAR" :display-name (i18n/label :t/currency-display-name-sar) :symbol "﷼"}
   :sek {:id :sek :code "SEK" :display-name (i18n/label :t/currency-display-name-sek) :symbol "kr"}
   :sgd {:id :sgd :code "SGD" :display-name (i18n/label :t/currency-display-name-sgd) :symbol "$"}
   :thb {:id :thb :code "THB" :display-name (i18n/label :t/currency-display-name-thb) :symbol "฿"}
   :ttd {:id :ttd :code "TTD" :display-name (i18n/label :t/currency-display-name-ttd) :symbol "TT$"}
   :twd {:id :twd :code "TWD" :display-name (i18n/label :t/currency-display-name-twd) :symbol "NT$"}
   :tzs {:id :tzs :code "TZS" :display-name (i18n/label :t/currency-display-name-tzs) :symbol "TSh"}
   :try {:id :try :code "TRY" :display-name (i18n/label :t/currency-display-name-try) :symbol "₺"}
   :uah {:id :uah :code "UAH" :display-name (i18n/label :t/currency-display-name-uah) :symbol "₴"}
   :ugx {:id :ugx :code "UGX" :display-name (i18n/label :t/currency-display-name-ugx) :symbol "USh"}
   :uyu {:id :uyu :code "UYU" :display-name (i18n/label :t/currency-display-name-uyu) :symbol "$U"}
   :usd {:id :usd :code "USD" :display-name (i18n/label :t/currency-display-name-usd) :symbol "$"}
   :vef {:id :vef :code "VEF" :display-name (i18n/label :t/currency-display-name-vef) :symbol "Bs"}
   :vnd {:id :vnd :code "VND" :display-name (i18n/label :t/currency-display-name-vnd) :symbol "₫"}
   :zar {:id :zar :code "ZAR" :display-name (i18n/label :t/currency-display-name-zar) :symbol "R"}})

;; Used to generate topic for contact discoveries
(def contact-discovery "contact-discovery")

(def ^:const send-transaction-failed-parse-response 1)
(def ^:const send-transaction-failed-parse-params   2)
(def ^:const send-transaction-no-account-selected  3)
(def ^:const send-transaction-invalid-tx-sender    4)
(def ^:const send-transaction-err-decrypt          5)

(def ^:const web3-send-transaction "eth_sendTransaction")
(def ^:const web3-personal-sign "personal_sign")
(def ^:const web3-get-logs "eth_getLogs")
(def ^:const admin-remove-peer "admin_removePeer")

(def ^:const event-transfer-hash
  (ethereum/sha3 "Transfer(address,address,uint256)"))

(def regx-emoji #"^((?:[\u261D\u26F9\u270A-\u270D]|\uD83C[\uDF85\uDFC2-\uDFC4\uDFC7\uDFCA-\uDFCC]|\uD83D[\uDC42\uDC43\uDC46-\uDC50\uDC66-\uDC69\uDC6E\uDC70-\uDC78\uDC7C\uDC81-\uDC83\uDC85-\uDC87\uDCAA\uDD74\uDD75\uDD7A\uDD90\uDD95\uDD96\uDE45-\uDE47\uDE4B-\uDE4F\uDEA3\uDEB4-\uDEB6\uDEC0\uDECC]|\uD83E[\uDD18-\uDD1C\uDD1E\uDD1F\uDD26\uDD30-\uDD39\uDD3D\uDD3E\uDDD1-\uDDDD])(?:\uD83C[\uDFFB-\uDFFF])?|(?:[\u231A\u231B\u23E9-\u23EC\u23F0\u23F3\u25FD\u25FE\u2614\u2615\u2648-\u2653\u267F\u2693\u26A1\u26AA\u26AB\u26BD\u26BE\u26C4\u26C5\u26CE\u26D4\u26EA\u26F2\u26F3\u26F5\u26FA\u26FD\u2705\u270A\u270B\u2728\u274C\u274E\u2753-\u2755\u2757\u2795-\u2797\u27B0\u27BF\u2B1B\u2B1C\u2B50\u2B55]|\uD83C[\uDC04\uDCCF\uDD8E\uDD91-\uDD9A\uDDE6-\uDDFF\uDE01\uDE1A\uDE2F\uDE32-\uDE36\uDE38-\uDE3A\uDE50\uDE51\uDF00-\uDF20\uDF2D-\uDF35\uDF37-\uDF7C\uDF7E-\uDF93\uDFA0-\uDFCA\uDFCF-\uDFD3\uDFE0-\uDFF0\uDFF4\uDFF8-\uDFFF]|\uD83D[\uDC00-\uDC3E\uDC40\uDC42-\uDCFC\uDCFF-\uDD3D\uDD4B-\uDD4E\uDD50-\uDD67\uDD7A\uDD95\uDD96\uDDA4\uDDFB-\uDE4F\uDE80-\uDEC5\uDECC\uDED0-\uDED2\uDEEB\uDEEC\uDEF4-\uDEF8]|\uD83E[\uDD10-\uDD3A\uDD3C-\uDD3E\uDD40-\uDD45\uDD47-\uDD4C\uDD50-\uDD6B\uDD80-\uDD97\uDDC0\uDDD0-\uDDE6])|(?:[#\*0-9\xA9\xAE\u203C\u2049\u2122\u2139\u2194-\u2199\u21A9\u21AA\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA\u24C2\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE\u2600-\u2604\u260E\u2611\u2614\u2615\u2618\u261D\u2620\u2622\u2623\u2626\u262A\u262E\u262F\u2638-\u263A\u2640\u2642\u2648-\u2653\u2660\u2663\u2665\u2666\u2668\u267B\u267F\u2692-\u2697\u2699\u269B\u269C\u26A0\u26A1\u26AA\u26AB\u26B0\u26B1\u26BD\u26BE\u26C4\u26C5\u26C8\u26CE\u26CF\u26D1\u26D3\u26D4\u26E9\u26EA\u26F0-\u26F5\u26F7-\u26FA\u26FD\u2702\u2705\u2708-\u270D\u270F\u2712\u2714\u2716\u271D\u2721\u2728\u2733\u2734\u2744\u2747\u274C\u274E\u2753-\u2755\u2757\u2763\u2764\u2795-\u2797\u27A1\u27B0\u27BF\u2934\u2935\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55\u3030\u303D\u3297\u3299]|\uD83C[\uDC04\uDCCF\uDD70\uDD71\uDD7E\uDD7F\uDD8E\uDD91-\uDD9A\uDDE6-\uDDFF\uDE01\uDE02\uDE1A\uDE2F\uDE32-\uDE3A\uDE50\uDE51\uDF00-\uDF21\uDF24-\uDF93\uDF96\uDF97\uDF99-\uDF9B\uDF9E-\uDFF0\uDFF3-\uDFF5\uDFF7-\uDFFF]|\uD83D[\uDC00-\uDCFD\uDCFF-\uDD3D\uDD49-\uDD4E\uDD50-\uDD67\uDD6F\uDD70\uDD73-\uDD7A\uDD87\uDD8A-\uDD8D\uDD90\uDD95\uDD96\uDDA4\uDDA5\uDDA8\uDDB1\uDDB2\uDDBC\uDDC2-\uDDC4\uDDD1-\uDDD3\uDDDC-\uDDDE\uDDE1\uDDE3\uDDE8\uDDEF\uDDF3\uDDFA-\uDE4F\uDE80-\uDEC5\uDECB-\uDED2\uDEE0-\uDEE5\uDEE9\uDEEB\uDEEC\uDEF0\uDEF3-\uDEF8]|\uD83E[\uDD10-\uDD3A\uDD3C-\uDD3E\uDD40-\uDD45\uDD47-\uDD4C\uDD50-\uDD6B\uDD80-\uDD97\uDDC0\uDDD0-\uDDE6])\uFE0F|[\t-\r \xA0\u1680\u2000-\u200A\u2028\u2029\u202F\u205F\u3000\uFEFF])+$")

(def ^:const dapp-permission-contact-code "CONTACT_CODE")
(def ^:const dapp-permission-web3 "WEB3")
(def ^:const status-api-success "status-api-success")
(def ^:const status-api-request "status-api-request")
(def ^:const history-state-changed "history-state-changed")
(def ^:const web3-send-async "web3-send-async")
(def ^:const web3-send-async-callback "web3-send-async-callback")
