(ns status-im.utils.ethereum.tokens
  (:require-macros [status-im.utils.ethereum.macros :refer [resolve-icons]])
  (:require [clojure.string :as string]
            [status-im.utils.config :as config]))

(defn- asset-border [color]
  {:border-color color :border-width 1 :border-radius 32})

(def ethereum {:name     "Ether"
               :symbol   :ETH
               :decimals 18
               :icon     {:source (js/require "./resources/images/assets/ethereum.png")
                          ;; TODO(goranjovic) find a better place to set UI info
                          ;; like colors. Removed the reference to component.styles to
                          ;; avoid circular dependency between namespaces.
                          :style  (asset-border "#628fe333")}})

(defn ethereum? [k]
  (= k (:symbol ethereum)))

;; symbol are used as global identifier (per network) so they must be unique

(def all
  {:mainnet
   (resolve-icons :mainnet
                  [{:symbol   :DAI
                    :name     "DAI"
                    :address  "0x89d24a6b4ccb1b6faa2625fe562bdd9a23260359"
                    :decimals 18}
                   {:symbol   :EOS
                    :name     "EOS"
                    :address  "0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0"
                    :decimals 18}
                   {:symbol   :OMG
                    :name     "OMGToken"
                    :address  "0xd26114cd6EE289AccF82350c8d8487fedB8A0C07"
                    :decimals 18}
                   {:symbol   :PPT
                    :name     "Populous Platform"
                    :address  "0xd4fa1460f537bb9085d22c7bccb5dd450ef28e3a"
                    :decimals 8}
                   {:symbol   :REP
                    :name     "Reputation"
                    :address  "0x1985365e9f78359a9B6AD760e32412f4a445E862"
                    :decimals 18}
                   {:symbol   :POWR
                    :name     "PowerLedger"
                    :address  "0x595832f8fc6bf59c85c527fec3740a1b7a361269"
                    :decimals 6}
                   {:symbol   :PAY
                    :name     "TenX Pay Token"
                    :address  "0xB97048628DB6B661D4C2aA833e95Dbe1A905B280"
                    :decimals 18}
                   {:symbol   :VRS
                    :name     "VEROS"
                    :address  "0xedbaf3c5100302dcdda53269322f3730b1f0416d"
                    :decimals 5}
                   {:symbol   :GNT
                    :name     "Golem Network Token"
                    :address  "0xa74476443119A942dE498590Fe1f2454d7D4aC0d"
                    :decimals 18}
                   {:symbol   :SALT
                    :name     "Salt"
                    :address  "0x4156D3342D5c385a87D264F90653733592000581"
                    :decimals 8}
                   {:symbol   :BNB
                    :name     "BNB"
                    :address  "0xB8c77482e45F1F44dE1745F52C74426C631bDD52"
                    :decimals 18}
                   {:symbol   :BAT
                    :name     "Basic Attention Token"
                    :address  "0x0d8775f648430679a709e98d2b0cb6250d2887ef"
                    :decimals 18}
                   {:symbol   :KNC
                    :name     "Kyber Network Crystal"
                    :address  "0xdd974d5c2e2928dea5f71b9825b8b646686bd200"
                    :decimals 18}
                   {:symbol   :DGD
                    :name     "Digix DAO"
                    :address  "0xe0b7927c4af23765cb51314a0e0521a9645f0e2a"
                    :decimals 9}
                   {:symbol   :AE
                    :name     "Aeternity"
                    :address  "0x5ca9a71b1d01849c0a95490cc00559717fcf0d1d"
                    :decimals 18}
                   {:symbol   :TRX
                    :name     "Tronix"
                    :address  "0xf230b790e05390fc8295f4d3f60332c93bed42e2"
                    :decimals 6}
                   {:symbol   :BQX
                    :name     "Bitquence"
                    :address  "0x5af2be193a6abca9c8817001f45744777db30756"
                    :decimals 8}
                   {:symbol   :RDN
                    :name     "Raiden Token"
                    :address  "0x255aa6df07540cb5d3d297f0d0d4d84cb52bc8e6"
                    :decimals 18}
                   {:symbol   :SNT
                    :name     "Status Network"
                    :address  "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
                    :decimals 18}
                   {:symbol   :SNGLS
                    :name     "SingularDTV"
                    :address  "0xaec2e87e0a235266d9c5adc9deb4b2e29b54d009"
                    :decimals 0}
                   {:symbol   :GNO
                    :name     "Gnosis Token"
                    :address  "0x6810e776880c02933d47db1b9fc05908e5386b96"
                    :decimals 18}
                   {:symbol   :STORJ
                    :name     "StorjToken"
                    :address  "0xb64ef51c888972c908cfacf59b47c1afbc0ab8ac"
                    :decimals 8}
                   {:symbol   :ADX
                    :name     "AdEx"
                    :address  "0x4470bb87d77b963a013db939be332f927f2b992e"
                    :decimals 4}
                   {:symbol   :FUN
                    :name     "FunFair"
                    :address  "0x419d0d8bdd9af5e606ae2232ed285aff190e711b"
                    :decimals 8}
                   {:symbol   :CVC
                    :name     "Civic"
                    :address  "0x41e5560054824ea6b0732e656e3ad64e20e94e45"
                    :decimals 8}
                   {:symbol   :ICN
                    :name     "ICONOMI"
                    :address  "0x888666CA69E0f178DED6D75b5726Cee99A87D698"
                    :decimals 18}
                   {:symbol   :WTC
                    :name     "Walton Token"
                    :address  "0xb7cb1c96db6b22b0d3d9536e0108d062bd488f74"
                    :decimals 18}
                   {:symbol   :BTM
                    :name     "Bytom"
                    :address  "0xcb97e65f07da24d46bcdd078ebebd7c6e6e3d750"
                    :decimals 8}
                   {:symbol   :ZRX
                    :name     "0x Protocol Token"
                    :address  "0xe41d2489571d322189246dafa5ebde1f4699f498"
                    :decimals 18}
                   {:symbol   :BNT
                    :name     "Bancor Network Token"
                    :address  "0x1f573d6fb3f13d689ff844b4ce37794d79a7ff1c"
                    :decimals 18}
                   {:symbol   :MTL
                    :name     "Metal"
                    :address  "0xF433089366899D83a9f26A773D59ec7eCF30355e"
                    :decimals 8}
                   {:symbol   :PPP
                    :name     "PayPie"
                    :address  "0xc42209aCcC14029c1012fB5680D95fBd6036E2a0"
                    :decimals 18}
                   {:symbol   :LINK
                    :name     "ChainLink Token"
                    :address  "0x514910771af9ca656af840dff83e8264ecf986ca"
                    :decimals 18}
                   {:symbol   :KIN
                    :name     "Kin"
                    :address  "0x818fc6c2ec5986bc6e2cbf00939d90556ab12ce5"
                    :decimals 18}
                   {:symbol   :ANT
                    :name     "Aragon Network Token"
                    :address  "0x960b236A07cf122663c4303350609A66A7B288C0"
                    :decimals 18}
                   {:symbol   :MGO
                    :name     "MobileGo Token"
                    :address  "0x40395044Ac3c0C57051906dA938B54BD6557F212"
                    :decimals 8}
                   {:symbol   :MCO
                    :name     "Monaco"
                    :address  "0xb63b606ac810a52cca15e44bb630fd42d8d1d83d"
                    :decimals 8}
                   {:symbol   :LRC
                    :name     "loopring"
                    :address  "0xEF68e7C694F40c8202821eDF525dE3782458639f"
                    :decimals 18}
                   {:symbol   :ZSC
                    :name     "Zeus Shield Coin"
                    :address  "0x7A41e0517a5ecA4FdbC7FbebA4D4c47B9fF6DC63"
                    :decimals 18}
                   {:symbol   :DATA
                    :name     "DATAcoin"
                    :address  "0x0cf0ee63788a0849fe5297f3407f701e122cc023"
                    :decimals 18}
                   {:symbol   :RCN
                    :name     "Ripio Credit Network Token"
                    :address  "0xf970b8e36e23f7fc3fd752eea86f8be8d83375a6"
                    :decimals 18}
                   {:symbol   :WINGS
                    :name     "WINGS"
                    :address  "0x667088b212ce3d06a1b553a7221E1fD19000d9aF"
                    :decimals 18}
                   {:symbol   :EDG
                    :name     "Edgeless"
                    :address  "0x08711d3b02c8758f2fb3ab4e80228418a7f8e39c"
                    :decimals 0}
                   {:symbol   :MLN
                    :name     "Melon Token"
                    :address  "0xBEB9eF514a379B997e0798FDcC901Ee474B6D9A1"
                    :decimals 18}
                   {:symbol   :MDA
                    :name     "Moeda Loyalty Points"
                    :address  "0x51db5ad35c671a87207d88fc11d593ac0c8415bd"
                    :decimals 18}
                   {:symbol   :PLR
                    :name     "PILLAR"
                    :address  "0xe3818504c1b32bf1557b16c238b2e01fd3149c17"
                    :decimals 18}
                   {:symbol   :QRL
                    :name     "QRL"
                    :address  "0x697beac28b09e122c4332d163985e8a73121b97f"
                    :decimals 8}
                   {:symbol   :MOD
                    :name     "Modum Token"
                    :address  "0x957c30ab0426e0c93cd8241e2c60392d08c6ac8e"
                    :decimals 0}
                   {:symbol   :TAAS
                    :name     "Token-as-a-Service"
                    :address  "0xe7775a6e9bcf904eb39da2b68c5efb4f9360e08c"
                    :decimals 6}
                   {:symbol   :GRID
                    :name     "GRID Token"
                    :address  "0x12b19d3e2ccc14da04fae33e63652ce469b3f2fd"
                    :decimals 12}
                   {:symbol   :SAN
                    :name     "SANtiment network token"
                    :address  "0x7c5a0ce9267ed19b22f8cae653f198e3e8daf098"
                    :decimals 18}
                   {:symbol   :SNM
                    :name     "SONM Token"
                    :address  "0x983f6d60db79ea8ca4eb9968c6aff8cfa04b3c63"
                    :decimals 18}
                   {:symbol   :REQ
                    :name     "Request Token"
                    :address  "0x8f8221afbb33998d8584a2b05749ba73c37a938a"
                    :decimals 18}
                   {:symbol   :SUB
                    :name     "Substratum"
                    :address  "0x12480e24eb5bec1a9d4369cab6a80cad3c0a377a"
                    :decimals 2}
                   {:symbol   :MANA
                    :name     "Decentraland"
                    :address  "0x0f5d2fb29fb7d3cfee444a200298f468908cc942"
                    :decimals 18}
                   {:symbol   :AST
                    :name     "AirSwap Token"
                    :address  "0x27054b13b1b798b345b591a4d22e6562d47ea75a"
                    :decimals 4}
                   {:symbol   :R
                    :name     "R token"
                    :address  "0x48f775efbe4f5ece6e0df2f7b5932df56823b990"
                    :decimals 0}
                   {:symbol   :1ST
                    :name     "Firstblood"
                    :address  "0xaf30d2a7e90d7dc361c8c4585e9bb7d2f6f15bc7"
                    :decimals 18}
                   {:symbol   :CFI
                    :name     "Cofoundit"
                    :address  "0x12fef5e57bf45873cd9b62e9dbd7bfb99e32d73e"
                    :decimals 18}
                   {:symbol   :ENG
                    :name     "Enigma"
                    :address  "0xf0ee6b27b759c9893ce4f094b49ad28fd15a23e4"
                    :decimals 8}
                   {:symbol   :AMB
                    :name     "Amber Token"
                    :address  "0x4dc3643dbc642b72c158e7f3d2ff232df61cb6ce"
                    :decimals 18}
                   {:symbol   :XPA
                    :name     "XPlay Token"
                    :address  "0x90528aeb3a2b736b780fd1b6c478bb7e1d643170"
                    :decimals 18}
                   {:symbol   :OTN
                    :name     "Open Trading Network"
                    :address  "0x881ef48211982d01e2cb7092c915e647cd40d85c"
                    :decimals 18}
                   {:symbol   :TRST
                    :name     "Trustcoin"
                    :address  "0xcb94be6f13a1182e4a4b6140cb7bf2025d28e41b"
                    :decimals 6}
                   {:symbol   :TKN
                    :name     "Monolith TKN"
                    :address  "0xaaaf91d9b90df800df4f55c205fd6989c977e73a"
                    :decimals 8}
                   {:symbol   :RHOC
                    :name     "RHOC"
                    :address  "0x168296bb09e24a88805cb9c33356536b980d3fc5"
                    :decimals 8}
                   {:symbol   :TGT
                    :name     "Target Coin"
                    :address  "0xac3da587eac229c9896d919abc235ca4fd7f72c1"
                    :decimals 1}
                   {:symbol   :EVX
                    :name     "Everex"
                    :address  "0xf3db5fa2c66b7af3eb0c0b782510816cbe4813b8"
                    :decimals 4}
                   {:symbol   :ICOS
                    :name     "ICOS"
                    :address  "0x014b50466590340d41307cc54dcee990c8d58aa8"
                    :decimals 6}
                   {:symbol   :DNT
                    :name     "district0x Network Token"
                    :address  "0x0abdace70d3790235af448c88547603b945604ea"
                    :decimals 18}
                   {:symbol   :٨
                    :name     "Dentacoin"
                    :address  "0x08d32b0da63e2C3bcF8019c9c5d849d7a9d791e6"
                    :decimals 0}
                   {:symbol   :EDO
                    :name     "Eidoo Token"
                    :address  "0xced4e93198734ddaff8492d525bd258d49eb388e"
                    :decimals 18}
                   {:symbol   :CSNO
                    :name     "BitDice CSNO"
                    :address  "0x29d75277ac7f0335b2165d0895e8725cbf658d73"
                    :decimals 8}
                   {:symbol   :COB
                    :name     "Cobinhood Token"
                    :address  "0xb2f7eb1f2c37645be61d73953035360e768d81e6"
                    :decimals 18}
                   {:symbol   :ENJ
                    :name     "Enjin Coin"
                    :address  "0xf629cbd94d3791c9250152bd8dfbdf380e2a3b9c"
                    :decimals 18}
                   {:symbol   :AVT
                    :name     "AVENTUS"
                    :address  "0x0d88ed6e74bbfd96b831231638b66c05571e824f"
                    :decimals 18}
                   {:symbol   :TIME
                    :name     "Chronobank TIME"
                    :address  "0x6531f133e6deebe7f2dce5a0441aa7ef330b4e53"
                    :decimals 8}
                   {:symbol   :CND
                    :name     "Cindicator Token"
                    :address  "0xd4c435f5b09f855c3317c8524cb1f586e42795fa"
                    :decimals 18}
                   {:symbol   :STX
                    :name     "Stox"
                    :address  "0x006BeA43Baa3f7A6f765F14f10A1a1b08334EF45"
                    :decimals 18}
                   {:symbol   :XAUR
                    :name     "Xaurum"
                    :address  "0x4DF812F6064def1e5e029f1ca858777CC98D2D81"
                    :decimals 8}
                   {:symbol   :VIB
                    :name     "VIB"
                    :address  "0x2c974b2d0ba1716e644c1fc59982a89ddd2ff724"
                    :decimals 18}
                   {:symbol   :PRG
                    :name     "ParagonCoin"
                    :address  "0x7728dFEF5aBd468669EB7f9b48A7f70a501eD29D"
                    :decimals 6}
                   {:symbol   :DPY
                    :name     "Delphy Token"
                    :address  "0x6c2adc2073994fb2ccc5032cc2906fa221e9b391"
                    :decimals 18}
                   {:symbol   :CDT
                    :name     "CoinDash Token"
                    :address  "0x2fe6ab85ebbf7776fee46d191ee4cea322cecf51"
                    :decimals 18}
                   {:symbol   :TNT
                    :name     "Tierion Network Token"
                    :address  "0x08f5a9235b08173b7569f83645d2c7fb55e8ccd8"
                    :decimals 8}
                   {:symbol   :DRT
                    :name     "Domraider"
                    :address  "0x9af4f26941677c706cfecf6d3379ff01bb85d5ab"
                    :decimals 8}
                   {:symbol   :SPANK
                    :name     "SPANK"
                    :address  "0x42d6622deCe394b54999Fbd73D108123806f6a18"
                    :decimals 18}
                   {:symbol   :BRLN
                    :name     "Berlin Coin"
                    :address  "0x80046305aaab08f6033b56a360c184391165dc2d"
                    :decimals 18}
                   ;; NOTE(goranjovic) : the following three tokens are removed from the Manage Assets list
                   ;; and automatically removed from user's selection by a migration. However, we still need
                   ;; them listed here in order to correctly display any previous transactions the user had
                   ;; in their history prior to the upgrade. So, we're just hiding them, not actually deleting from the
                   ;; app.
                   {:symbol   :CTR
                    :name     "Centra"
                    :address  "0x96A65609a7B84E8842732DEB08f56C3E21aC6f8a"
                    :decimals 18
                    :hidden?  true}
                   {:symbol   :ROL
                    :name     "DICE"
                    :address  "0x2e071D2966Aa7D8dECB1005885bA1977D6038A65"
                    :decimals 16
                    :hidden?  true}
                   {:symbol   :ATM
                    :name     "Attention Token of Media"
                    :address  "0x9B11EFcAAA1890f6eE52C6bB7CF8153aC5d74139"
                    :decimals 8
                    :hidden?  true}
                   {:symbol  :CK
                    :nft?    true
                    :name    "CryptoKitties"
                    :address "0x06012c8cf97bead5deae237070f9587f8e7a266d"}
                   {:symbol  :EMONA
                    :nft?    true
                    :name    "Etheremon"
                    :address "0xB2c0782ae4A299f7358758B2D15dA9bF29E1DD99"}
                   {:symbol  :STRK
                    :nft?    true
                    :name    "CryptoStrikers"
                    :address "0xdcaad9fd9a74144d226dbf94ce6162ca9f09ed7e"}])
   :testnet
   (resolve-icons :testnet
                  [{:name     "Status Test Token"
                    :symbol   :STT
                    :decimals 18
                    ;;NOTE(goranjovic): intentionally checksummed for purposes of testing
                    :address  "0xc55cF4B03948D7EBc8b9E8BAD92643703811d162"}
                   {:name     "Handy Test Token"
                    :symbol   :HND
                    :decimals 0
                    :address  "0xdee43a267e8726efd60c2e7d5b81552dcd4fa35c"}
                   {:name     "Lucky XS Test"
                    :symbol   :LXS
                    :decimals 2
                    :address  "0x703d7dc0bc8e314d65436adf985dda51e09ad43b"}
                   {:name     "Adi Test Token"
                    :symbol   :ADI
                    :decimals 7
                    :address  "0xe639e24346d646e927f323558e6e0031bfc93581"}
                   {:name     "Wagner Test Token"
                    :symbol   :WGN
                    :decimals 10
                    :address  "0x2e7cd05f437eb256f363417fd8f920e2efa77540"}
                   {:name     "Modest Test Token"
                    :symbol   :MDS
                    :decimals 18
                    :address  "0x57cc9b83730e6d22b224e9dc3e370967b44a2de0"}])

   :rinkeby
   (resolve-icons :rinkeby
                  [{:name     "Moksha Coin"
                    :symbol   :MOKSHA
                    :decimals 18
                    :address  "0x6ba7dc8dd10880ab83041e60c4ede52bb607864b"}])

   :custom []})

(defn tokens-for [chain]
  (get all chain))

(defn nfts-for [chain]
  (filter :nft? (tokens-for chain)))

(defn sorted-tokens-for [chain]
  (->> (tokens-for chain)
       (filter #(not (:hidden? %)))
       (sort #(compare (string/lower-case (:name %1))
                       (string/lower-case (:name %2))))))

(defn symbol->token [chain symbol]
  (some #(when (= symbol (:symbol %)) %) (tokens-for chain)))

(defn address->token [chain address]
  (some #(when (= (string/lower-case address)
                  (string/lower-case (:address %))) %) (tokens-for chain)))

(defn asset-for [chain symbol]
  (if (= (:symbol ethereum) symbol)
    ethereum
    (symbol->token chain symbol)))
