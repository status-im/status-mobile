(ns status-im.utils.ethereum.tokens
  (:require [status-im.ui.components.styles :as styles]
            [status-im.utils.ethereum.core :as ethereum])
  (:require-macros [status-im.utils.ethereum.macros :refer [resolve-icons]]))

(defn- asset-border [color]
  {:border-color color :border-width 1 :border-radius 32})

(def ethereum {:name     "Ethereum"
               :symbol   :ETH
               :decimals 18
               :icon     {:source (js/require "./resources/images/assets/ethereum.png")
                          :style  (asset-border styles/color-light-blue-transparent)}})

(def all
  {:mainnet
   (resolve-icons
     [{:symbol   :EOS
       :name     "EOS"
       :address  "0x86fa049857e0209aa7d9e616f7eb3b3b78ecfdb0"
       :decimals 18}
      {:symbol   :OMG
       :name     "OmiseGo"
       :address  "0xd26114cd6EE289AccF82350c8d8487fedB8A0C07"
       :decimals 18}
      {:symbol   :PPT
       :name     "Populous"
       :address  "0xd4fa1460f537bb9085d22c7bccb5dd450ef28e3a"
       :decimals 18}
      {:symbol   :REP
       :name     "Augur"
       :address  "0xe94327d07fc17907b4db788e5adf2ed424addff6"
       :decimals 18}
      {:symbol   :POWR
       :name     "PowerLedger"
       :address  "0x595832f8fc6bf59c85c527fec3740a1b7a361269"
       :decimals 18}
      {:symbol   :PAY
       :name     "TenXPay"
       :address  "0xB97048628DB6B661D4C2aA833e95Dbe1A905B280"
       :decimals 18}
      {:symbol   :VERI
       :name     "Veros"
       :address  "0xedbaf3c5100302dcdda53269322f3730b1f0416d"
       :decimals 18}
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
       :name     "DGD"
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
      {:symbol   :ATMChain
       :name     "Attention Token of Media"
       :address  "0x9B11EFcAAA1890f6eE52C6bB7CF8153aC5d74139"
       :decimals 8}
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
       :name     "LoopringCoin"
       :address  "0xEF68e7C694F40c8202821eDF525dE3782458639f"
       :decimals 18}
      {:symbol   :ZSC
       :name     "Zeus Shield Token"
       :address  "0x7A41e0517a5ecA4FdbC7FbebA4D4c47B9fF6DC63"
       :decimals 18}
      {:symbol   :DATA
       :name     "Streamr DATAcoin"
       :address  "0x0cf0ee63788a0849fe5297f3407f701e122cc023"
       :decimals 18}
      {:symbol   :RCN
       :name     "Ripio Credit Network Token"
       :address  "0xf970b8e36e23f7fc3fd752eea86f8be8d83375a6"
       :decimals 9}
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
      {:symbol   :Centra
       :name     "Centra Token"
       :address  "0x96A65609a7B84E8842732DEB08f56C3E21aC6f8a"
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
       :name     "GRID+ Token"
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
       :name     "R Token"
       :address  "0x48f775efbe4f5ece6e0df2f7b5932df56823b990"
       :decimals 0}
      {:symbol   :1ST
       :name     "FirstBlood Token"
       :address  "0xaf30d2a7e90d7dc361c8c4585e9bb7d2f6f15bc7"
       :decimals 18}
      {:symbol   :CFI
       :name     "Cofoundit"
       :address  "0x12fef5e57bf45873cd9b62e9dbd7bfb99e32d73e"
       :decimals 2}
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
       :decimals 0}
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
      {:symbol   :DCN
       :name     "Dentacoin"
       :address  "0x08d32b0da63e2C3bcF8019c9c5d849d7a9d791e6"
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
       :name     "AVT"
       :address  "0x0d88ed6e74bbfd96b831231638b66c05571e824f"
       :decimals 18}
      {:symbol   :TIME
       :name     "TIME"
       :address  "0x6531f133e6deebe7f2dce5a0441aa7ef330b4e53"
       :decimals 18}
      {:symbol   :CND
       :name     "Cindicator Token"
       :address  "0xd4c435f5b09f855c3317c8524cb1f586e42795fa"
       :decimals 18}
      {:symbol   :STX
       :name     "STOX"
       :address  "0x006BeA43Baa3f7A6f765F14f10A1a1b08334EF45"
       :decimals 18}
      {:symbol   :XAUR
       :name     "Xaurum"
       :address  "0x4DF812F6064def1e5e029f1ca858777CC98D2D81"
       :decimals 8}
      {:symbol   :VIB
       :name     "Vibe"
       :address  "0x2c974b2d0ba1716e644c1fc59982a89ddd2ff724"
       :decimals 18}
      {:symbol   :PRG
       :name     "PRG"
       :address  "0x7728dFEF5aBd468669EB7f9b48A7f70a501eD29D"
       :decimals 6}
      {:symbol   :DPY
       :name     "Delphy"
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
       :name     "DomRaider Token"
       :address  "0x9af4f26941677c706cfecf6d3379ff01bb85d5ab"
       :decimals 8}
      {:symbol   :ROL
       :name     "Dice"
       :address  "0x2e071D2966Aa7D8dECB1005885bA1977D6038A65"
       :decimals 16}])
   :testnet
   (resolve-icons
     [{:name     "Status Test Token"
       :symbol   :STT
       :decimals 18
       :address  "0xc55cf4b03948d7ebc8b9e8bad92643703811d162"}])})

(defn tokens-for [chain-id]
  (get all chain-id))

(defn token-for [chain-id symbol]
  (some #(if (= symbol (:symbol %)) %) (tokens-for chain-id)))