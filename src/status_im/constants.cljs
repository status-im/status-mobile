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

(def min-password-length 6)
(def max-chat-name-length 20)
(def response-suggesstion-resize-duration 100)
(def default-number-of-messages 20)
(def blocks-per-hour 120)

(def console-chat-id "console")

(def default-network config/default-network)

(def system "system")

(def default-wallet-transactions
  {:filters
   {:type [{:id :inbound   :label (i18n/label :t/incoming)  :checked? true}
           {:id :outbound  :label (i18n/label :t/outgoing)  :checked? true}
           {:id :pending   :label (i18n/label :t/pending)   :checked? true}
           ;; TODO(jeluard) Restore once we support postponing transaction
           #_{:id :postponed :label (i18n/label :t/postponed) :checked? true}]}})

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

(def default-wnodes
  {:testnet {"mailserver-a" {:id      "mailserver-a"
                             :name    "Status mailserver A"
                             :address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"}
             "mailserver-b" {:id      "mailserver-b"
                             :name    "Status mailserver B"
                             :address "enode://07ac64fe0e9b2d4ecbfe0ccaeab3d8d95fa8858754f511104bb403b19255d7bf47a8416bdd0df01f6720ff82164451b7a028c93d15ddd37b7e8382d74e91ebc2@167.99.209.72:30504"}}
   :mainnet {"mailserver-a" {:id      "mailserver-a"
                             :name    "Status mailserver A"
                             :address "enode://1da276e34126e93babf24ec88aac1a7602b4cbb2e11b0961d0ab5e989ca9c261aa7f7c1c85f15550a5f1e5a5ca2305b53b9280cf5894d5ecf7d257b173136d40@167.99.209.61:30504"}
             "mailserver-b" {:id      "mailserver-b"
                             :name    "Status mailserver B"
                             :address "enode://07ac64fe0e9b2d4ecbfe0ccaeab3d8d95fa8858754f511104bb403b19255d7bf47a8416bdd0df01f6720ff82164451b7a028c93d15ddd37b7e8382d74e91ebc2@167.99.209.72:30504"}}
   :rinkeby {"mailserver-a" {:id     "mailserver-a"
                             :name   "Status mailserver A"
                             :address "enode://43829580446ad138386dadb7fa50b6bd4d99f7c28659a0bc08115f8c0380005922a340962496f6af756a42b94a1522baa38a694fa27de59c3a73d4e08d5dbb31@206.189.6.48:30504"}
             "mailserver-b" {:id      "mailserver-b"
                             :name    "Status mailserver B"
                             :address "enode://70a2004e78399075f566033c42e9a0b1d43c683d4742755bb5457d03191be66a1b48c2b4fb259696839f28646a5828a1958b900860e27897f984ad0fc8482404@206.189.56.154:30504"}}})

(defn default-account-settings []
  {:wallet {:visible-tokens {:testnet #{:STT :ATT}
                             :mainnet #{:SNT}}}
   :wnode {:testnet (rand-nth (keys (:testnet default-wnodes)))
           :mainnet (rand-nth (keys (:mainnet default-wnodes)))
           :rinkeby (rand-nth (keys (:rinkeby default-wnodes)))}})

(def currencies
  {:aed {:id :aed :code "AED" :display-name (i18n/label :t/currency-display-name-aed) :symbol "د.إ"}
   :afn {:id :afn :code "AFN" :display-name (i18n/label :t/currency-display-name-afn) :symbol "؋"}
   :ars {:id :ars :code "ARS" :display-name (i18n/label :t/currency-display-name-ars) :symbol "$"}
   :aud {:id :aud :code "AUD" :display-name (i18n/label :t/currency-display-name-aud) :symbol "$"}
   :bbd {:id :bbd :code "BBD" :display-name (i18n/label :t/currency-display-name-bbd) :symbol "$"}
   :bdt {:id :bdt :code "BDT" :display-name (i18n/label :t/currency-display-name-bdt) :symbol " Tk"}
   :bgn {:id :bgn :code "BGN" :display-name (i18n/label :t/currency-display-name-bgn) :symbol "лв"}
   :bhd {:id :bhd :code "BHD" :display-name (i18n/label :t/currency-display-name-bhd) :symbol "BD"}
   :bmd {:id :bmd :code "BMD" :display-name (i18n/label :t/currency-display-name-bmd) :symbol "$"}
   :bnd {:id :bnd :code "BND" :display-name (i18n/label :t/currency-display-name-bnd) :symbol "$"}
   :bob {:id :bob :code "BOB" :display-name (i18n/label :t/currency-display-name-bob) :symbol "$b"}
   :brl {:id :brl :code "BRL" :display-name (i18n/label :t/currency-display-name-brl) :symbol "R$"}
   :btn {:id :btn :code "BTN" :display-name (i18n/label :t/currency-display-name-btn) :symbol "Nu."}
   :bzd {:id :bzd :code "BZD" :display-name (i18n/label :t/currency-display-name-bzd) :symbol "BZ$"}
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
   :gmd {:id :gmd :code "GMD" :display-name (i18n/label :t/currency-display-name-gmd) :symbol "D"}
   :gyd {:id :gyd :code "GYD" :display-name (i18n/label :t/currency-display-name-gyd) :symbol "$"}
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
   :kyd {:id :kyd :code "KYD" :display-name (i18n/label :t/currency-display-name-kyd) :symbol "$"}
   :kzt {:id :kzt :code "KZT" :display-name (i18n/label :t/currency-display-name-kzt) :symbol "лв"}
   :lak {:id :lak :code "LAK" :display-name (i18n/label :t/currency-display-name-lak) :symbol "₭"}
   :lkr {:id :lkr :code "LKR" :display-name (i18n/label :t/currency-display-name-lkr) :symbol "₨"}
   :lrd {:id :lrd :code "LRD" :display-name (i18n/label :t/currency-display-name-lrd) :symbol "$"}
   :ltl {:id :ltl :code "LTL" :display-name (i18n/label :t/currency-display-name-ltl) :symbol "Lt"}
   :mad {:id :mad :code "MAD" :display-name (i18n/label :t/currency-display-name-mad) :symbol "MAD"}
   :mdl {:id :mdl :code "MDL" :display-name (i18n/label :t/currency-display-name-mdl) :symbol "MDL"}
   :mkd {:id :mkd :code "MKD" :display-name (i18n/label :t/currency-display-name-mkd) :symbol "ден"}
   :mnt {:id :mnt :code "MNT" :display-name (i18n/label :t/currency-display-name-mnt) :symbol "₮"}
   :mur {:id :mur :code "MUR" :display-name (i18n/label :t/currency-display-name-mur) :symbol "₨"}
   :mwk {:id :mwk :code "MWK" :display-name (i18n/label :t/currency-display-name-mwk) :symbol "MK"}
   :mxn {:id :mxn :code "MXN" :display-name (i18n/label :t/currency-display-name-mxn) :symbol "$"}
   :myr {:id :myr :code "MYR" :display-name (i18n/label :t/currency-display-name-myr) :symbol "RM"}
   :mzn {:id :mzn :code "MZN" :display-name (i18n/label :t/currency-display-name-mzn) :symbol "MT"}
   :nad {:id :nad :code "NAD" :display-name (i18n/label :t/currency-display-name-nad) :symbol "$"}
   :ngn {:id :ngn :code "NGN" :display-name (i18n/label :t/currency-display-name-ngn) :symbol "₦"}
   :nio {:id :nio :code "NIO" :display-name (i18n/label :t/currency-display-name-nio) :symbol "C$"}
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
   :sos {:id :sos :code "SOS" :display-name (i18n/label :t/currency-display-name-sos) :symbol "S"}
   :srd {:id :srd :code "SRD" :display-name (i18n/label :t/currency-display-name-srd) :symbol "$"}
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
   :yer {:id :yer :code "YER" :display-name (i18n/label :t/currency-display-name-yer) :symbol "﷼"}
   :zar {:id :zar :code "ZAR" :display-name (i18n/label :t/currency-display-name-zar) :symbol "R"}})

(def inbox-password "status-offline-inbox")

;; Used to generate topic for contact discoveries
(def contact-discovery "contact-discovery")

(def ^:const send-transaction-no-error-code        "0")
(def ^:const send-transaction-default-error-code   "1")
(def ^:const send-transaction-password-error-code  "2")
(def ^:const send-transaction-timeout-error-code   "3")
(def ^:const send-transaction-discarded-error-code "4")

(def ^:const web3-send-transaction "eth_sendTransaction")
(def ^:const web3-personal-sign "personal_sign")
(def ^:const web3-get-logs "eth_getLogs")

(def ^:const event-transfer-hash
  (ethereum/sha3 "Transfer(address,address,uint256)"))

(def regx-emoji #"^((?:[\u261D\u26F9\u270A-\u270D]|\uD83C[\uDF85\uDFC2-\uDFC4\uDFC7\uDFCA-\uDFCC]|\uD83D[\uDC42\uDC43\uDC46-\uDC50\uDC66-\uDC69\uDC6E\uDC70-\uDC78\uDC7C\uDC81-\uDC83\uDC85-\uDC87\uDCAA\uDD74\uDD75\uDD7A\uDD90\uDD95\uDD96\uDE45-\uDE47\uDE4B-\uDE4F\uDEA3\uDEB4-\uDEB6\uDEC0\uDECC]|\uD83E[\uDD18-\uDD1C\uDD1E\uDD1F\uDD26\uDD30-\uDD39\uDD3D\uDD3E\uDDD1-\uDDDD])(?:\uD83C[\uDFFB-\uDFFF])?|(?:[\u231A\u231B\u23E9-\u23EC\u23F0\u23F3\u25FD\u25FE\u2614\u2615\u2648-\u2653\u267F\u2693\u26A1\u26AA\u26AB\u26BD\u26BE\u26C4\u26C5\u26CE\u26D4\u26EA\u26F2\u26F3\u26F5\u26FA\u26FD\u2705\u270A\u270B\u2728\u274C\u274E\u2753-\u2755\u2757\u2795-\u2797\u27B0\u27BF\u2B1B\u2B1C\u2B50\u2B55]|\uD83C[\uDC04\uDCCF\uDD8E\uDD91-\uDD9A\uDDE6-\uDDFF\uDE01\uDE1A\uDE2F\uDE32-\uDE36\uDE38-\uDE3A\uDE50\uDE51\uDF00-\uDF20\uDF2D-\uDF35\uDF37-\uDF7C\uDF7E-\uDF93\uDFA0-\uDFCA\uDFCF-\uDFD3\uDFE0-\uDFF0\uDFF4\uDFF8-\uDFFF]|\uD83D[\uDC00-\uDC3E\uDC40\uDC42-\uDCFC\uDCFF-\uDD3D\uDD4B-\uDD4E\uDD50-\uDD67\uDD7A\uDD95\uDD96\uDDA4\uDDFB-\uDE4F\uDE80-\uDEC5\uDECC\uDED0-\uDED2\uDEEB\uDEEC\uDEF4-\uDEF8]|\uD83E[\uDD10-\uDD3A\uDD3C-\uDD3E\uDD40-\uDD45\uDD47-\uDD4C\uDD50-\uDD6B\uDD80-\uDD97\uDDC0\uDDD0-\uDDE6])|(?:[#\*0-9\xA9\xAE\u203C\u2049\u2122\u2139\u2194-\u2199\u21A9\u21AA\u231A\u231B\u2328\u23CF\u23E9-\u23F3\u23F8-\u23FA\u24C2\u25AA\u25AB\u25B6\u25C0\u25FB-\u25FE\u2600-\u2604\u260E\u2611\u2614\u2615\u2618\u261D\u2620\u2622\u2623\u2626\u262A\u262E\u262F\u2638-\u263A\u2640\u2642\u2648-\u2653\u2660\u2663\u2665\u2666\u2668\u267B\u267F\u2692-\u2697\u2699\u269B\u269C\u26A0\u26A1\u26AA\u26AB\u26B0\u26B1\u26BD\u26BE\u26C4\u26C5\u26C8\u26CE\u26CF\u26D1\u26D3\u26D4\u26E9\u26EA\u26F0-\u26F5\u26F7-\u26FA\u26FD\u2702\u2705\u2708-\u270D\u270F\u2712\u2714\u2716\u271D\u2721\u2728\u2733\u2734\u2744\u2747\u274C\u274E\u2753-\u2755\u2757\u2763\u2764\u2795-\u2797\u27A1\u27B0\u27BF\u2934\u2935\u2B05-\u2B07\u2B1B\u2B1C\u2B50\u2B55\u3030\u303D\u3297\u3299]|\uD83C[\uDC04\uDCCF\uDD70\uDD71\uDD7E\uDD7F\uDD8E\uDD91-\uDD9A\uDDE6-\uDDFF\uDE01\uDE02\uDE1A\uDE2F\uDE32-\uDE3A\uDE50\uDE51\uDF00-\uDF21\uDF24-\uDF93\uDF96\uDF97\uDF99-\uDF9B\uDF9E-\uDFF0\uDFF3-\uDFF5\uDFF7-\uDFFF]|\uD83D[\uDC00-\uDCFD\uDCFF-\uDD3D\uDD49-\uDD4E\uDD50-\uDD67\uDD6F\uDD70\uDD73-\uDD7A\uDD87\uDD8A-\uDD8D\uDD90\uDD95\uDD96\uDDA4\uDDA5\uDDA8\uDDB1\uDDB2\uDDBC\uDDC2-\uDDC4\uDDD1-\uDDD3\uDDDC-\uDDDE\uDDE1\uDDE3\uDDE8\uDDEF\uDDF3\uDDFA-\uDE4F\uDE80-\uDEC5\uDECB-\uDED2\uDEE0-\uDEE5\uDEE9\uDEEB\uDEEC\uDEF0\uDEF3-\uDEF8]|\uD83E[\uDD10-\uDD3A\uDD3C-\uDD3E\uDD40-\uDD45\uDD47-\uDD4C\uDD50-\uDD6B\uDD80-\uDD97\uDDC0\uDDD0-\uDDE6])\uFE0F|[\t-\r \xA0\u1680\u2000-\u200A\u2028\u2029\u202F\u205F\u3000\uFEFF])+$")
