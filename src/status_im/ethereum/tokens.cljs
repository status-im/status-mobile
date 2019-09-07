(ns status-im.ethereum.tokens
  (:require [clojure.string :as string])
  (:require-macros
   [status-im.ethereum.macros :as ethereum.macros :refer [resolve-icons]]))

(defn- asset-border [color]
  {:border-color color :border-width 1 :border-radius 32})

(def default-native-currency
  (memoize
   (fn []
     {:name     "Native"
      :symbol   :ETH
      :decimals 18
      :icon     {:source (js/require "../resources/images/tokens/default-native.png")}})))

(def all-native-currencies
  (ethereum.macros/resolve-native-currency-icons
   {:mainnet {:name     "Ether"
              :symbol   :ETH
              :decimals 18
              :icon     {:style (asset-border "#628fe333")}}
    :testnet {:name           "Ropsten Ether"
              :symbol         :ETH
              :symbol-display :ETHro
              :decimals       18}
    :rinkeby {:name           "Rinkeby Ether"
              :symbol         :ETH
              :symbol-display :ETHri
              :decimals       18}
    :poa     {:name           "POA"
              :symbol         :ETH
              :symbol-display :POA
              :decimals       18}
    :xdai    {:name            "xDAI"
              :symbol          :ETH
              :symbol-display  :xDAI
              :symbol-exchange :DAI
              :decimals        18}}))

(def native-currency-symbols
  (set (map #(-> % val :symbol) all-native-currencies)))

(defn native-currency [chain]
  (-> (get all-native-currencies chain (default-native-currency))))

(defn ethereum? [symbol]
  (native-currency-symbols symbol))

;; NOTE(goranjovic) - fields description:
;;
;; - address - token contract address
;; - symbol - token identifier, must be unique within network
;; - name - token display name
;; - decimals - the maximum number of decimals (raw balance must be divided by 10^decimals to get the actual amount)
;; - nft? - set to true when token is an ERC-781 collectible
;; - hidden? - when true, token is not displayed in any asset selection screens, but will be displayed properly in
;;             transaction history (setting this field is a form of "soft" token removal).
;; - skip-decimals-check? - some tokens do not include the decimals field, which is compliant with ERC-20 since it is
;;;     and optional field. In that case we are explicitly skipping this step in order not to raise a false error.
;;;     We have this explicit flag for decimals and not for name and symbol because we can't tell apart unset decimals
;;;     from 0 decimals case.

(def all-default-tokens
  {:mainnet
   (resolve-icons :mainnet
                  [{:symbol   :DAI
                    :name     "Dai Stablecoin"
                    :address  "0x6b175474e89094c44da98b954eedeac495271d0f"
                    :decimals 18}
                   {:symbol   :SAI
                    :name     "Sai Stablecoin v1.0"
                    :address  "0x89d24a6b4ccb1b6faa2625fe562bdd9a23260359"
                    :decimals 18}
                   {:symbol   :MKR
                    :name     "MKR"
                    :address  "0x9f8f72aa9304c8b593d555f12ef6589cc3a579a2"
                    :decimals 18}
                   {:symbol   :EOS
                    :name     "EOS"
                    :address  "0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0"
                    :decimals 18}
                   {:symbol   :OMG
                    :name     "OMGToken"
                    :address  "0xd26114cd6ee289accf82350c8d8487fedb8a0c07"
                    :decimals 18}
                   {:symbol   :PPT
                    :name     "Populous Platform"
                    :address  "0xd4fa1460f537bb9085d22c7bccb5dd450ef28e3a"
                    :decimals 8}
                   {:symbol   :REP
                    :name     "Reputation"
                    :address  "0x1985365e9f78359a9b6ad760e32412f4a445e862"
                    :decimals 18}
                   {:symbol   :POWR
                    :name     "PowerLedger"
                    :address  "0x595832f8fc6bf59c85c527fec3740a1b7a361269"
                    :decimals 6}
                   {:symbol   :PAY
                    :name     "TenX Pay Token"
                    :address  "0xb97048628db6b661d4c2aa833e95dbe1a905b280"
                    :decimals 18}
                   {:symbol   :VRS
                    :name     "Veros"
                    :address  "0x92e78dae1315067a8819efd6dca432de9dcde2e9"
                    :decimals 6}
                   {:symbol   :GNT
                    :name     "Golem Network Token"
                    :address  "0xa74476443119a942de498590fe1f2454d7d4ac0d"
                    :decimals 18}
                   {:symbol   :SALT
                    :name     "Salt"
                    :address  "0x4156d3342d5c385a87d264f90653733592000581"
                    :decimals 8}
                   {:symbol   :BNB
                    :name     "BNB"
                    :address  "0xb8c77482e45f1f44de1745f52c74426c631bdd52"
                    :decimals 18}
                   {:symbol   :BAT
                    :name     "Basic Attention Token"
                    :address  "0x0d8775f648430679a709e98d2b0cb6250d2887ef"
                    :decimals 18}
                   {:symbol   :KNC
                    :name     "Kyber Network Crystal"
                    :address  "0xdd974d5c2e2928dea5f71b9825b8b646686bd200"
                    :decimals 18}
                   {:symbol   :BTU
                    :name     "BTU Protocol"
                    :address  "0xb683D83a532e2Cb7DFa5275eED3698436371cc9f"
                    :decimals 18}
                   {:symbol   :DGD
                    :name     "Digix DAO"
                    :address  "0xe0b7927c4af23765cb51314a0e0521a9645f0e2a"
                    :decimals 9
                    :skip-decimals-check? true}
                   {:symbol   :AE
                    :name     "Aeternity"
                    :address  "0x5ca9a71b1d01849c0a95490cc00559717fcf0d1d"
                    :decimals 18}
                   {:symbol   :TRX
                    :name     "Tronix"
                    :address  "0xf230b790e05390fc8295f4d3f60332c93bed42e2"
                    :decimals 6}
                   {:symbol   :ETHOS
                    :name     "Ethos"
                    :address  "0x5af2be193a6abca9c8817001f45744777db30756"
                    :decimals 8}
                   {:symbol   :RDN
                    :name     "Raiden Token"
                    :address  "0x255aa6df07540cb5d3d297f0d0d4d84cb52bc8e6"
                    :decimals 18}
                   {:symbol   :SNT
                    :name     "Status Network Token"
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
                    :address  "0x888666ca69e0f178ded6d75b5726cee99a87d698"
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
                    :address  "0xf433089366899d83a9f26a773d59ec7ecf30355e"
                    :decimals 8}
                   {:symbol   :PPP
                    :name     "PayPie"
                    :address  "0xc42209accc14029c1012fb5680d95fbd6036e2a0"
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
                    :address  "0x960b236a07cf122663c4303350609a66a7b288c0"
                    :decimals 18}
                   {:symbol   :MGO
                    :name     "MobileGo Token"
                    :address  "0x40395044ac3c0c57051906da938b54bd6557f212"
                    :decimals 8}
                   {:symbol   :MCO
                    :name     "Monaco"
                    :address  "0xb63b606ac810a52cca15e44bb630fd42d8d1d83d"
                    :decimals 8}
                   {:symbol   :LRC
                    :name     "loopring"
                    :address  "0xef68e7c694f40c8202821edf525de3782458639f"
                    :decimals 18
                    :skip-decimals-check? true}
                   {:symbol   :ZSC
                    :name     "Zeus Shield Coin"
                    :address  "0x7a41e0517a5eca4fdbc7fbeba4d4c47b9ff6dc63"
                    :decimals 18}
                   {:symbol   :DATA
                    :name     "Streamr DATAcoin"
                    :address  "0x0cf0ee63788a0849fe5297f3407f701e122cc023"
                    :decimals 18}
                   {:symbol   :RCN
                    :name     "Ripio Credit Network Token"
                    :address  "0xf970b8e36e23f7fc3fd752eea86f8be8d83375a6"
                    :decimals 18}
                   {:symbol   :WINGS
                    :name     "WINGS"
                    :address  "0x667088b212ce3d06a1b553a7221e1fd19000d9af"
                    :decimals 18}
                   {:symbol   :EDG
                    :name     "Edgeless"
                    :address  "0x08711d3b02c8758f2fb3ab4e80228418a7f8e39c"
                    :decimals 0}
                   {:symbol   :MLN
                    :name     "Melon Token"
                    :address  "0xbeb9ef514a379b997e0798fdcc901ee474b6d9a1"
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
                    :name     "Decentraland MANA"
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
                    :name     "FirstBlood Token"
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
                    :address  "0x08d32b0da63e2c3bcf8019c9c5d849d7a9d791e6"
                    :decimals 0}
                   {:symbol   :EDO
                    :name     "Eidoo Token"
                    :address  "0xced4e93198734ddaff8492d525bd258d49eb388e"
                    :decimals 18}
                   {:symbol   :CSNO
                    :name     "BitDice"
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
                    :address  "0x006bea43baa3f7a6f765f14f10a1a1b08334ef45"
                    :decimals 18}
                   {:symbol   :XAUR
                    :name     "Xaurum"
                    :address  "0x4df812f6064def1e5e029f1ca858777cc98d2d81"
                    :decimals 8}
                   {:symbol   :VIB
                    :name     "Vibe"
                    :address  "0x2c974b2d0ba1716e644c1fc59982a89ddd2ff724"
                    :decimals 18}
                   {:symbol   :PRG
                    :name     "PRG"
                    :address  "0x7728dfef5abd468669eb7f9b48a7f70a501ed29d"
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
                    :name     "DomRaiderToken"
                    :address  "0x9af4f26941677c706cfecf6d3379ff01bb85d5ab"
                    :decimals 8}
                   {:symbol   :SPANK
                    :name     "SPANK"
                    :address  "0x42d6622dece394b54999fbd73d108123806f6a18"
                    :decimals 18}
                   {:symbol   :BRLN
                    :name     "Berlin Coin"
                    :address  "0x80046305aaab08f6033b56a360c184391165dc2d"
                    :decimals 18}
                   {:symbol   :USDC
                    :name     "USD//C"
                    :address  "0xa0b86991c6218b36c1d19d4a2e9eb0ce3606eb48"
                    :decimals 6}
                   {:symbol   :LPT
                    :name     "Livepeer Token"
                    :address  "0x58b6a8a3302369daec383334672404ee733ab239"
                    :decimals 18}
                   {:symbol   :ST
                    :name     "Simple Token"
                    :address  "0x2c4e8f2d746113d0696ce89b35f0d8bf88e0aeca"
                    :decimals 18}
                   {:symbol   :WBTC
                    :name     "Wrapped BTC"
                    :address  "0x2260fac5e5542a773aa44fbcfedf7c193bc2c599"
                    :decimals 8}
                   {:symbol   :BLT
                    :name     "Bloom Token"
                    :address  "0x107c4504cd79c5d2696ea0030a8dd4e92601b82e"
                    :decimals 18}
                   ;; NOTE(goranjovic): the following three tokens are removed from the Manage Assets list
                   ;; and automatically removed from user's selection by a migration. However, we still need
                   ;; them listed here in order to correctly display any previous transactions the user had
                   ;; in their history prior to the upgrade. So, we're just hiding them, not actually deleting from the
                   ;; app.
                   {:symbol   :Centra
                    :name     "Centra token"
                    :address  "0x96a65609a7b84e8842732deb08f56c3e21ac6f8a"
                    :decimals 18
                    :hidden?  true}
                   {:symbol   :ROL
                    :name     "DICE"
                    :address  "0x2e071d2966aa7d8decb1005885ba1977d6038a65"
                    :decimals 16
                    :hidden?  true}
                   {:symbol   :ATM
                    :name     "Attention Token of Media"
                    :address  "0x9b11efcaaa1890f6ee52c6bb7cf8153ac5d74139"
                    :decimals 8
                    :hidden?  true}
                   ;; NOTE(goranjovic): the following tokens are collectibles
                   {:symbol  :CK
                    :nft?    true
                    :name    "CryptoKitties"
                    :address "0x06012c8cf97bead5deae237070f9587f8e7a266d"}
                   {:symbol  :EMONA
                    :nft?    true
                    :name    "EtheremonAsset"
                    :address "0xb2c0782ae4a299f7358758b2d15da9bf29e1dd99"}
                   {:symbol  :STRK
                    :nft?    true
                    :name    "CryptoStrikers"
                    :address "0xdcaad9fd9a74144d226dbf94ce6162ca9f09ed7e"}
                   {:symbol  :SUPR
                    :nft?    true
                    :name    "SupeRare"
                    :address "0x41a322b28d0ff354040e2cbc676f0320d8c8850d"}
                   {:symbol  :KDO
                    :nft?    true
                    :name    "KudosToken"
                    :address "0x2aea4add166ebf38b63d09a75de1a7b94aa24163"}])
   :testnet
   (resolve-icons :testnet
                  [{:name     "Status Test Token"
                    :symbol   :STT
                    :decimals 18
                    ;;NOTE(goranjovic): intentionally checksummed for purposes of testing
                    :address  "0xc55cf4b03948d7ebc8b9e8bad92643703811d162"}
                   {:name     "Handy Test Token"
                    :symbol   :HND
                    :decimals 0
                    :address  "0xdee43a267e8726efd60c2e7d5b81552dcd4fa35c"}
                   {:name     "Lucky Test Token"
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
                    :address  "0x6ba7dc8dd10880ab83041e60c4ede52bb607864b"}
                   {:symbol   :KDO
                    :nft?     true
                    :name     "KudosToken"
                    :address  "0x93bb0afbd0627bbd3a6c72bc318341d3a22e254a"}
                   {:symbol   :WIBB
                    :name     "WIBB"
                    :address  "0x7d4ccf6af2f0fdad48ee7958bcc28bdef7b732c7"
                    :decimals 18}])

   :xdai
   (resolve-icons :xdai
                  [{:name     "buffiDai"
                    :symbol   :BUFF
                    :decimals 18
                    :address  "0x3e50bf6703fc132a94e4baff068db2055655f11b"}])

   :custom []})

(defn nfts-for [all-tokens]
  (filter :nft? (vals all-tokens)))

(defn sorted-tokens-for [all-tokens]
  (->> (vals all-tokens)
       (filter #(not (:hidden? %)))
       (sort #(compare (string/lower-case (:name %1))
                       (string/lower-case (:name %2))))))

(defn symbol->token [all-tokens symbol]
  (some #(when (= symbol (:symbol %)) %) (vals all-tokens)))

(defn address->token [all-tokens address]
  (get all-tokens (string/lower-case address)))

(defn asset-for [all-tokens chain symbol]
  (let [native-coin (native-currency chain)]
    (if (or (= (:symbol-display native-coin) symbol)
            (= (:symbol native-coin) symbol))
      native-coin
      (symbol->token all-tokens symbol))))
